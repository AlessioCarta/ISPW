package com.ispw.uniride.boundary;

import com.ispw.uniride.config.LocationCatalog;
import com.ispw.uniride.controller.LoginController;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Boundary grafica (JavaFX Controller) mappata sull'XML {@code Register.fxml}.
 * Raccoglie i dati del modulo di registrazione, valida solo la loro presenza (non le regole di
 * business, che restano nel Controller Applicativo) e li inoltra a {@link LoginController}.
 */
public class RegisterBoundary {
    // Nodi dell'albero FXML iniettati automaticamente da JavaFX in base a fx:id.
    @FXML private TextField fullNameField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> homeLocationField;
    @FXML private TextField phoneNumberField;
    @FXML private Label messageLabel;

    // Dependency injection manuale del Controller Applicativo: essendo questa classe istanziata
    // dall'FXMLLoader (non da un costruttore che controlliamo noi), l'inizializzazione diretta
    // del campo è il modo più semplice per garantirne la disponibilità.
    private LoginController loginController = new LoginController();

    /**
     * Popola il menu a tendina delle località con l'intero catalogo, per far scegliere
     * allo studente il proprio comune/paese senza doverlo digitare a mano.
     */
    @FXML
    public void initialize() {
        // Controllo di sicurezza: initialize() viene chiamato automaticamente da JavaFX dopo
        // l'iniezione dei campi @FXML, ma un null-check protegge da un eventuale caricamento
        // FXML incompleto o da un riutilizzo del controller fuori dal contesto atteso.
        if (homeLocationField != null) {
            homeLocationField.setItems(FXCollections.observableArrayList(LocationCatalog.getAllNames()));
        }
    }

    @FXML
    public void handleRegisterSubmit(ActionEvent event) {
        String fullName = fullNameField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();
        // getEditor().getText() invece di getValue(): la ComboBox è editabile (l'utente può
        // digitare una località non presente nel catalogo), quindi si legge sempre il testo
        // effettivamente scritto nel campo, non solo un valore selezionato dalla lista.
        String homeLocation = homeLocationField.getEditor().getText();
        String phoneNumber = phoneNumberField.getText();

        // Validazione di solo formato (campi non vuoti): compito del Controller Grafico secondo
        // BCE, non delle regole di business più profonde (username duplicato, password troppo
        // corta), che restano nel Controller Applicativo.
        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Compila tutti i campi!");
            messageLabel.setTextFill(javafx.scene.paint.Color.RED);
            return;
        }

        try {
            // Posizione e telefono sono entrambi facoltativi: se lasciati vuoti, l'account resta
            // pienamente funzionante, solo privo dei relativi suggerimenti/contatti.
            loginController.registerUser(username, password, fullName, homeLocation, phoneNumber);
            messageLabel.setText("Registrazione Completata!");
            messageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
        } catch (Exception e) {
            // L'eccezione risalita dal Controller Applicativo (username duplicato, password
            // troppo corta) viene qui intercettata e tradotta in un messaggio per l'utente:
            // esattamente il compito del Controller Grafico nel flusso delle eccezioni BCE.
            messageLabel.setText(e.getMessage());
            messageLabel.setTextFill(javafx.scene.paint.Color.RED);
        }
    }

    @FXML
    public void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ispw/uniride/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) messageLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 420, 480));
            stage.setTitle("UniRide - Login");
        } catch (IOException e) {
            com.ispw.uniride.utils.LoggerCustom.error("Errore schermata Login", e);
        }
    }
}
