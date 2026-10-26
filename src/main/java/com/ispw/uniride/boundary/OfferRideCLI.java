package com.ispw.uniride.boundary;

import com.ispw.uniride.bean.RideBean;
import com.ispw.uniride.controller.OfferRideController;

/**
 * Boundary testuale del caso d'uso "Offri Passaggio" (equivalente CLI di
 * {@link OfferRideBoundary}): compila lo stesso Bean e invoca lo stesso Controller Applicativo.
 */
public class OfferRideCLI {
    private final OfferRideController offerRideController = new OfferRideController();

    public void start() {
        System.out.println("\n--- Offri un Passaggio ---");
        try {
            // Stesso Bean, stesso Controller Applicativo usato dalla Boundary grafica: i setter
            // del Bean controllano il formato di ogni campo appena viene letto.
            RideBean bean = new RideBean();
            bean.setDeparture(InputCLI.readLine("Partenza: "));
            bean.setDestination(InputCLI.readLine("Destinazione: "));
            bean.setDate(InputCLI.readLine("Data (gg/mm/aaaa): "));
            bean.setDepartureTime(InputCLI.readLine("Orario di partenza (HH:mm): "));
            bean.setTotalSeats(InputCLI.readLine("Posti Totali: "));
            bean.setBasePrice(InputCLI.readLine("Costo Base Stimato (Carburante+Pedaggio): "));

            offerRideController.offerRide(bean);
            System.out.println("Passaggio offerto con successo!");
        } catch (Exception e) {
            // Errori di formato (dal Bean) e di dominio (dal Controller Applicativo) vengono
            // tradotti in un messaggio per l'utente, come fa la controparte grafica.
            System.out.println("Errore: " + ErrorMessages.forUser(e));
        }
    }
}
