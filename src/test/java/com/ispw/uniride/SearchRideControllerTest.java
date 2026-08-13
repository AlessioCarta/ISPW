package com.ispw.uniride;

import com.ispw.uniride.bean.RideBean;
import com.ispw.uniride.controller.OfferRideController;
import com.ispw.uniride.controller.SearchRideController;
import com.ispw.uniride.controller.Session;
import com.ispw.uniride.exceptions.RideActionException;
import com.ispw.uniride.exceptions.RideNotFoundException;
import com.ispw.uniride.exceptions.UserNotAuthorizedException;
import com.ispw.uniride.model.Student;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite di Unit Testing (JUnit 5) per {@code SearchRideController}: ricerca dei passaggi
 * disponibili e richiesta di prenotazione (che riserva subito il posto senza confermarlo).
 * @author Alessio Carta
 */
class SearchRideControllerTest {

    private final OfferRideController offerRideController = new OfferRideController();
    private final SearchRideController searchRideController = new SearchRideController();

    private String unique(String prefix) {
        return prefix + "." + System.nanoTime();
    }

    private void loginAs(String username) {
        Session.getInstance().setLoggedUser(new Student(username, "[HASHED]password", "Utente " + username));
    }

    @AfterEach
    void tearDown() {
        Session.getInstance().logout();
    }

    private RideBean offerUniqueRide(String driverUsername) throws UserNotAuthorizedException, RideActionException {
        String departure = unique("Partenza");
        String destination = unique("Destinazione");
        loginAs(driverUsername);
        offerRideController.offerRide(new RideBean(departure, destination, "10/12/2026", "08:30", 2, 20.0));

        List<RideBean> results = searchRideController.searchRides(departure, destination);
        assertEquals(1, results.size(), "La ricerca deve trovare esattamente la corsa appena offerta");
        return results.get(0);
    }

    /**
     * Una corsa appena offerta deve comparire nella ricerca per la stessa tratta esatta.
     */
    @Test
    void testSearchRidesFindsOfferedRide() throws UserNotAuthorizedException, RideActionException {
        RideBean ride = offerUniqueRide(unique("driver"));
        assertNotNull(ride.getId());
    }

    /**
     * Una richiesta di prenotazione valida deve riuscire e riservare subito il posto.
     */
    @Test
    void testBookRideSuccessReservesSeat() throws Exception {
        RideBean ride = offerUniqueRide(unique("driver"));

        loginAs(unique("passenger"));
        boolean success = searchRideController.bookRide(ride.getId());

        assertTrue(success, "La richiesta di prenotazione deve riuscire se ci sono posti liberi");
    }

    /**
     * Il conducente non può prenotare un posto sul proprio stesso passaggio.
     */
    @Test
    void testBookOwnRideFails() throws Exception {
        String driver = unique("driver");
        RideBean ride = offerUniqueRide(driver);

        loginAs(driver);
        RideActionException ex = assertThrows(RideActionException.class, () -> searchRideController.bookRide(ride.getId()));
        assertEquals("Non puoi prenotare un posto sul passaggio che hai offerto tu stesso.", ex.getMessage());
    }

    /**
     * Una seconda richiesta sulla stessa corsa da parte dello stesso passeggero deve essere rifiutata.
     */
    @Test
    void testDuplicateBookingRequestFails() throws Exception {
        RideBean ride = offerUniqueRide(unique("driver"));

        loginAs(unique("passenger"));
        assertTrue(searchRideController.bookRide(ride.getId()));

        RideActionException ex = assertThrows(RideActionException.class, () -> searchRideController.bookRide(ride.getId()));
        assertEquals("Hai già una richiesta attiva per questa corsa.", ex.getMessage());
    }

    /**
     * Una richiesta su un id inesistente deve segnalare l'assenza del passaggio.
     */
    @Test
    void testBookNonExistentRideFails() {
        loginAs(unique("passenger"));
        assertThrows(RideNotFoundException.class, () -> searchRideController.bookRide("id-inesistente"));
    }

    /**
     * Senza un utente loggato la richiesta di prenotazione deve essere rifiutata.
     */
    @Test
    void testBookRideWithoutLoginFails() {
        Session.getInstance().logout();
        assertThrows(UserNotAuthorizedException.class, () -> searchRideController.bookRide("qualsiasi-id"));
    }
}
