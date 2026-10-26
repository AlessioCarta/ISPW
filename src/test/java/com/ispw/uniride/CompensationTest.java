package com.ispw.uniride;

import com.ispw.uniride.controller.OfferRideController;
import com.ispw.uniride.controller.SearchRideController;
import com.ispw.uniride.controller.Session;
import com.ispw.uniride.dao.BookingDAO;
import com.ispw.uniride.dao.DAOFactory;
import com.ispw.uniride.dao.RideDAO;
import com.ispw.uniride.dao.StudentDAO;
import com.ispw.uniride.dao.memory.MemoryBookingDAO;
import com.ispw.uniride.dao.memory.MemoryDAOFactory;
import com.ispw.uniride.exceptions.PersistenceException;
import com.ispw.uniride.model.Booking;
import com.ispw.uniride.model.Ride;
import com.ispw.uniride.model.Student;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite di Unit Testing (JUnit 5) per le azioni compensative dei Controller: quando la seconda
 * di due scritture (corsa, poi prenotazione) fallisce, la prima deve essere annullata.
 * @author Alessio Carta
 */
class CompensationTest {

    private static final String FUTURE_DATE = LocalDate.now().plusMonths(1).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

    /** Factory Memory reale, tranne il DAO delle prenotazioni che fallisce sempre in scrittura. */
    private static final class FailingBookingFactory extends DAOFactory {
        private final MemoryDAOFactory delegate = new MemoryDAOFactory();

        @Override
        public RideDAO getRideDAO() { return delegate.getRideDAO(); }

        @Override
        public StudentDAO getStudentDAO() { return delegate.getStudentDAO(); }

        @Override
        public BookingDAO getBookingDAO() {
            return new MemoryBookingDAO() {
                @Override
                public void saveBooking(Booking booking) {
                    throw new PersistenceException("Guasto simulato", null);
                }
            };
        }
    }

    @AfterEach
    void tearDown() {
        Session.getInstance().logout();
        DAOFactory.setInstance(null);
    }

    @Test
    void testFailedBookingSaveFreesTheReservedSeat() throws Exception {
        String departure = "Partenza." + System.nanoTime();
        String destination = "Destinazione." + System.nanoTime();
        Session.getInstance().setLoggedUser(new Student("driver." + System.nanoTime(), "x", "Guidatore"));
        new OfferRideController().offerRide(TestBeans.ride(departure, destination, FUTURE_DATE, "08:30", 2, 10.0));
        String rideId = new SearchRideController().searchRides(departure, destination).get(0).getId();

        DAOFactory.setInstance(new FailingBookingFactory());
        String passenger = "passenger." + System.nanoTime();
        Session.getInstance().setLoggedUser(new Student(passenger, "x", "Passeggero"));

        SearchRideController controller = new SearchRideController();
        assertThrows(PersistenceException.class, () -> controller.bookRide(rideId));

        Ride ride = DAOFactory.getInstance().getRideDAO().getRideById(rideId);
        assertEquals(2, ride.getAvailableSeats());
        assertFalse(ride.getPassengerUsernames().contains(passenger));
    }
}
