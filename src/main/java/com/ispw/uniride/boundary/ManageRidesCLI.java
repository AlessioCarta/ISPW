package com.ispw.uniride.boundary;

import com.ispw.uniride.bean.BookingBean;
import com.ispw.uniride.bean.RideBean;
import com.ispw.uniride.controller.ManageRidesController;

import java.util.List;
import java.util.Scanner;

/**
 * Boundary testuale del caso d'uso "Le Mie Corse" (equivalente CLI, ridotto, di
 * {@link ManageRidesBoundary}). Mostra le corse offerte e le prenotazioni attive, e permette
 * solo l'annullamento di una prenotazione come passeggero: la conferma/rifiuto delle richieste
 * pendenti come guidatore e l'annullamento/completamento di una corsa offerta restano
 * disponibili solo dalla Boundary grafica.
 */
public class ManageRidesCLI {
    private ManageRidesController controller = new ManageRidesController();

    public void start() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n--- Le Mie Corse ---");

        try {
            System.out.println("\n[OFFERTE - Come Guidatore]");
            List<RideBean> offered = controller.getMyOfferedRides();
            if (offered.isEmpty()) System.out.println("Nessuna corsa offerta.");
            else {
                for (RideBean r : offered) {
                    System.out.printf("ID: %s | %s -> %s (%s) [%s]%n", r.getId(), r.getDeparture(), r.getDestination(), r.getDate(), r.getStatus());
                }
            }

            System.out.println("\n[PRENOTAZIONI - Come Passeggero]");
            List<BookingBean> booked = controller.getMyBookedRides();
            if (booked.isEmpty()) System.out.println("Nessuna prenotazione attiva.");
            else {
                // Elenco numerato (1-based) per permettere all'utente di scegliere quale
                // prenotazione annullare digitando semplicemente il suo numero.
                for (int i = 0; i < booked.size(); i++) {
                    BookingBean b = booked.get(i);
                    System.out.printf("%d. [%s] Guidatore: %s | %s -> %s (%s) [%s]%n",
                            i+1, b.getRideId(), b.getCounterpartName(), b.getDeparture(), b.getDestination(), b.getDate(), b.getState());
                }
            }

            if (!booked.isEmpty()) {
                System.out.print("\nVuoi annullare una prenotazione? Inserisci il numero (0 per uscire): ");
                int choice = scanner.nextInt();
                scanner.nextLine(); // consume newline: nextInt() non legge il carattere \n finale.

                if (choice > 0 && choice <= booked.size()) {
                    BookingBean selected = booked.get(choice - 1);
                    controller.cancelBooking(selected.getRideId());
                    System.out.println("Prenotazione annullata con successo!");
                }
                // choice == 0 o fuori range: nessuna azione, si esce silenziosamente.
            } else {
                // Nessuna prenotazione da annullare: si attende solo un Invio prima di tornare
                // al menu precedente, per dare il tempo di leggere l'elenco appena stampato.
                System.out.println("\nPremi Invio per tornare alla Dashboard...");
                scanner.nextLine();
            }

        } catch (Exception e) {
            // Cattura eventuali eccezioni di dominio risalite da ManageRidesController
            // (es. tentativo di annullare una prenotazione già chiusa).
            System.out.println("Errore: " + e.getMessage());
        }
    }
}
