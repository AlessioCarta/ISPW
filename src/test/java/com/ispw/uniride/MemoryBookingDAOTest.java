package com.ispw.uniride;

import com.ispw.uniride.dao.BookingDAO;
import com.ispw.uniride.dao.memory.MemoryBookingDAO;
import com.ispw.uniride.model.Booking;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite di Unit Testing (JUnit 5) per {@code MemoryBookingDAO}.
 */
class MemoryBookingDAOTest {

    private final BookingDAO dao = new MemoryBookingDAO();

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
    void testGetBookingsByRide() {
        String rideId = unique("ride");
        Booking booking = new Booking(rideId, unique("passenger"));
        dao.saveBooking(booking);

        List<Booking> bookings = dao.getBookingsByRide(rideId);

        assertEquals(1, bookings.size());
        assertEquals(booking.getId(), bookings.get(0).getId());
    }

    @Test
    void testGetBookingsByPassenger() {
        String passenger = unique("passenger");
        Booking booking = new Booking(unique("ride"), passenger);
        dao.saveBooking(booking);

        List<Booking> bookings = dao.getBookingsByPassenger(passenger);

        assertEquals(1, bookings.size());
        assertEquals(booking.getId(), bookings.get(0).getId());
    }

    @Test
    void testUpdateBookingPersistsStateChange() {
        Booking booking = new Booking(unique("ride"), unique("passenger"));
        dao.saveBooking(booking);

        booking.confirm();
        dao.updateBooking(booking);

        Booking reloaded = dao.getBookingById(booking.getId());
        assertEquals(Booking.CONFIRMED, reloaded.getState());
    }
}
