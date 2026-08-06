package com.ispw.uniride.boundary;

import com.ispw.uniride.bean.RideBean;
import com.ispw.uniride.controller.SearchRideController;

import java.util.List;
import java.util.Scanner;

public class SearchRideCLI {
    private SearchRideController searchRideController = new SearchRideController();

    public void start() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n--- Cerca un Passaggio ---");
        System.out.print("Partenza: ");
        String departure = scanner.nextLine();
        System.out.print("Destinazione: ");
        String destination = scanner.nextLine();

        List<RideBean> rides = searchRideController.searchRides(departure, destination);

        if (rides.isEmpty()) {
            System.out.println("Nessun passaggio trovato.");
            return;
        }

        System.out.println("\nPassaggi disponibili:");
        for (int i = 0; i < rides.size(); i++) {
            RideBean r = rides.get(i);
            System.out.printf("%d. [%s] Da: %s A: %s | Data: %s | Guidatore: %s | Posti: %d | Costo Stimato Attuale: %.2f€\n",
                    (i + 1), r.getId(), r.getDeparture(), r.getDestination(), r.getDate(),
                    r.getDriver(), r.getAvailableSeats(), r.getComputedPrice());
        }

        System.out.print("\nInserisci il numero del passaggio per prenotare (0 per annullare): ");
        int choice = scanner.nextInt();
        if (choice > 0 && choice <= rides.size()) {
            RideBean selected = rides.get(choice - 1);
            try {
                boolean booked = searchRideController.bookRide(selected.getId());
                if (booked) {
                    System.out.println("Richiesta inviata! In attesa di conferma del guidatore.");
                } else {
                    System.out.println("Impossibile prenotare (posti esauriti).");
                }
            } catch (Exception e) {
                System.err.println("Errore: " + e.getMessage());
            }
        }
    }
}
