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

public class RegisterBoundary {
    @FXML private TextField fullNameField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> homeLocationField;
    @FXML private Label messageLabel;

    private LoginController loginController = new LoginController();

    /**
     * Popola il menu a tendina delle località con l'intero catalogo, per far scegliere
     * allo studente il proprio comune/paese senza doverlo digitare a mano.
     */
    @FXML
    public void initialize() {
        if (homeLocationField != null) {
            homeLocationField.setItems(FXCollections.observableArrayList(LocationCatalog.getAllNames()));
        }
    }

    @FXML
    public void handleRegisterSubmit(ActionEvent event) {
        String fullName = fullNameField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();
        String homeLocation = homeLocationField.getEditor().getText();

        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Compila tutti i campi!");
            messageLabel.setTextFill(javafx.scene.paint.Color.RED);
            return;
        }

        try {
            // La posizione è facoltativa: se lasciata vuota, Offri/Cerca Passaggio mostreranno
            // l'elenco completo delle località senza suggerimenti di vicinanza.
            loginController.registerUser(username, password, fullName, homeLocation);
            messageLabel.setText("Registrazione Completata!");
            messageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
        } catch (Exception e) {
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
