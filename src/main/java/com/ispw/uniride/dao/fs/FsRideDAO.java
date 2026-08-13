package com.ispw.uniride.dao.fs;

import com.ispw.uniride.dao.RideDAO;
import com.ispw.uniride.model.Ride;
import com.ispw.uniride.model.state.AvailableState;
import com.ispw.uniride.model.state.CancelledState;
import com.ispw.uniride.model.state.CompletedState;
import com.ispw.uniride.model.state.FullState;
import com.ispw.uniride.model.state.RideState;
import com.ispw.uniride.utils.LoggerCustom;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private static Map<String, Ride> cache = null;
    private static long lastModified = 0;

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
            }
        }
    }

    private void refreshCache() {
        File file = new File(FILE_PATH);
        // Primo controllo (senza lock): se la cache esiste già ed è aggiornata, evitiamo del
        // tutto il costo di acquisire il lock — ottimizzazione per il caso comune (nessuna
        // modifica esterna dall'ultima lettura).
        if (cache == null || file.lastModified() > lastModified) {
            synchronized (LOCK) {
                // Secondo controllo IDENTICO, ma dentro il lock: necessario perché un altro
                // thread potrebbe aver già ricaricato la cache fra il primo controllo e
                // l'acquisizione del lock (pattern check-lock-check, evita ricariche duplicate).
                if (cache == null || file.lastModified() > lastModified) {
                    loadCacheFromFile(file);
                }
            }
        }
    }

    /**
     * Legge integralmente rides.csv e ricostruisce la cache in memoria, riga per riga.
     * Estratta da {@code refreshCache} per mantenere la complessità cognitiva di ciascun
     * metodo entro la soglia consentita.
     */
    private static void loadCacheFromFile(File file) {
        Map<String, Ride> loaded = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            // Ogni riga del CSV rappresenta un Ride: la si converte e la si indicizza per id.
            while ((line = br.readLine()) != null) {
                Ride ride = parseRideFromCsvLine(line);
                if (ride != null) {
                    loaded.put(ride.getId(), ride);
                }
            }
            // Solo a lettura completata sostituiamo la cache: se il file fosse corrotto a metà
            // lettura, non si arriva mai a questo punto e la vecchia cache (se presente) resta valida.
            cache = loaded;
            lastModified = file.lastModified();
        } catch (IOException e) {
            LoggerCustom.error("Error reading rides.csv", e);
            // Anche in caso di errore assegniamo la cache (parziale/vuota): meglio un elenco
            // corse vuoto che un'eccezione non gestita propagata al Controller.
            cache = loaded;
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
        // silenziosamente invece di lanciare un'eccezione che bloccherebbe l'intero caricamento.
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
        ride.setState(parseState(parts[8]));

        // I campi dopo il decimo (indice MIN_CSV_FIELDS) sono, uno per uno, gli username dei
        // passeggeri prenotati: numero variabile, per questo non fanno parte dei campi fissi.
        for (int i = MIN_CSV_FIELDS; i < parts.length; i++) {
            if (!parts[i].trim().isEmpty()) {
                ride.addPassengerUsername(parts[i]);
            }
        }
        return ride;
    }

    /**
     * Ricostruisce l'istanza di {@link RideState} concreta a partire dalla sigla testuale persistita.
     */
    private static RideState parseState(String status) {
        // Traduzione stringa -> oggetto State: il CSV non può salvare direttamente un'istanza
        // Java, quindi si persiste solo l'etichetta testuale e la si "reidrata" a un oggetto
        // State concreto qui, in lettura.
        switch (status) {
            case "AVAILABLE":
                return new AvailableState();
            case "COMPLETED":
                return new CompletedState();
            case "CANCELLED":
                return new CancelledState();
            default:
                // Qualunque valore non riconosciuto (compreso "FULL") ricade su FullState:
                // scelta prudente, un Ride "sconosciuto" viene trattato come non prenotabile
                // piuttosto che come liberamente prenotabile.
                return new FullState();
        }
    }

    private void rewriteFileFromCache() {
        synchronized (LOCK) {
            // false = non in append: il file viene troncato e riscritto da zero ad ogni chiamata,
            // così la cache in RAM (fonte di verità dopo una modifica) e il file restano identici,
            // senza righe duplicate o obsolete lasciate indietro.
            try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_PATH, false))) {
                for (Ride r : cache.values()) {
                    pw.println(formatRideAsCsvLine(r));
                }
                // Aggiorniamo subito lastModified al valore post-scrittura: evita che la prossima
                // refreshCache() interpreti la nostra stessa scrittura come "modifica esterna"
                // e ricarichi inutilmente da file quello che abbiamo appena scritto.
                lastModified = new File(FILE_PATH).lastModified();
            } catch (IOException e) {
                LoggerCustom.error("Error writing rides.csv", e);
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
        refreshCache();
        synchronized (LOCK) {
            cache.put(ride.getId(), ride);
            rewriteFileFromCache();
        }
    }

    @Override
    public List<Ride> getAllRides() {
        refreshCache();
        // Copia difensiva: il chiamante non deve poter alterare la cache interna manipolando
        // la lista ricevuta.
        return new ArrayList<>(cache.values());
    }

    @Override
    public List<Ride> getAvailableRides(String departure, String destination) {
        refreshCache();
        List<Ride> available = new ArrayList<>();
        for (Ride ride : cache.values()) {
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
        refreshCache();
        return cache.get(id);
    }

    @Override
    public void updateRide(Ride ride) {
        // Un aggiornamento è, sul nostro storage CSV, indistinguibile da un nuovo salvataggio:
        // in entrambi i casi la riga esistente (stesso id) viene sovrascritta per intero.
        saveRide(ride);
    }

    @Override
    public List<Ride> getRidesByDriver(String username) {
        refreshCache();
        List<Ride> result = new ArrayList<>();
        for (Ride ride : cache.values()) {
            if (ride.getDriverUsername().equals(username)) {
                result.add(ride);
            }
        }
        return result;
    }

    @Override
    public List<Ride> getRidesByPassenger(String username) {
        refreshCache();
        List<Ride> result = new ArrayList<>();
        for (Ride ride : cache.values()) {
            if (ride.getPassengerUsernames().contains(username)) {
                result.add(ride);
            }
        }
        return result;
    }

    @Override
    public void deleteRide(String id) {
        refreshCache();
        synchronized (LOCK) {
            cache.remove(id);
            rewriteFileFromCache();
        }
    }
}
