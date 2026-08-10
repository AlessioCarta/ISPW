package com.ispw.uniride.boundary;

import com.ispw.uniride.bean.RideBean;
import com.ispw.uniride.controller.OfferRideController;

import java.util.Scanner;

/**
 * Boundary testuale del caso d'uso "Offri Passaggio" (equivalente CLI di
 * {@link OfferRideBoundary}): raccoglie gli stessi dati del form grafico da riga di comando e
 * li inoltra, tramite lo stesso {@link RideBean}, allo stesso {@link OfferRideController}.
 */
public class OfferRideCLI {
    private OfferRideController offerRideController = new OfferRideController();

    public void start() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n--- Offri un Passaggio ---");
        System.out.print("Partenza: ");
        String departure = scanner.nextLine();
        System.out.print("Destinazione: ");
        String destination = scanner.nextLine();
        System.out.print("Data (gg/mm/aaaa): ");
        String date = scanner.nextLine();
        System.out.print("Posti Totali: ");
        int totalSeats = scanner.nextInt();
        System.out.print("Costo Base Stimato (Carburante+Pedaggio): ");
        double basePrice = scanner.nextDouble();

        // Stesso Bean, stesso Controller Applicativo usato dalla Boundary grafica: nessuna
        // regola di business viene ripetuta o reinterpretata qui.
        RideBean bean = new RideBean(departure, destination, date, totalSeats, basePrice);

        try {
            offerRideController.offerRide(bean);
            System.out.println("Passaggio offerto con successo!");
        } catch (Exception e) {
            // Le eccezioni di validazione (campi obbligatori, posti/costo non validi) risalite
            // dal Controller Applicativo vengono stampate su stderr invece che su stdout.
            System.err.println("Errore: " + e.getMessage());
        }
    }
}
