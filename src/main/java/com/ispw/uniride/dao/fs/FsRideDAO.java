package com.ispw.uniride.dao.fs;

import com.ispw.uniride.dao.RideDAO;
import com.ispw.uniride.model.Ride;
import com.ispw.uniride.model.state.AvailableState;
import com.ispw.uniride.model.state.FullState;
import com.ispw.uniride.utils.LoggerCustom;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FsRideDAO implements RideDAO {
    private static final String FILE_PATH = "rides.csv";
    private static final Object LOCK = new Object();

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
                file.createNewFile();
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
                    cache = new HashMap<>();
                    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            String[] parts = line.split(",");
                            if (parts.length >= 9) {
                                // id, driverUsername, departure, destination, date, totalSeats, availableSeats, basePrice, status
                                Ride ride = new Ride(parts[1], parts[2], parts[3], parts[4], Integer.parseInt(parts[5]), Double.parseDouble(parts[7]));
                                // Override generated ID and state
                                ride.setId(parts[0]);
                                ride.setAvailableSeats(Integer.parseInt(parts[6]));
                                if ("AVAILABLE".equals(parts[8])) {
                                    ride.setState(new AvailableState());
                                } else {
                                    ride.setState(new FullState());
                                }
                                cache.put(parts[0], ride);
                            }
                        }
                        lastModified = file.lastModified();
                    } catch (IOException e) {
                        LoggerCustom.error("Error reading rides.csv", e);
                    }
                }
            }
        }
    }

    private void rewriteFileFromCache() {
        synchronized (LOCK) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_PATH, false))) {
                for (Map.Entry<String, Ride> entry : cache.entrySet()) {
                    String id = entry.getKey();
                    Ride r = entry.getValue();
                    pw.println(id + "," + r.getDriverUsername() + "," + r.getDeparture() + "," + r.getDestination() + "," +
                            r.getDate() + "," + r.getTotalSeats() + "," + r.getAvailableSeats() + "," + r.getBasePrice() + "," + r.getStatus());
                }
                lastModified = new File(FILE_PATH).lastModified();
            } catch (IOException e) {
                LoggerCustom.error("Error writing rides.csv", e);
            }
        }
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
        refreshCache();
        synchronized (LOCK) {
            cache.put(ride.getId(), ride);
            rewriteFileFromCache();
        }
    }
}
