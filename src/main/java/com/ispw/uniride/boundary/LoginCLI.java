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
        System.out.println("=== Login UniRide ===");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        // Incapsula logicamente gli input prima del passaggio per isolare il Dominio.
        UserBean bean = new UserBean(username, password);

        // Richiama l'applicativo vero e proprio che elabora e interroga lo strato sottostante.
        if (loginController.login(bean)) {
            System.out.println("Login effettuato con successo!");
            // Transizione della Boundary (equivalente a "cambia scena FXML" ma testuale)
            new StudentCLI().start();
        } else {
            System.out.println("Credenziali non valide. Accesso Negato.");
        }
    }
}
