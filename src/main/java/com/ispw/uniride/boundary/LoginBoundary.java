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

public class LoginBoundary {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    private LoginController loginController = new LoginController();

    @FXML
    public void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        UserBean bean = new UserBean(username, password);

        if (loginController.login(bean)) {
            messageLabel.setText("Login Effettuato!");
            messageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
            // Load Student Dashboard
            loadDashboard();
        } else {
            messageLabel.setText("Credenziali non valide!");
            messageLabel.setTextFill(javafx.scene.paint.Color.RED);
        }
    }

    private void loadDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ispw/uniride/StudentDashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root, 600, 400));
            stage.setTitle("UniRide - Dashboard");
        } catch (IOException e) {
            com.ispw.uniride.utils.LoggerCustom.error("Errore caricamento Dashboard", e);
        }
    }
}
