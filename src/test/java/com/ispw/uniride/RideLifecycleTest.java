package com.ispw.uniride;

import com.ispw.uniride.model.Ride;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite di Unit Testing (JUnit 5) per le transizioni di fine vita di un {@code Ride}:
 * completamento e annullamento (Pattern State, stati {@code CompletedState}/{@code CancelledState}).
 * @author Alessio Carta
 */
class RideLifecycleTest {

    /**
     * Da AVAILABLE il guidatore può sempre segnare il viaggio come concluso.
     */
    @Test
    void testCompleteFromAvailable() {
        Ride ride = new Ride("driver1", "Roma", "Milano", "2023-10-10", 3, 50.0);

        assertTrue(ride.complete(), "Il completamento da AVAILABLE deve avere successo");
        assertEquals("COMPLETED", ride.getStatus());
    }

    /**
     * Anche a pieno carico (FULL) il guidatore può segnare il viaggio come concluso.
     */
    @Test
    void testCompleteFromFull() {
        Ride ride = new Ride("driver1", "Roma", "Milano", "2023-10-10", 1, 50.0);
        ride.bookSeat(message -> {}, "passenger1");
        assertEquals("FULL", ride.getStatus());

        assertTrue(ride.complete());
        assertEquals("COMPLETED", ride.getStatus());
    }

    /**
     * Uno stato terminale non ammette ulteriori transizioni: un viaggio già concluso non può
     * essere ri-completato né annullato, e non accetta più prenotazioni o cancellazioni di posto.
     */
    @Test
    void testCompletedRideIsTerminal() {
        Ride ride = new Ride("driver1", "Roma", "Milano", "2023-10-10", 2, 50.0);
        ride.complete();

        assertFalse(ride.complete(), "Non si può completare due volte");
        assertFalse(ride.cancel(), "Non si può annullare un viaggio già concluso");
        assertFalse(ride.bookSeat(message -> {}, "passenger1"), "Nessuna nuova prenotazione su un viaggio concluso");
    }

    /**
     * Il guidatore può annullare un viaggio anche se ci sono già passeggeri prenotati
     * (a differenza della vecchia logica di eliminazione fisica del record).
     */
    @Test
    void testCancelWithPassengersSucceedsAndNotifies() {
        Ride ride = new Ride("driver1", "Roma", "Milano", "2023-10-10", 2, 50.0);
        AtomicBoolean notified = new AtomicBoolean(false);
        ride.bookSeat(message -> notified.set(true), "passenger1");

        assertTrue(ride.cancel(), "L'annullamento deve avere successo anche con passeggeri a bordo");
        assertEquals("CANCELLED", ride.getStatus());
        assertTrue(notified.get(), "I passeggeri a bordo devono essere notificati dell'annullamento");
    }

    /**
     * Uno stato CANCELLED è terminale al pari di COMPLETED.
     */
    @Test
    void testCancelledRideIsTerminal() {
        Ride ride = new Ride("driver1", "Roma", "Milano", "2023-10-10", 2, 50.0);
        ride.cancel();

        assertFalse(ride.cancel(), "Non si può annullare due volte");
        assertFalse(ride.complete(), "Non si può completare un viaggio già annullato");
        assertFalse(ride.bookSeat(message -> {}, "passenger1"), "Nessuna nuova prenotazione su un viaggio annullato");
    }
}
