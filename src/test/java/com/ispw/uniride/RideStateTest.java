package com.ispw.uniride;

import com.ispw.uniride.model.Ride;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite di Unit Testing (JUnit 5) per convalidare il core business.
 * Verifica la correttezza del Design Pattern State applicato alla classe `Ride`.
 * @author Alessio Carta
 */
class RideStateTest {

    /**
     * Controlla che una vettura instanziata con più di zero posti venga
     * automaticamente assegnata allo stato "Available" (disponibile).
     */
    @Test
    void testInitialStateIsAvailable() {
        Ride ride = new Ride("driver1", "Rome", "Milan", "2023-10-10", 3, 50.0);

        // Assert d'uguaglianza tra lo stato restituito e la stringa d'attesa
        assertEquals("AVAILABLE", ride.getStatus(), "La macchina ha posti, dovrebbe essere in stato AVAILABLE");
        assertEquals(3, ride.getAvailableSeats());
    }

    /**
     * Valuta dinamicamente il cambio di stato a runtime (la mutazione strutturale)
     * all'esaurimento dei posti fisici dell'auto.
     */
    @Test
    void testStateTransitionsToFull() {
        Ride ride = new Ride("driver1", "Rome", "Milan", "2023-10-10", 2, 50.0);

        // Prima prenotazione: i posti scendono ma lo stato non cambia.
        assertTrue(ride.bookSeat(message -> {}, "pass1"));
        assertEquals("AVAILABLE", ride.getStatus());
        assertEquals(1, ride.getAvailableSeats());

        // Seconda prenotazione (Ultimo posto): triggera la transizione
        assertTrue(ride.bookSeat(message -> {}, "pass2"));
        assertEquals("FULL", ride.getStatus(), "La macchina deve cambiare stato in FULL finiti i posti");
        assertEquals(0, ride.getAvailableSeats());

        // Tentativo illegale in condizione di pienone.
        // L'oggetto State non permetterà il side-effect negativo (underflow).
        assertFalse(ride.bookSeat(message -> {}, "pass3"), "Non è permesso prenotare su un veicolo nello stato FULL");
    }

    /**
     * Verifica la solidità dell'operazione inversa, ovvero il ripristino di stato
     * al momento della cancellazione di un booking da una macchina colma.
     */
    @Test
    void testCancelSeatRestoresState() {
        Ride ride = new Ride("driver1", "Rome", "Milan", "2023-10-10", 1, 50.0);

        // Macchina piena forzata con una sola prenotazione
        ride.bookSeat(message -> {}, "pass1");
        assertEquals("FULL", ride.getStatus());

        // Simula lo sganciamento del passeggero e la liberazione del seggiolino
        assertTrue(ride.getState().cancelSeat(ride), "Cancellare una seduta valida deve tornare successo");
        assertEquals("AVAILABLE", ride.getStatus(), "Una cancellazione da auto FULL deve rigenerare l'AvailableState");
        assertEquals(1, ride.getAvailableSeats());
    }
}
