package com.ispw.uniride.dao.fs;

import com.ispw.uniride.dao.BookingDAO;
import com.ispw.uniride.model.Booking;
import com.ispw.uniride.utils.LoggerCustom;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FsBookingDAO implements BookingDAO {
    private static final String FILE_PATH = "bookings.csv";
    private static final Object LOCK = new Object();

    // Static Cache
    private static Map<String, Booking> cache = null;
    private static long lastModified = 0;

    public FsBookingDAO() {
        ensureFileExists();
    }

    private void ensureFileExists() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                LoggerCustom.error("Failed to create bookings.csv", e);
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
                            // id, rideId, passengerUsername, state
                            if (parts.length >= 4) {
                                Booking booking = new Booking(parts[1], parts[2]);
                                booking.setId(parts[0]);
                                applyState(booking, parts[3]);
                                cache.put(parts[0], booking);
                            }
                        }
                        lastModified = file.lastModified();
                    } catch (IOException e) {
                        LoggerCustom.error("Error reading bookings.csv", e);
                    }
                }
            }
        }
    }

    /**
     * Riporta un Booking appena ricostruito da riga CSV allo stato persistito, rigiocando
     * la transizione corrispondente (il costruttore parte sempre da REQUESTED).
     */
    private void applyState(Booking booking, String state) {
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
            try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_PATH, false))) {
                for (Map.Entry<String, Booking> entry : cache.entrySet()) {
                    Booking b = entry.getValue();
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
        refreshCache();
        synchronized (LOCK) {
            cache.put(booking.getId(), booking);
            rewriteFileFromCache();
        }
    }
}
