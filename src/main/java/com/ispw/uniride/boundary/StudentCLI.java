package com.ispw.uniride.boundary;

import com.ispw.uniride.controller.LoginController;

import java.util.Scanner;

public class StudentCLI {
    public void start() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n=== Dashboard Studente ===");
            System.out.println("1. Offri un Passaggio");
            System.out.println("2. Cerca un Passaggio");
            System.out.println("3. Logout");
            System.out.print("Scegli un'opzione: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    new OfferRideCLI().start();
                    break;
                case 2:
                    new SearchRideCLI().start();
                    break;
                case 3:
                    new LoginController().logout();
                    System.out.println("Logout effettuato.");
                    return;
                default:
                    System.out.println("Opzione non valida.");
            }
        }
    }
}
