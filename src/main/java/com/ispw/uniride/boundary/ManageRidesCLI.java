package com.ispw.uniride.boundary;

import com.ispw.uniride.bean.RideBean;
import com.ispw.uniride.controller.ManageRidesController;

import java.util.List;
import java.util.Scanner;

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
                    System.out.printf("ID: %s | %s -> %s (%s) [%s]\n", r.getId(), r.getDeparture(), r.getDestination(), r.getDate(), r.getStatus());
                }
            }

            System.out.println("\n[PRENOTAZIONI - Come Passeggero]");
            List<RideBean> booked = controller.getMyBookedRides();
            if (booked.isEmpty()) System.out.println("Nessuna prenotazione attiva.");
            else {
                for (int i = 0; i < booked.size(); i++) {
                    RideBean r = booked.get(i);
                    System.out.printf("%d. [%s] Guidatore: %s | %s -> %s (%s) [%s]\n",
                            i+1, r.getId(), r.getDriver(), r.getDeparture(), r.getDestination(), r.getDate(), r.getStatus());
                }
            }

            if (!booked.isEmpty()) {
                System.out.print("\nVuoi annullare una prenotazione? Inserisci il numero (0 per uscire): ");
                int choice = scanner.nextInt();
                scanner.nextLine();

                if (choice > 0 && choice <= booked.size()) {
                    RideBean selected = booked.get(choice - 1);
                    controller.cancelBooking(selected.getId());
                    System.out.println("Prenotazione annullata con successo!");
                }
            } else {
                System.out.println("\nPremi Invio per tornare alla Dashboard...");
                scanner.nextLine();
            }

        } catch (Exception e) {
            System.out.println("Errore: " + e.getMessage());
        }
    }
}
