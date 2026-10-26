package com.ispw.uniride.boundary;

import com.ispw.uniride.config.LocationCatalog;
import com.ispw.uniride.bean.RegistrationBean;
import com.ispw.uniride.controller.LoginController;
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
    private final LoginController loginController = new LoginController();

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
            LocationAutoComplete.attach(homeLocationField, LocationCatalog.getAllNames());
        }
    }

    @FXML
    public void handleRegisterSubmit(ActionEvent event) {
        try {
            // I setter del Bean verificano il formato di ogni campo (obbligatori, username,
            // lunghezza della password, telefono): la Boundary si limita a raccogliere i valori.
            // Posizione e telefono restano facoltativi. getEditor().getText() invece di getValue():
            // la ComboBox è editabile, quindi si legge il testo effettivamente scritto nel campo.
            RegistrationBean bean = new RegistrationBean();
            bean.setFullName(fullNameField.getText());
            bean.setUsername(usernameField.getText());
            bean.setPassword(passwordField.getText());
            bean.setHomeLocation(homeLocationField.getEditor().getText());
            bean.setPhoneNumber(phoneNumberField.getText());
            loginController.registerUser(bean);
            messageLabel.setText("Registrazione Completata!");
            messageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
        } catch (Exception e) {
            // L'eccezione risalita dal Controller Applicativo (username duplicato, password
            // troppo corta) viene qui intercettata e tradotta in un messaggio per l'utente:
            // esattamente il compito del Controller Grafico nel flusso delle eccezioni BCE.
            messageLabel.setText(ErrorMessages.forUser(e));
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
            ErrorMessages.showViewLoadError();
        }
    }
}
