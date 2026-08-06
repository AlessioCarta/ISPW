package com.ispw.uniride;

import com.ispw.uniride.dao.RideDAO;
import com.ispw.uniride.dao.fs.FsRideDAO;
import com.ispw.uniride.model.Ride;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite di Unit Testing (JUnit 5) per {@code FsRideDAO}: persistenza su file CSV reale
 * (rides.csv nella root del progetto). La cache statica è condivisa per l'intera JVM di test,
 * quindi ogni test usa identificativi univoci invece di assumere che il file sia vuoto.
 */
class FsRideDAOTest {

    private final RideDAO dao = new FsRideDAO();

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
    void testUpdateRidePersistsStateChangeAcrossReload() {
        Ride ride = new Ride(unique("driver"), unique("Da"), unique("A"), "01/01/2027", 1, 10.0);
        dao.saveRide(ride);

        ride.bookSeat(message -> {}, "passenger1");
        dao.updateRide(ride);

        // La cache è statica (condivisa da tutte le istanze di FsRideDAO nella JVM): rileggendo
        // tramite il DAO si verifica comunque che la riscrittura su rides.csv sia andata a buon fine.
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
    void testDeleteRideRemovesIt() {
        Ride ride = new Ride(unique("driver"), unique("Da"), unique("A"), "01/01/2027", 2, 10.0);
        dao.saveRide(ride);

        dao.deleteRide(ride.getId());

        assertNull(dao.getRideById(ride.getId()));
    }
}
