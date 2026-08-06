package com.ispw.uniride;

import com.ispw.uniride.model.Booking;
import com.ispw.uniride.model.Ride;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite di Unit Testing (JUnit 5) per il workflow di prenotazione richiesta -> conferma/rifiuto:
 * verifica sia le transizioni dell'entità {@code Booking} sia la loro combinazione con il
 * Pattern State di {@code Ride} (riserva del posto alla richiesta, rilascio sul rifiuto).
 */
class BookingWorkflowTest {

    /**
     * Una nuova richiesta parte sempre in stato REQUESTED, indipendentemente dal fatto che il
     * posto sull'auto sia già stato fisicamente riservato da `Ride.bookSeat`.
     */
    @Test
    void testNewBookingStartsAsRequested() {
        Booking booking = new Booking("ride1", "passenger1");

        assertEquals(Booking.REQUESTED, booking.getState());
    }

    /**
     * Il guidatore conferma una richiesta pendente: la transizione REQUESTED -> CONFIRMED
     * deve avere successo e il posto, già riservato, resta tale (nessun cambio su Ride).
     */
    @Test
    void testDriverConfirmsPendingRequest() {
        Ride ride = new Ride("driver1", "Roma", "Milano", "2023-10-10", 2, 50.0);
        assertTrue(ride.bookSeat(message -> {}, "passenger1"), "Il posto deve riservarsi subito alla richiesta");
        Booking booking = new Booking(ride.getId(), "passenger1");

        assertTrue(booking.confirm(), "La conferma da REQUESTED deve avere successo");
        assertEquals(Booking.CONFIRMED, booking.getState());
        assertEquals(1, ride.getAvailableSeats(), "Il posto resta riservato dopo la conferma");
    }

    /**
     * Il guidatore rifiuta una richiesta pendente: la transizione REQUESTED -> REJECTED ha
     * successo, e il chiamante (il Controller applicativo) è responsabile di liberare il posto
     * su Ride tramite cancelSeat, qui simulato esplicitamente per verificarne l'effetto combinato.
     */
    @Test
    void testDriverRejectsPendingRequestFreesSeat() {
        Ride ride = new Ride("driver1", "Roma", "Milano", "2023-10-10", 1, 50.0);
        assertTrue(ride.bookSeat(message -> {}, "passenger1"));
        assertEquals("FULL", ride.getStatus(), "Un solo posto totale: la prenotazione lo esaurisce subito");

        Booking booking = new Booking(ride.getId(), "passenger1");
        assertTrue(booking.reject());
        assertEquals(Booking.REJECTED, booking.getState());

        // Il Controller libera il posto solo dopo un rifiuto riuscito.
        assertTrue(ride.cancelSeat("passenger1"));
        assertEquals("AVAILABLE", ride.getStatus(), "Il rifiuto deve restituire il posto liberato");
        assertEquals(1, ride.getAvailableSeats());
    }

    /**
     * Una richiesta già gestita (confermata o rifiutata) non può essere confermata o rifiutata
     * una seconda volta: evita che due click concorrenti del guidatore producano stati incoerenti.
     */
    @Test
    void testCannotConfirmOrRejectAlreadyHandledBooking() {
        Booking confirmed = new Booking("ride1", "passenger1");
        assertTrue(confirmed.confirm());
        assertFalse(confirmed.confirm(), "Non si può confermare due volte");
        assertFalse(confirmed.reject(), "Non si può rifiutare una richiesta già confermata");

        Booking rejected = new Booking("ride1", "passenger2");
        assertTrue(rejected.reject());
        assertFalse(rejected.reject(), "Non si può rifiutare due volte");
        assertFalse(rejected.confirm(), "Non si può confermare una richiesta già rifiutata");
    }

    /**
     * Il passeggero può annullare sia una richiesta ancora pendente sia una già confermata,
     * ma non una richiesta già chiusa (rifiutata o già annullata in precedenza).
     */
    @Test
    void testPassengerCancelsOwnBooking() {
        Booking pending = new Booking("ride1", "passenger1");
        assertTrue(pending.cancel(), "Si può annullare una richiesta ancora in attesa");
        assertEquals(Booking.CANCELLED, pending.getState());
        assertFalse(pending.cancel(), "Non si può annullare due volte");

        Booking confirmed = new Booking("ride1", "passenger2");
        confirmed.confirm();
        assertTrue(confirmed.cancel(), "Si può annullare anche una prenotazione già confermata");
    }
}
