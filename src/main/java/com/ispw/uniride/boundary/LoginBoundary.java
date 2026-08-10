package com.ispw.uniride.boundary;

import com.ispw.uniride.bean.UserBean;
import com.ispw.uniride.controller.LoginController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Boundary grafica (JavaFX Controller) mappata sull'XML `Login.fxml`.
 * Acquisisce i dati grafici e li incanala al Controller BCE proteggendolo da dipendenze UI.
 */
public class LoginBoundary {

    // Nodi dell'albero FXML iniettati automaticamente
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    // Riferimento allo strato Control. Essendo una classe JavaFX istanziata dall'FXMLLoader, la dependency injection manuale
    // qui funge da inizializzazione statica sicura.
    private LoginController loginController = new LoginController();

    /**
     * Intercetta l'evento di Click sul pulsante grafico di autenticazione.
     */
    @FXML
    public void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Incapsula e nasconde la natura dell'I/O dal livello superiore passando solo DTO.
        UserBean bean = new UserBean(username, password);

        // De-eleva la gestione applicativa al Control layer
        if (loginController.login(bean)) {
            messageLabel.setText("Login Effettuato!");
            messageLabel.setTextFill(javafx.scene.paint.Color.GREEN);

            // Effettua lo switch del Layout grafico solo ad operazione BCE conclusa
            loadDashboard();
        } else {
            messageLabel.setText("Credenziali non valide!");
            messageLabel.setTextFill(javafx.scene.paint.Color.RED);
        }
    }

    /**
     * Routine di utility per caricare la pagina successiva su questa medesima finestra (Stage).
     */
    @FXML
    public void handleRegister(ActionEvent event) {
        try {
            // Carica il file FXML della schermata di registrazione e ne costruisce l'albero di nodi.
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ispw/uniride/Register.fxml"));
            Parent root = loader.load();
            // Risale dallo Stage attuale (la finestra) tramite un nodo già presente in scena
            // (usernameField), per poterne sostituire la Scene con quella appena caricata.
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root, 440, 560));
            stage.setTitle("UniRide - Registrazione");
        } catch (IOException e) {
            com.ispw.uniride.utils.LoggerCustom.error("Errore caricamento Register", e);
        }
    }

    private void loadDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ispw/uniride/StudentDashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) usernameField.getScene().getWindow(); // Recupera l'appiglio del display corrente
            stage.setScene(new Scene(root, 760, 520));
            stage.setTitle("UniRide - Dashboard");
        } catch (IOException e) {
            // Sfrutta il Logger custom per nascondere il noioso stack trace standard
            com.ispw.uniride.utils.LoggerCustom.error("Errore caricamento Dashboard", e);
        }
    }
}
