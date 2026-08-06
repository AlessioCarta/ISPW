package com.ispw.uniride;

import com.ispw.uniride.bean.BookingBean;
import com.ispw.uniride.bean.RideBean;
import com.ispw.uniride.controller.ManageRidesController;
import com.ispw.uniride.controller.OfferRideController;
import com.ispw.uniride.controller.SearchRideController;
import com.ispw.uniride.controller.Session;
import com.ispw.uniride.exceptions.RideActionException;
import com.ispw.uniride.exceptions.UserNotAuthorizedException;
import com.ispw.uniride.model.Student;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite di Unit Testing (JUnit 5) per {@code ManageRidesController}: workflow di conferma/rifiuto
 * delle richieste, annullamento/completamento di una corsa offerta e annullamento di una
 * prenotazione da parte del passeggero.
 */
class ManageRidesControllerTest {

    private final OfferRideController offerRideController = new OfferRideController();
    private final SearchRideController searchRideController = new SearchRideController();
    private final ManageRidesController manageRidesController = new ManageRidesController();

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

    /**
     * Scenario di supporto comune: un guidatore offre una corsa e un passeggero vi richiede un posto.
     * @return l'id della corsa e il fx:id del guidatore/passeggero usati, tramite un piccolo record locale.
     */
    private record Scenario(String driver, String passenger, String rideId) {}

    private Scenario offerAndRequestBooking() throws Exception {
        String driver = unique("driver");
        String passenger = unique("passenger");
        String departure = unique("Partenza");
        String destination = unique("Destinazione");

        loginAs(driver);
        offerRideController.offerRide(new RideBean(departure, destination, "10/12/2026", 2, 20.0));
        List<RideBean> found = searchRideController.searchRides(departure, destination);
        String rideId = found.get(0).getId();

        loginAs(passenger);
        assertTrue(searchRideController.bookRide(rideId));

        return new Scenario(driver, passenger, rideId);
    }

    /**
     * Le corse offerte devono comparire per il guidatore, e la richiesta pendente per quella corsa
     * deve essere visibile e riconducibile al passeggero corretto.
     */
    @Test
    void testOfferedRidesAndPendingRequestsVisibleToDriver() throws Exception {
        Scenario s = offerAndRequestBooking();

        loginAs(s.driver());
        List<RideBean> offered = manageRidesController.getMyOfferedRides();
        assertTrue(offered.stream().anyMatch(r -> r.getId().equals(s.rideId())));

        List<BookingBean> pending = manageRidesController.getPendingRequestsForRide(s.rideId());
        assertEquals(1, pending.size());
        assertEquals(s.passenger(), pending.get(0).getCounterpartName());
        assertEquals("REQUESTED", pending.get(0).getState());
    }

    /**
     * La corsa prenotata deve comparire tra le prenotazioni del passeggero con stato "REQUESTED"
     * finché il guidatore non la conferma.
     */
    @Test
    void testBookedRideVisibleToPassengerAsRequested() throws Exception {
        Scenario s = offerAndRequestBooking();

        loginAs(s.passenger());
        List<BookingBean> booked = manageRidesController.getMyBookedRides();
        assertEquals(1, booked.size());
        assertEquals(s.rideId(), booked.get(0).getRideId());
        assertEquals("REQUESTED", booked.get(0).getState());
    }

    /**
     * Il guidatore conferma una richiesta pendente: lo stato passa a CONFIRMED e il posto resta riservato.
     */
    @Test
    void testConfirmBookingSucceeds() throws Exception {
        Scenario s = offerAndRequestBooking();

        loginAs(s.driver());
        String bookingId = manageRidesController.getPendingRequestsForRide(s.rideId()).get(0).getBookingId();
        manageRidesController.confirmBooking(bookingId);

        loginAs(s.passenger());
        List<BookingBean> booked = manageRidesController.getMyBookedRides();
        assertEquals("CONFIRMED", booked.get(0).getState());
    }

    /**
     * Il guidatore rifiuta una richiesta pendente: il posto viene liberato e la prenotazione
     * non compare più tra quelle attive del passeggero.
     */
    @Test
    void testRejectBookingReleasesSeat() throws Exception {
        Scenario s = offerAndRequestBooking();

        loginAs(s.driver());
        String bookingId = manageRidesController.getPendingRequestsForRide(s.rideId()).get(0).getBookingId();
        manageRidesController.rejectBooking(bookingId);

        loginAs(s.passenger());
        assertTrue(manageRidesController.getMyBookedRides().isEmpty(), "Una richiesta rifiutata non deve più risultare attiva");
    }

    /**
     * Un utente diverso dal conducente non può confermare né rifiutare le richieste di quella corsa.
     */
    @Test
    void testOnlyDriverCanConfirmOrReject() throws Exception {
        Scenario s = offerAndRequestBooking();

        loginAs(s.driver());
        String bookingId = manageRidesController.getPendingRequestsForRide(s.rideId()).get(0).getBookingId();

        loginAs(unique("intruso"));
        assertThrows(UserNotAuthorizedException.class, () -> manageRidesController.confirmBooking(bookingId));
    }

    /**
     * Il passeggero annulla la propria richiesta: il posto torna disponibile.
     */
    @Test
    void testPassengerCancelsOwnBooking() throws Exception {
        Scenario s = offerAndRequestBooking();

        loginAs(s.passenger());
        manageRidesController.cancelBooking(s.rideId());

        assertTrue(manageRidesController.getMyBookedRides().isEmpty());
    }

    /**
     * Il guidatore può annullare la propria corsa offerta anche con un passeggero già prenotato:
     * a differenza della vecchia logica, l'operazione non viene più bloccata.
     */
    @Test
    void testCancelOfferedRideWorksEvenWithPassengers() throws Exception {
        Scenario s = offerAndRequestBooking();

        loginAs(s.driver());
        assertDoesNotThrow(() -> manageRidesController.cancelOfferedRide(s.rideId()));

        List<RideBean> offered = manageRidesController.getMyOfferedRides();
        RideBean cancelled = offered.stream().filter(r -> r.getId().equals(s.rideId())).findFirst().orElseThrow();
        assertEquals("CANCELLED", cancelled.getStatus());
    }

    /**
     * Una corsa già annullata non può essere annullata una seconda volta né completata.
     */
    @Test
    void testCancelOfferedRideTwiceFails() throws Exception {
        Scenario s = offerAndRequestBooking();

        loginAs(s.driver());
        manageRidesController.cancelOfferedRide(s.rideId());

        assertThrows(RideActionException.class, () -> manageRidesController.cancelOfferedRide(s.rideId()));
        assertThrows(RideActionException.class, () -> manageRidesController.completeRide(s.rideId()));
    }

    /**
     * Il guidatore può segnare una corsa come completata.
     */
    @Test
    void testCompleteRideSucceeds() throws Exception {
        Scenario s = offerAndRequestBooking();

        loginAs(s.driver());
        manageRidesController.completeRide(s.rideId());

        RideBean completed = manageRidesController.getMyOfferedRides().stream()
                .filter(r -> r.getId().equals(s.rideId())).findFirst().orElseThrow();
        assertEquals("COMPLETED", completed.getStatus());
    }
}
