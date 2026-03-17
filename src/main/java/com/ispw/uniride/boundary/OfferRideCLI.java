package com.ispw.uniride.boundary;

import com.ispw.uniride.bean.RideBean;
import com.ispw.uniride.controller.OfferRideController;

import java.util.Scanner;

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

        RideBean bean = new RideBean(departure, destination, date, totalSeats, basePrice);

        try {
            offerRideController.offerRide(bean);
            System.out.println("Passaggio offerto con successo!");
        } catch (Exception e) {
            System.err.println("Errore: " + e.getMessage());
        }
    }
}
