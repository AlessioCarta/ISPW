package com.ispw.uniride.dao.fs;

import com.ispw.uniride.dao.BookingDAO;
import com.ispw.uniride.model.Booking;
import com.ispw.uniride.utils.LoggerCustom;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementazione concreta di {@link BookingDAO} per la famiglia File System.
 * Persiste su {@code bookings.csv}, con la stessa strategia di cache-invalidation-su-timestamp
 * delle altre due Fs*DAO. Peculiarità: siccome {@link Booking} non ha un costruttore che accetti
 * direttamente uno stato arbitrario (nasce sempre in {@code REQUESTED} per costruzione), la
 * lettura da CSV deve "rigiocare" la transizione giusta invece di assegnare lo stato a mano.
 */
public class FsBookingDAO implements BookingDAO {
    private static final String FILE_PATH = "bookings.csv";
    private static final Object LOCK = new Object();

    // Numero minimo di campi attesi su una riga CSV valida (id, rideId, passengerUsername, state).
    private static final int MIN_CSV_FIELDS = 4;

    // Static Cache: condivisa fra tutte le istanze di FsBookingDAO.
    private static Map<String, Booking> cache = null;
    private static long lastModified = 0;

    public FsBookingDAO() {
        ensureFileExists();
    }

    private void ensureFileExists() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            try {
                boolean created = file.createNewFile();
                if (!created) {
                    LoggerCustom.warning("bookings.csv risultava assente ma non è stato possibile crearlo (probabile race condition concorrente).");
                }
            } catch (IOException e) {
                LoggerCustom.error("Failed to create bookings.csv", e);
            }
        }
    }

    private void refreshCache() {
        File file = new File(FILE_PATH);
        // Stesso pattern check-lock-check delle altre Fs*DAO: evita ricariche non necessarie.
        if (cache == null || file.lastModified() > lastModified) {
            synchronized (LOCK) {
                if (cache == null || file.lastModified() > lastModified) {
                    loadCacheFromFile(file);
                }
            }
        }
    }

    /**
     * Legge integralmente bookings.csv e ricostruisce la cache in memoria, riga per riga.
     */
    private static void loadCacheFromFile(File file) {
        Map<String, Booking> loaded = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                Booking booking = parseBookingFromCsvLine(line);
                if (booking != null) {
                    loaded.put(booking.getId(), booking);
                }
            }
            cache = loaded;
            lastModified = file.lastModified();
        } catch (IOException e) {
            LoggerCustom.error("Error reading bookings.csv", e);
            cache = loaded;
        }
    }

    /**
     * Converte una riga CSV (id, rideId, passengerUsername, state) in un'istanza di {@link Booking}.
     * @return il Booking ricostruito, oppure {@code null} se la riga è malformata.
     */
    private static Booking parseBookingFromCsvLine(String line) {
        String[] parts = line.split(",");
        if (parts.length < MIN_CSV_FIELDS) {
            return null;
        }
        // Il costruttore imposta sempre stato REQUESTED e genera un id nuovo: li sovrascriviamo
        // subito dopo con i valori realmente persistiti (setId, poi lo stato via applyState).
        Booking booking = new Booking(parts[1], parts[2]);
        booking.setId(parts[0]);
        applyState(booking, parts[3]);
        return booking;
    }

    /**
     * Riporta un Booking appena ricostruito da riga CSV allo stato persistito, rigiocando
     * la transizione corrispondente (il costruttore parte sempre da REQUESTED).
     */
    private static void applyState(Booking booking, String state) {
        // Si richiama lo stesso metodo pubblico che userebbe il flusso applicativo reale
        // (confirm/reject/cancel), così le regole di transizione restano uniche e centralizzate
        // in Booking, senza duplicarle qui con un'assegnazione diretta del campo stato.
        if (Booking.CONFIRMED.equals(state)) {
            booking.confirm();
        } else if (Booking.REJECTED.equals(state)) {
            booking.reject();
        } else if (Booking.CANCELLED.equals(state)) {
            booking.cancel();
        }
        // REQUESTED: nessuna transizione necessaria, è lo stato iniziale del costruttore.
    }

    private void rewriteFileFromCache() {
        synchronized (LOCK) {
            // Riscrittura completa (non append), come in FsRideDAO: la cache in RAM è la fonte
            // di verità dopo una modifica, il file viene sempre allineato per intero.
            try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_PATH, false))) {
                for (Booking b : cache.values()) {
                    pw.println(b.getId() + "," + b.getRideId() + "," + b.getPassengerUsername() + "," + b.getState());
                }
                lastModified = new File(FILE_PATH).lastModified();
            } catch (IOException e) {
                LoggerCustom.error("Error writing bookings.csv", e);
            }
        }
    }

    @Override
    public void saveBooking(Booking booking) {
        refreshCache();
        synchronized (LOCK) {
            cache.put(booking.getId(), booking);
            rewriteFileFromCache();
        }
    }

    @Override
    public Booking getBookingById(String id) {
        refreshCache();
        return cache.get(id);
    }

    @Override
    public List<Booking> getBookingsByRide(String rideId) {
        refreshCache();
        List<Booking> result = new ArrayList<>();
        for (Booking booking : cache.values()) {
            if (booking.getRideId().equals(rideId)) {
                result.add(booking);
            }
        }
        return result;
    }

    @Override
    public List<Booking> getBookingsByPassenger(String passengerUsername) {
        refreshCache();
        List<Booking> result = new ArrayList<>();
        for (Booking booking : cache.values()) {
            if (booking.getPassengerUsername().equals(passengerUsername)) {
                result.add(booking);
            }
        }
        return result;
    }

    @Override
    public void updateBooking(Booking booking) {
        // Un aggiornamento è, sul nostro storage CSV, indistinguibile da un nuovo salvataggio:
        // in entrambi i casi la riga esistente (stesso id) viene sovrascritta per intero.
        saveBooking(booking);
    }
}
