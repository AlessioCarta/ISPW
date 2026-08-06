package com.ispw.uniride.dao.memory;

import com.ispw.uniride.dao.BookingDAO;
import com.ispw.uniride.model.Booking;

import java.util.ArrayList;
import java.util.List;

public class MemoryBookingDAO implements BookingDAO {
    private MemoryStorage storage;

    public MemoryBookingDAO() {
        this.storage = MemoryStorage.getInstance();
    }

    @Override
    public void saveBooking(Booking booking) {
        storage.getBookingsMap().put(booking.getId(), booking);
    }

    @Override
    public Booking getBookingById(String id) {
        return storage.getBookingsMap().get(id);
    }

    @Override
    public List<Booking> getBookingsByRide(String rideId) {
        List<Booking> result = new ArrayList<>();
        for (Booking booking : storage.getBookingsList()) {
            if (booking.getRideId().equals(rideId)) {
                result.add(booking);
            }
        }
        return result;
    }

    @Override
    public List<Booking> getBookingsByPassenger(String passengerUsername) {
        List<Booking> result = new ArrayList<>();
        for (Booking booking : storage.getBookingsList()) {
            if (booking.getPassengerUsername().equals(passengerUsername)) {
                result.add(booking);
            }
        }
        return result;
    }

    @Override
    public void updateBooking(Booking booking) {
        storage.getBookingsMap().put(booking.getId(), booking);
    }
}
