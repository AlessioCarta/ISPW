package com.ispw.uniride.model.state;

import com.ispw.uniride.model.Ride;

/**
 * Stato Concreto (Pattern State) indicante un veicolo che ha ancora 1 o più slot di seduta vuoti.
 * Fornisce un'implementazione di booking attiva.
 */
public class AvailableState implements RideState {

    /**
     * Essendo disponibile, accetta la richiesta di prenotazione: diminuisce il counter posti,
     * e se raggiungiamo lo zero innesca autonomamente la transizione verso il 'FullState'.
     */
    @Override
    public boolean bookSeat(Ride ride) {
        if (ride.getAvailableSeats() > 0) {
            ride.setAvailableSeats(ride.getAvailableSeats() - 1);

            // Transizione stato interno
            if (ride.getAvailableSeats() == 0) {
                ride.setState(new FullState());
            }
            return true; // Prenotazione andata a buon fine
        }
        return false; // Operazione invalida o concorrenza andata male
    }

    /**
     * Se è "Available" ma sono comunque presenti prenotazioni attive (< TotalSeats),
     * consente ad un passeggero di rinunciare al suo posto per ridarlo alla coda.
     */
    @Override
    public boolean cancelSeat(Ride ride) {
        if (ride.getAvailableSeats() < ride.getTotalSeats()) {
            ride.setAvailableSeats(ride.getAvailableSeats() + 1);
            return true; // Annullo completato
        }
        return false; // Non ci sono prenotazioni pregresse (auto completamente vuota)
    }

    /**
     * @return Label human-readable in inglese utilizzata nei bean e interfacce FXML
     */
    @Override
    public String getStatus() {
        return "AVAILABLE";
    }
}
