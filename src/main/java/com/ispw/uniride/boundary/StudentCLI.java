package com.ispw.uniride.boundary;

import com.ispw.uniride.controller.LoginController;

import java.util.Scanner;

/**
 * Boundary testuale del menu principale post-login (equivalente CLI di
 * {@link StudentDashboardBoundary}): smista verso le altre Boundary CLI in base alla scelta,
 * nessuna logica applicativa qui dentro.
 */
public class StudentCLI {
    public void start() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n=== Dashboard Studente ===");
            System.out.println("1. Offri un Passaggio");
            System.out.println("2. Cerca un Passaggio");
            System.out.println("3. Le Mie Corse");
            System.out.println("4. Logout");
            System.out.print("Scegli un'opzione: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline: nextInt() non legge il carattere \n finale.

            switch (choice) {
                case 1:
                    // Ognuna di queste chiamate blocca il ciclo corrente finché l'utente non
                    // esce dalla sotto-schermata, poi il controllo ritorna qui al menu principale.
                    new OfferRideCLI().start();
                    break;
                case 2:
                    new SearchRideCLI().start();
                    break;
                case 3:
                    new ManageRidesCLI().start();
                    break;
                case 4:
                    new LoginController().logout();
                    System.out.println("Logout effettuato.");
                    // return (non break): esce definitivamente dal metodo, terminando il menu
                    // e riportando il controllo a LoginCLI.
                    return;
                default:
                    System.out.println("Opzione non valida.");
            }
        }
    }
}
