package com.ispw.uniride.boundary;

import com.ispw.uniride.bean.RideBean;
import com.ispw.uniride.controller.SearchRideController;

import java.util.List;
import java.util.Scanner;

/**
 * Boundary testuale del caso d'uso "Cerca Passaggio" (equivalente CLI di
 * {@link SearchRideBoundary}): stessa ricerca e stessa richiesta di prenotazione, ma con
 * l'elenco risultati numerato e la selezione fatta digitando un numero invece di un click.
 */
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

        // Stampa numerata (1-based, non 0-based) perché più naturale da digitare per l'utente
        // rispetto a un indice che parte da zero.
        System.out.println("\nPassaggi disponibili:");
        for (int i = 0; i < rides.size(); i++) {
            RideBean r = rides.get(i);
            System.out.printf("%d. [%s] Da: %s A: %s | Data: %s | Guidatore: %s | Posti: %d | Costo Stimato Attuale: %.2f€%n",
                    (i + 1), r.getId(), r.getDeparture(), r.getDestination(), r.getDate(),
                    r.getDriver(), r.getAvailableSeats(), r.getComputedPrice());
        }

        System.out.print("\nInserisci il numero del passaggio per prenotare (0 per annullare): ");
        int choice = scanner.nextInt();
        // 0 (o qualunque valore fuori range) annulla silenziosamente: nessuna richiesta viene
        // inviata, e il metodo termina senza ulteriori messaggi.
        if (choice > 0 && choice <= rides.size()) {
            // choice è 1-based (vedi stampa sopra), la lista è 0-based: -1 per convertire.
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
