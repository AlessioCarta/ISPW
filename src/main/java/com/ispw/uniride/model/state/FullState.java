package com.ispw.uniride.model.state;

import com.ispw.uniride.model.Ride;

/**
 * Stato Concreto indicante il riempimento di un passaggio auto nel Pattern State.
 * I posti disponibili sono pari a zero, e quindi questo stato non consente nuove prenotazioni.
 */
public class FullState implements RideState {

    /**
     * Respinge i tentativi concorrenti di prenotazione: i posti fisici del veicolo sono finiti.
     * Non altera lo stato dell'entità Ride.
     */
    @Override
    public boolean bookSeat(Ride ride) {
        return false; // Prenotazione fallita sistematicamente, macchina piena
    }

    /**
     * La cancellazione di un posto quando la macchina è "Full" provoca un'immediata
     * rigenerazione dello stato verso l'alto ("AvailableState").
     */
    @Override
    public boolean cancelSeat(Ride ride) {
        if (ride.getAvailableSeats() < ride.getTotalSeats()) {
            // Un posto si libera
            ride.setAvailableSeats(ride.getAvailableSeats() + 1);

            // Ripristino/transizione di stato
            ride.setState(new AvailableState());
            return true;
        }
        return false; // Evita underflow logico
    }

    /**
     * @return Label human-readable
     */
    @Override
    public String getStatus() {
        return "FULL";
    }
}
