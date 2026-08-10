package com.ispw.uniride.dao.memory;

import com.ispw.uniride.dao.BookingDAO;
import com.ispw.uniride.model.Booking;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementazione concreta di {@link BookingDAO} per la famiglia In-Memory.
 * Come le altre due Memory*DAO, non ha stato proprio: legge/scrive sulla HashMap condivisa
 * di {@link MemoryStorage}, indicizzata per id della richiesta di prenotazione.
 */
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
        // Tutte le richieste (pendenti, confermate, rifiutate...) relative a un dato passaggio:
        // usato dal guidatore per vedere chi ha chiesto un posto sulla sua corsa.
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
        // Tutte le richieste fatte da un dato passeggero: usato per la vista "Le mie prenotazioni".
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
        // Stessa put() del salvataggio iniziale: usata dopo confirm()/reject()/cancel() sul
        // Booking, per persistere il nuovo valore di stato.
        storage.getBookingsMap().put(booking.getId(), booking);
    }
}
