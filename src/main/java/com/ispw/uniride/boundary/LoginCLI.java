package com.ispw.uniride.boundary;

import com.ispw.uniride.bean.UserBean;
import com.ispw.uniride.controller.LoginController;

import java.util.Scanner;

/**
 * Boundary testuale per l'inserimento delle credenziali (Scenario "Avvio e Autenticazione").
 * Sostituisce la View FXML in esecuzioni Headless: invoca lo stesso identico
 * {@link LoginController} usato da {@link LoginBoundary}, a riprova che il disaccoppiamento
 * Boundary/Control (BCE) è reale e non solo dichiarato.
 */
public class LoginCLI {

    // Riferimento al livello Logico (Controller) - Regola Architetturale BCE.
    private LoginController loginController = new LoginController();

    /**
     * Esegue un ciclo o blocco di sistema attendendo gli input testuali in console (I/O).
     */
    public void start() {
        // Un solo Scanner condiviso per tutto il ciclo: System.in è uno stream, aprirne uno
        // nuovo ad ogni lettura ne perderebbe la posizione corrente.
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

                // Stesso Bean usato dalla controparte grafica: il Controller Applicativo non
                // sa (e non deve sapere) se i dati arrivano da un form o dalla console.
                UserBean bean = new UserBean(username, password);

                if (loginController.login(bean)) {
                    System.out.println("Login effettuato con successo!");
                    // Login riuscito: passa il controllo al menu testuale post-autenticazione.
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
                System.out.print("Numero di telefono (facoltativo, invio per saltare): ");
                String phoneNumber = scanner.nextLine();

                try {
                    // Overload senza homeLocation: la CLI, a differenza della GUI, non offre
                    // ancora un modo pratico per scegliere una posizione da un catalogo. Il
                    // telefono invece è già raccolto anche qui, essendo un semplice campo testuale.
                    loginController.registerUser(username, password, fullName, null, phoneNumber);
                    System.out.println("Registrazione completata! Ora puoi fare il login.");
                } catch (Exception e) {
                    // Le eccezioni di dominio (username duplicato, password troppo corta)
                    // vengono qui intercettate e stampate, stesso ruolo di un Controller Grafico.
                    System.out.println("Errore: " + e.getMessage());
                }
            } else if (choice.equals("3")) {
                // Esce dal ciclo di menu principale, terminando il programma CLI.
                break;
            }
        }
    }
}
