package com.ispw.uniride.boundary;

import com.ispw.uniride.bean.UserBean;
import com.ispw.uniride.controller.LoginController;

import java.util.Scanner;

/**
 * Boundary testuale per l'inserimento delle credenziali (Scenario "Avvio e Autenticazione").
 * Sostituisce la View FXML in esecuzioni Headless.
 */
public class LoginCLI {

    // Riferimento al livello Logico (Controller) - Regola Architetturale BCE.
    private LoginController loginController = new LoginController();

    /**
     * Esegue un ciclo o blocco di sistema attendendo gli input testuali in console (I/O).
     */
    public void start() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n=== UniRide CLI ===");
            System.out.println("1. Login");
            System.out.println("2. Registrati");
            System.out.println("3. Esci");
            System.out.print("Scelta: ");
            String choice = scanner.nextLine();

            if (choice.equals("1")) {
                System.out.print("Username: ");
                String username = scanner.nextLine();
                System.out.print("Password: ");
                String password = scanner.nextLine();

                UserBean bean = new UserBean(username, password);

                if (loginController.login(bean)) {
                    System.out.println("Login effettuato con successo!");
                    new StudentCLI().start();
                } else {
                    System.out.println("Credenziali non valide. Accesso Negato.");
                }
            } else if (choice.equals("2")) {
                System.out.print("Nome Completo: ");
                String fullName = scanner.nextLine();
                System.out.print("Nuovo Username: ");
                String username = scanner.nextLine();
                System.out.print("Password: ");
                String password = scanner.nextLine();

                try {
                    loginController.registerUser(username, password, fullName);
                    System.out.println("Registrazione completata! Ora puoi fare il login.");
                } catch (Exception e) {
                    System.out.println("Errore: " + e.getMessage());
                }
            } else if (choice.equals("3")) {
                break;
            }
        }
    }
}
