package com.ispw.uniride;

import com.ispw.uniride.dao.RideDAO;
import com.ispw.uniride.dao.memory.MemoryRideDAO;
import com.ispw.uniride.model.Ride;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite di Unit Testing (JUnit 5) per {@code MemoryRideDAO}: operazioni CRUD dirette sullo
 * storage in RAM condiviso. Ogni test usa partenza/destinazione/driver univoci per restare
 * indipendente dagli altri, dato che lo storage è uno Singleton condiviso per l'intera JVM.
 */
class MemoryRideDAOTest {

    private final RideDAO dao = new MemoryRideDAO();

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
    void testUpdateRidePersistsStateChange() {
        Ride ride = new Ride(unique("driver"), unique("Da"), unique("A"), "01/01/2027", 1, 10.0);
        dao.saveRide(ride);

        ride.bookSeat(message -> {}, "passenger1");
        dao.updateRide(ride);

        Ride reloaded = dao.getRideById(ride.getId());
        assertEquals("FULL", reloaded.getStatus());
    }

    @Test
    void testGetRidesByDriver() {
        String driver = unique("driver");
        Ride ride = new Ride(driver, unique("Da"), unique("A"), "01/01/2027", 2, 10.0);
        dao.saveRide(ride);

        List<Ride> rides = dao.getRidesByDriver(driver);

        assertTrue(rides.stream().anyMatch(r -> r.getId().equals(ride.getId())));
    }

    @Test
    void testGetRidesByPassenger() {
        Ride ride = new Ride(unique("driver"), unique("Da"), unique("A"), "01/01/2027", 2, 10.0);
        String passenger = unique("passenger");
        ride.bookSeat(message -> {}, passenger);
        dao.saveRide(ride);

        List<Ride> rides = dao.getRidesByPassenger(passenger);

        assertTrue(rides.stream().anyMatch(r -> r.getId().equals(ride.getId())));
    }

    @Test
    void testDeleteRideRemovesIt() {
        Ride ride = new Ride(unique("driver"), unique("Da"), unique("A"), "01/01/2027", 2, 10.0);
        dao.saveRide(ride);

        dao.deleteRide(ride.getId());

        assertNull(dao.getRideById(ride.getId()));
    }
}
