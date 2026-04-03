package com.ispw.uniride.boundary;

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

public class RegisterBoundary {
    @FXML private TextField fullNameField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    private LoginController loginController = new LoginController();

    @FXML
    public void handleRegisterSubmit(ActionEvent event) {
        String fullName = fullNameField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Compila tutti i campi!");
            messageLabel.setTextFill(javafx.scene.paint.Color.RED);
            return;
        }

        try {
            loginController.registerUser(username, password, fullName);
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
            stage.setScene(new Scene(root, 400, 300));
            stage.setTitle("UniRide - Login");
        } catch (IOException e) {
            com.ispw.uniride.utils.LoggerCustom.error("Errore schermata Login", e);
        }
    }
}
