package com.ispw.uniride.model.state;

import com.ispw.uniride.model.Ride;

/**
 * Stato Concreto (Pattern State) indicante un passaggio già concluso (viaggio avvenuto).
 * È uno stato terminale: nessuna nuova prenotazione, cancellazione di posto o ulteriore
 * transizione di stato è più ammessa.
 */
public class CompletedState implements RideState {

    @Override
    public boolean bookSeat(Ride ride) {
        return false; // Il viaggio è già concluso: non si può più prenotare.
    }

    @Override
    public boolean cancelSeat(Ride ride) {
        return false; // Non ha più senso liberare un posto su un viaggio concluso.
    }

    @Override
    public String getStatus() {
        return "COMPLETED";
    }

    @Override
    public boolean complete(Ride ride) {
        return false; // Già completato: transizione non ripetibile.
    }

    @Override
    public boolean cancel(Ride ride) {
        return false; // Un viaggio già concluso non può più essere annullato.
    }
}
