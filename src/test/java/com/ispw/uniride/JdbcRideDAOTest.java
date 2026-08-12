package com.ispw.uniride;

import com.ispw.uniride.dao.RideDAO;
import com.ispw.uniride.dao.sql.JdbcRideDAO;
import com.ispw.uniride.model.Ride;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite di Unit Testing (JUnit 5) per {@code JdbcRideDAO}: persistenza su database relazionale
 * reale (H2 embedded). Il database su file è condiviso fra tutti i test della JVM, quindi ogni
 * test usa identificativi univoci invece di assumere che le tabelle siano vuote.
 * @author Alessio Carta
 */
class JdbcRideDAOTest {

    private final RideDAO dao = new JdbcRideDAO();

    private String unique(String prefix) {
        return prefix + "." + System.nanoTime();
    }

    @Test
    void testSaveAndGetRideById() {
        Ride ride = new Ride(unique("driver"), unique("Da"), unique("A"), "01/01/2027", 3, 15.0);
        dao.saveRide(ride);

        Ride found = dao.getRideById(ride.getId());

        assertNotNull(found);
        assertEquals(ride.getId(), found.getId());
        assertEquals(ride.getDeparture(), found.getDeparture());
        assertEquals(3, found.getTotalSeats());
    }

    @Test
    void testGetAvailableRidesFiltersByRouteAndStatus() {
        String departure = unique("Partenza");
        String destination = unique("Destinazione");
        Ride ride = new Ride(unique("driver"), departure, destination, "01/01/2027", 2, 10.0);
        dao.saveRide(ride);

        List<Ride> results = dao.getAvailableRides(departure, destination);

        assertEquals(1, results.size());
        assertEquals(ride.getId(), results.get(0).getId());
    }

    @Test
    void testUpdateRidePersistsStateAndPassengers() {
        Ride ride = new Ride(unique("driver"), unique("Da"), unique("A"), "01/01/2027", 1, 10.0);
        dao.saveRide(ride);

        ride.bookSeat(message -> {}, "passenger1");
        dao.updateRide(ride);

        Ride reloaded = dao.getRideById(ride.getId());
        assertEquals("FULL", reloaded.getStatus());
        assertTrue(reloaded.getPassengerUsernames().contains("passenger1"));
    }

    @Test
    void testGetRidesByDriverAndByPassenger() {
        String driver = unique("driver");
        String passenger = unique("passenger");
        Ride ride = new Ride(driver, unique("Da"), unique("A"), "01/01/2027", 2, 10.0);
        ride.bookSeat(message -> {}, passenger);
        dao.saveRide(ride);

        assertTrue(dao.getRidesByDriver(driver).stream().anyMatch(r -> r.getId().equals(ride.getId())));
        assertTrue(dao.getRidesByPassenger(passenger).stream().anyMatch(r -> r.getId().equals(ride.getId())));
    }

    @Test
    void testDeleteRideRemovesItAndItsPassengers() {
        Ride ride = new Ride(unique("driver"), unique("Da"), unique("A"), "01/01/2027", 2, 10.0);
        ride.bookSeat(message -> {}, unique("passenger"));
        dao.saveRide(ride);

        dao.deleteRide(ride.getId());

        assertNull(dao.getRideById(ride.getId()));
    }
}
