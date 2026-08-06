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

public class FsRideDAO implements RideDAO {
    private static final String FILE_PATH = "rides.csv";
    private static final Object LOCK = new Object();

    // Numero minimo di campi attesi su una riga CSV valida (id, driver, partenza, destinazione,
    // data, posti totali, posti disponibili, prezzo, stato); eventuali passeggeri seguono.
    private static final int MIN_CSV_FIELDS = 9;

    // Static Cache
    private static Map<String, Ride> cache = null;
    private static long lastModified = 0;

    public FsRideDAO() {
        ensureFileExists();
    }

    private void ensureFileExists() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            try {
                boolean created = file.createNewFile();
                if (!created) {
                    LoggerCustom.warning("rides.csv risultava assente ma non è stato possibile crearlo (probabile race condition concorrente).");
                }
            } catch (IOException e) {
                LoggerCustom.error("Failed to create rides.csv", e);
            }
        }
    }

    private void refreshCache() {
        File file = new File(FILE_PATH);
        if (cache == null || file.lastModified() > lastModified) {
            synchronized (LOCK) {
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
    private void loadCacheFromFile(File file) {
        Map<String, Ride> loaded = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                Ride ride = parseRideFromCsvLine(line);
                if (ride != null) {
                    loaded.put(ride.getId(), ride);
                }
            }
            cache = loaded;
            lastModified = file.lastModified();
        } catch (IOException e) {
            LoggerCustom.error("Error reading rides.csv", e);
            cache = loaded;
        }
    }

    /**
     * Converte una singola riga CSV (id, driverUsername, departure, destination, date, totalSeats,
     * availableSeats, basePrice, status, [passengerUsernames...]) in un'istanza di {@link Ride}.
     * @return il Ride ricostruito, oppure {@code null} se la riga è malformata.
     */
    private Ride parseRideFromCsvLine(String line) {
        String[] parts = line.split(",");
        if (parts.length < MIN_CSV_FIELDS) {
            return null;
        }

        Ride ride = new Ride(parts[1], parts[2], parts[3], parts[4], Integer.parseInt(parts[5]), Double.parseDouble(parts[7]));
        ride.setId(parts[0]);
        ride.setAvailableSeats(Integer.parseInt(parts[6]));
        ride.setState(parseState(parts[8]));

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
    private RideState parseState(String status) {
        switch (status) {
            case "AVAILABLE":
                return new AvailableState();
            case "COMPLETED":
                return new CompletedState();
            case "CANCELLED":
                return new CancelledState();
            default:
                return new FullState();
        }
    }

    private void rewriteFileFromCache() {
        synchronized (LOCK) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_PATH, false))) {
                for (Ride r : cache.values()) {
                    pw.println(formatRideAsCsvLine(r));
                }
                lastModified = new File(FILE_PATH).lastModified();
            } catch (IOException e) {
                LoggerCustom.error("Error writing rides.csv", e);
            }
        }
    }

    private String formatRideAsCsvLine(Ride r) {
        StringBuilder sb = new StringBuilder();
        sb.append(r.getId()).append(",").append(r.getDriverUsername()).append(",")
          .append(r.getDeparture()).append(",").append(r.getDestination()).append(",")
          .append(r.getDate()).append(",").append(r.getTotalSeats()).append(",")
          .append(r.getAvailableSeats()).append(",").append(r.getBasePrice()).append(",")
          .append(r.getStatus());

        for (String pass : r.getPassengerUsernames()) {
            sb.append(",").append(pass);
        }
        return sb.toString();
    }

    @Override
    public void saveRide(Ride ride) {
        refreshCache();
        synchronized (LOCK) {
            cache.put(ride.getId(), ride);
            rewriteFileFromCache();
        }
    }

    @Override
    public List<Ride> getAllRides() {
        refreshCache();
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
