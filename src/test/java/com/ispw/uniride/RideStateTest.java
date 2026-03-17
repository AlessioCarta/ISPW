package com.ispw.uniride;

import com.ispw.uniride.model.Ride;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RideStateTest {

    @Test
    void testInitialStateIsAvailable() {
        Ride ride = new Ride("driver1", "Rome", "Milan", "2023-10-10", 3, 50.0);
        assertEquals("AVAILABLE", ride.getStatus(), "Ride should initially be AVAILABLE");
        assertEquals(3, ride.getAvailableSeats());
    }

    @Test
    void testStateTransitionsToFull() {
        Ride ride = new Ride("driver1", "Rome", "Milan", "2023-10-10", 2, 50.0);

        // Book 1st seat
        assertTrue(ride.bookSeat(message -> {}));
        assertEquals("AVAILABLE", ride.getStatus());
        assertEquals(1, ride.getAvailableSeats());

        // Book 2nd seat
        assertTrue(ride.bookSeat(message -> {}));
        assertEquals("FULL", ride.getStatus(), "Ride state should transition to FULL when seats run out");
        assertEquals(0, ride.getAvailableSeats());

        // Try to book 3rd seat
        assertFalse(ride.bookSeat(message -> {}), "Should not allow booking when FULL");
    }

    @Test
    void testCancelSeatRestoresState() {
        Ride ride = new Ride("driver1", "Rome", "Milan", "2023-10-10", 1, 50.0);

        // Book the only seat
        ride.bookSeat(message -> {});
        assertEquals("FULL", ride.getStatus());

        // Cancel the seat
        assertTrue(ride.getState().cancelSeat(ride), "Canceling a seat should succeed");
        assertEquals("AVAILABLE", ride.getStatus(), "State should transition back to AVAILABLE");
        assertEquals(1, ride.getAvailableSeats());
    }
}
