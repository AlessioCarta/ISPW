package com.ispw.uniride.model.state;

import com.ispw.uniride.model.Ride;

/**
 * Stato Concreto (Pattern State) indicante un passaggio annullato dal guidatore.
 * È uno stato terminale: nessuna nuova prenotazione, cancellazione di posto o ulteriore
 * transizione di stato è più ammessa.
 */
public class CancelledState implements RideState {

    @Override
    public boolean bookSeat(Ride ride) {
        return false; // Il viaggio è stato annullato: non si può più prenotare.
    }

    @Override
    public boolean cancelSeat(Ride ride) {
        return false; // Le prenotazioni sono già state chiuse in blocco dall'annullamento.
    }

    @Override
    public String getStatus() {
        return "CANCELLED";
    }

    @Override
    public boolean complete(Ride ride) {
        return false; // Un viaggio annullato non può essere marcato come concluso.
    }

    @Override
    public boolean cancel(Ride ride) {
        return false; // Già annullato: transizione non ripetibile.
    }
}
