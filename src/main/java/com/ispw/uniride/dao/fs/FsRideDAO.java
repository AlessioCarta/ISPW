package com.ispw.uniride.dao.fs;

import com.ispw.uniride.dao.RideDAO;
import com.ispw.uniride.exceptions.PersistenceException;
import com.ispw.uniride.model.Ride;
import com.ispw.uniride.model.state.RideState;
import com.ispw.uniride.utils.LoggerCustom;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Implementazione concreta di {@link RideDAO} per la famiglia File System (Pattern Abstract
 * Factory: prodotta da {@link FsDAOFactory}). Persiste i dati su {@code rides.csv} nella root
 * del progetto, con una cache in RAM invalidata automaticamente in base al timestamp di ultima
 * modifica del file, così da restare coerente anche se il file viene toccato da un altro processo.
 */
public class FsRideDAO implements RideDAO {
    // Percorso del file CSV: relativo alla working directory del processo Java.
    private static final String FILE_PATH = "rides.csv";
    // Oggetto usato esclusivamente come monitor per la sincronizzazione fra thread (non contiene
    // dati): garantisce che letture/scritture sulla cache e sul file non si accavallino.
    private static final Object LOCK = new Object();

    // Numero minimo di campi attesi su una riga CSV valida (id, driver, partenza, destinazione,
    // data, posti totali, posti disponibili, prezzo, stato, orario di partenza); eventuali
    // passeggeri seguono.
    private static final int MIN_CSV_FIELDS = 10;

    // Static Cache: condivisa fra tutte le istanze di FsRideDAO (analogamente a MemoryStorage,
    // ma qui la "fonte di verità" resta il file su disco, la cache è solo un acceleratore).
    // AtomicReference: la cache viene letta anche fuori dal lock (vedi refreshCache), quindi ogni
    // thread deve vedere subito il riferimento aggiornato o azzerato da un altro thread.
    private static final AtomicReference<Map<String, Ride>> CACHE = new AtomicReference<>();
    private static volatile long lastModified = 0;

    public FsRideDAO() {
        // Ogni volta che viene creato un DAO ci si assicura che il file esista già, per non dover
        // gestire FileNotFoundException nei metodi di lettura/scrittura successivi.
        ensureFileExists();
    }

    private void ensureFileExists() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            try {
                boolean created = file.createNewFile();
                if (!created) {
                    // createNewFile() può ritornare false anche per una race condition innocua
                    // (un altro thread/processo l'ha creato nel frattempo): lo logghiamo solo
                    // come warning, non è un errore bloccante.
                    LoggerCustom.warning("rides.csv risultava assente ma non è stato possibile crearlo (probabile race condition concorrente).");
                }
            } catch (IOException e) {
                LoggerCustom.error("Failed to create rides.csv", e);
                // Senza file non è possibile salvare nulla: meglio fallire subito che lasciar
                // credere all'utente che le operazioni successive vadano a buon fine.
                throw new PersistenceException("Impossibile creare il file dati rides.csv.", e);
            }
        }
    }

    private Map<String, Ride> refreshCache() {
        File file = new File(FILE_PATH);
        // Si lavora su un riferimento locale: un altro thread potrebbe azzerare il campo cache
        // (invalidazione dopo una scrittura fallita) mentre questo lo sta ancora usando.
        Map<String, Ride> current = CACHE.get();
        // Primo controllo (senza lock): se la cache esiste già ed è aggiornata, evitiamo del
        // tutto il costo di acquisire il lock — ottimizzazione per il caso comune (nessuna
        // modifica esterna dall'ultima lettura).
        if (current == null || file.lastModified() > lastModified) {
            synchronized (LOCK) {
                // Secondo controllo IDENTICO, ma dentro il lock: necessario perché un altro
                // thread potrebbe aver già ricaricato la cache fra il primo controllo e
                // l'acquisizione del lock (pattern check-lock-check, evita ricariche duplicate).
                if (CACHE.get() == null || file.lastModified() > lastModified) {
                    loadCacheFromFile(file);
                }
                current = CACHE.get();
            }
        }
        return current;
    }

    /**
     * Legge integralmente rides.csv e ricostruisce la cache in memoria, riga per riga.
     * Estratta da {@code refreshCache} per mantenere la complessità cognitiva di ciascun
     * metodo entro la soglia consentita.
     */
    private static void loadCacheFromFile(File file) {
        Map<String, Ride> loaded = new ConcurrentHashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;
            // Ogni riga del CSV rappresenta un Ride: la si converte e la si indicizza per id.
            while ((line = br.readLine()) != null) {
                lineNumber++;
                Ride ride = parseLineSafely(line, lineNumber);
                if (ride != null) {
                    loaded.put(ride.getId(), ride);
                }
            }
            // Solo a lettura completata sostituiamo la cache: se il file fosse corrotto a metà
            // lettura, non si arriva mai a questo punto e la vecchia cache (se presente) resta valida.
            CACHE.set(loaded);
            lastModified = file.lastModified();
        } catch (IOException e) {
            LoggerCustom.error("Error reading rides.csv", e);
            // La cache NON viene sostituita con quella parziale: la successiva riscrittura del
            // file partirebbe da dati incompleti e cancellerebbe da disco le righe non lette.
            throw new PersistenceException("Impossibile leggere il file dati rides.csv.", e);
        }
    }

    /**
     * Converte una riga CSV senza far fallire l'intero caricamento se la riga è corrotta
     * (es. un numero non valido): la riga viene scartata e segnalata nel log col suo numero.
     */
    private static Ride parseLineSafely(String line, int lineNumber) {
        try {
            Ride parsed = parseRideFromCsvLine(line);
            if (parsed == null) {
                LoggerCustom.warning("rides.csv: riga " + lineNumber + " malformata, ignorata.");
            }
            return parsed;
        } catch (RuntimeException e) {
            LoggerCustom.warning("rides.csv: riga " + lineNumber + " non valida (" + e.getMessage() + "), ignorata.");
            return null;
        }
    }

    /**
     * Converte una singola riga CSV (id, driverUsername, departure, destination, date, totalSeats,
     * availableSeats, basePrice, status, departureTime, [passengerUsernames...]) in un'istanza di
     * {@link Ride}.
     * @return il Ride ricostruito, oppure {@code null} se la riga è malformata.
     */
    private static Ride parseRideFromCsvLine(String line) {
        String[] parts = line.split(",", -1);
        // Riga troppo corta: probabilmente scrittura incompleta o corruzione — la si scarta
        // (parseLineSafely la segnala nel log) invece di bloccare l'intero caricamento.
        if (parts.length < MIN_CSV_FIELDS) {
            return null;
        }

        // Campo facoltativo (può essere stato salvato vuoto per passaggi creati prima
        // dell'introduzione di questo campo): stringa vuota -> null, coerentemente con Ride.
        String departureTime = parts[9].trim().isEmpty() ? null : parts[9];

        // Il costruttore pubblico di Ride genera un nuovo id e imposta availableSeats=totalSeats:
        // per questo, subito dopo, sovrascriviamo entrambi i campi con i valori realmente
        // persistiti (setId, setAvailableSeats), altrimenti perderemmo lo stato reale del Ride.
        Ride ride = new Ride(parts[1], parts[2], parts[3], parts[4], Integer.parseInt(parts[5]), Double.parseDouble(parts[7]), departureTime);
        ride.setId(parts[0]);
        ride.setAvailableSeats(Integer.parseInt(parts[6]));
        ride.setState(RideState.fromStatus(parts[8]));

        // I campi dopo il decimo (indice MIN_CSV_FIELDS) sono, uno per uno, gli username dei
        // passeggeri prenotati: numero variabile, per questo non fanno parte dei campi fissi.
        for (int i = MIN_CSV_FIELDS; i < parts.length; i++) {
            if (!parts[i].trim().isEmpty()) {
                ride.addPassengerUsername(parts[i]);
            }
        }
        return ride;
    }

    private void rewriteFile(Map<String, Ride> current) {
        synchronized (LOCK) {
            // false = non in append: il file viene troncato e riscritto da zero ad ogni chiamata,
            // così la cache in RAM (fonte di verità dopo una modifica) e il file restano identici,
            // senza righe duplicate o obsolete lasciate indietro.
            try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_PATH, false))) {
                for (Ride r : current.values()) {
                    pw.println(formatRideAsCsvLine(r));
                }
                // PrintWriter non lancia IOException in scrittura: l'errore va interrogato a mano.
                if (pw.checkError()) {
                    throw new IOException("Scrittura incompleta su " + FILE_PATH);
                }
                // Aggiorniamo subito lastModified al valore post-scrittura: evita che la prossima
                // refreshCache() interpreti la nostra stessa scrittura come "modifica esterna"
                // e ricarichi inutilmente da file quello che abbiamo appena scritto.
                lastModified = new File(FILE_PATH).lastModified();
            } catch (IOException e) {
                LoggerCustom.error("Error writing rides.csv", e);
                throw new PersistenceException("Impossibile salvare i passaggi sul file rides.csv.", e);
            }
        }
    }

    private String formatRideAsCsvLine(Ride r) {
        // Costruzione manuale della riga CSV, campo per campo, nello stesso ordine letto da
        // parseRideFromCsvLine(): l'ordine dei due metodi deve restare sincronizzato a mano.
        StringBuilder sb = new StringBuilder();
        sb.append(r.getId()).append(",").append(r.getDriverUsername()).append(",")
          .append(r.getDeparture()).append(",").append(r.getDestination()).append(",")
          .append(r.getDate()).append(",").append(r.getTotalSeats()).append(",")
          .append(r.getAvailableSeats()).append(",").append(r.getBasePrice()).append(",")
          .append(r.getStatus()).append(",").append(r.getDepartureTime() != null ? r.getDepartureTime() : "");

        // Gli username dei passeggeri vengono accodati come campi extra di lunghezza variabile.
        for (String pass : r.getPassengerUsernames()) {
            sb.append(",").append(pass);
        }
        return sb.toString();
    }

    @Override
    public void saveRide(Ride ride) {
        // Prima ci si assicura che la cache rifletta lo stato più recente del file (nel caso
        // sia stato modificato da un altro processo), poi si applica la modifica e si riscrive.
        synchronized (LOCK) {
            Map<String, Ride> current = refreshCache();
            current.put(ride.getId(), ride);
            try {
                rewriteFile(current);
            } catch (PersistenceException e) {
                invalidateCache();
                throw e;
            }
        }
    }

    @Override
    public List<Ride> getAllRides() {
        Map<String, Ride> current = refreshCache();
        // Copia difensiva: il chiamante non deve poter alterare la cache interna manipolando
        // la lista ricevuta.
        return new ArrayList<>(current.values());
    }

    @Override
    public List<Ride> getAvailableRides(String departure, String destination) {
        Map<String, Ride> current = refreshCache();
        List<Ride> available = new ArrayList<>();
        for (Ride ride : current.values()) {
            if ("AVAILABLE".equals(ride.getStatus()) &&
                ride.getDeparture().equalsIgnoreCase(departure) &&
                ride.getDestination().equalsIgnoreCase(destination)) {
                available.add(ride);
            }
        }
        return available;
    }

    @Override
    public Ride getRideById(String id) {
        Map<String, Ride> current = refreshCache();
        return current.get(id);
    }

    @Override
    public void updateRide(Ride ride) {
        // Un aggiornamento è, sul nostro storage CSV, indistinguibile da un nuovo salvataggio:
        // in entrambi i casi la riga esistente (stesso id) viene sovrascritta per intero.
        saveRide(ride);
    }

    @Override
    public List<Ride> getRidesByDriver(String username) {
        Map<String, Ride> current = refreshCache();
        List<Ride> result = new ArrayList<>();
        for (Ride ride : current.values()) {
            if (ride.getDriverUsername().equals(username)) {
                result.add(ride);
            }
        }
        return result;
    }

    @Override
    public List<Ride> getRidesByPassenger(String username) {
        Map<String, Ride> current = refreshCache();
        List<Ride> result = new ArrayList<>();
        for (Ride ride : current.values()) {
            if (ride.getPassengerUsernames().contains(username)) {
                result.add(ride);
            }
        }
        return result;
    }

    @Override
    public void deleteRide(String id) {
        synchronized (LOCK) {
            Map<String, Ride> current = refreshCache();
            current.remove(id);
            try {
                rewriteFile(current);
            } catch (PersistenceException e) {
                invalidateCache();
                throw e;
            }
        }
    }

    /**
     * Scrittura fallita: la cache viene scartata, così la lettura successiva riparte dal file su
     * disco (la fonte di verità). Ripristinare solo la voce modificata non basterebbe, perché i
     * metodi di lettura restituiscono le istanze stesse della cache, che il Controller potrebbe
     * aver già modificato prima di chiedere il salvataggio.
     */
    private static void invalidateCache() {
        CACHE.set(null);
    }
}
