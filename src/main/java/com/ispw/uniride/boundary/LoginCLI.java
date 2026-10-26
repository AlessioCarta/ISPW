package com.ispw.uniride.boundary;

import com.ispw.uniride.bean.RegistrationBean;
import com.ispw.uniride.bean.UserBean;
import com.ispw.uniride.controller.LoginController;
import com.ispw.uniride.exceptions.InvalidInputException;
import com.ispw.uniride.exceptions.PersistenceException;


/**
 * Boundary testuale per l'inserimento delle credenziali (Scenario "Avvio e Autenticazione").
 * Sostituisce la View FXML in esecuzioni Headless: invoca lo stesso identico
 * {@link LoginController} usato da {@link LoginBoundary}, a riprova che il disaccoppiamento
 * Boundary/Control (BCE) è reale e non solo dichiarato.
 */
public class LoginCLI {

    // Riferimento al livello Logico (Controller) - Regola Architetturale BCE.
    private final LoginController loginController = new LoginController();

    /**
     * Esegue un ciclo o blocco di sistema attendendo gli input testuali in console (I/O).
     */
    public void start() {
        // Tutte le letture passano da InputCLI, che usa un unico Scanner per l'intero processo.
        while (true) {
            System.out.println("\n=== UniRide CLI ===");
            System.out.println("1. Login");
            System.out.println("2. Registrati");
            System.out.println("3. Esci");
            String choice = InputCLI.readLine("Scelta: ").trim();

            if (choice.equals("1")) {
                try {
                    // Stesso Bean usato dalla controparte grafica: il Controller Applicativo non
                    // sa (e non deve sapere) se i dati arrivano da un form o dalla console.
                    UserBean bean = new UserBean();
                    bean.setUsername(InputCLI.readLine("Username: "));
                    bean.setPassword(InputCLI.readLine("Password: "));
                    if (loginController.login(bean)) {
                        System.out.println("Login effettuato con successo!");
                        // Login riuscito: passa il controllo al menu testuale post-autenticazione.
                        new StudentCLI().start();
                    } else {
                        System.out.println("Credenziali non valide. Accesso Negato.");
                    }
                } catch (InvalidInputException | PersistenceException e) {
                    System.out.println("Errore: " + e.getMessage());
                }
            } else if (choice.equals("2")) {
                try {
                    // La CLI, a differenza della GUI, non offre un catalogo da cui scegliere la
                    // posizione: resta facoltativa e viene lasciata vuota.
                    RegistrationBean bean = new RegistrationBean();
                    bean.setFullName(InputCLI.readLine("Nome Completo: "));
                    bean.setUsername(InputCLI.readLine("Nuovo Username: "));
                    bean.setPassword(InputCLI.readLine("Password: "));
                    bean.setPhoneNumber(InputCLI.readLine("Numero di telefono (facoltativo, invio per saltare): "));
                    loginController.registerUser(bean);
                    System.out.println("Registrazione completata! Ora puoi fare il login.");
                } catch (Exception e) {
                    // Errori di formato (dal Bean) e di dominio (username duplicato) vengono qui
                    // intercettati e stampati, stesso ruolo di un Controller Grafico.
                    System.out.println("Errore: " + ErrorMessages.forUser(e));
                }
            } else if (choice.equals("3")) {
                // Esce dal ciclo di menu principale, terminando il programma CLI.
                break;
            }
        }
    }
}
