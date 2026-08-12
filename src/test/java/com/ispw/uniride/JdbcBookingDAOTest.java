package com.ispw.uniride;

import com.ispw.uniride.dao.BookingDAO;
import com.ispw.uniride.dao.sql.JdbcBookingDAO;
import com.ispw.uniride.model.Booking;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite di Unit Testing (JUnit 5) per {@code JdbcBookingDAO}.
 * @author Alessio Carta
 */
class JdbcBookingDAOTest {

    private final BookingDAO dao = new JdbcBookingDAO();

    private String unique(String prefix) {
        return prefix + "." + System.nanoTime();
    }

    @Test
    void testSaveAndGetBookingById() {
        Booking booking = new Booking(unique("ride"), unique("passenger"));
        dao.saveBooking(booking);

        Booking found = dao.getBookingById(booking.getId());

        assertNotNull(found);
        assertEquals(Booking.REQUESTED, found.getState());
    }

    @Test
    void testGetBookingsByRideAndByPassenger() {
        String rideId = unique("ride");
        String passenger = unique("passenger");
        Booking booking = new Booking(rideId, passenger);
        dao.saveBooking(booking);

        List<Booking> byRide = dao.getBookingsByRide(rideId);
        List<Booking> byPassenger = dao.getBookingsByPassenger(passenger);

        assertEquals(1, byRide.size());
        assertEquals(1, byPassenger.size());
        assertEquals(booking.getId(), byRide.get(0).getId());
    }

    @Test
    void testUpdateBookingPersistsConfirmedState() {
        Booking booking = new Booking(unique("ride"), unique("passenger"));
        dao.saveBooking(booking);

        booking.confirm();
        dao.updateBooking(booking);

        Booking reloaded = dao.getBookingById(booking.getId());
        assertEquals(Booking.CONFIRMED, reloaded.getState());
    }

    @Test
    void testUpdateBookingPersistsRejectedState() {
        Booking booking = new Booking(unique("ride"), unique("passenger"));
        dao.saveBooking(booking);

        booking.reject();
        dao.updateBooking(booking);

        Booking reloaded = dao.getBookingById(booking.getId());
        assertEquals(Booking.REJECTED, reloaded.getState());
    }
}
