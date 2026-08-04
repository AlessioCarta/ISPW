package com.ispw.uniride.boundary;

import com.ispw.uniride.controller.LoginController;
import com.ispw.uniride.controller.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class StudentDashboardBoundary {

    @FXML
    private Label welcomeLabel;

    @FXML
    public void initialize() {
        if (Session.getInstance().getLoggedUser() != null) {
            welcomeLabel.setText("Benvenuto, " + Session.getInstance().getLoggedUser().getFullName() + "!");
        }
    }

    @FXML
    public void handleOfferRide(ActionEvent event) {
        com.ispw.uniride.utils.LoggerCustom.info("Opening Offer Ride view...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ispw/uniride/OfferRide.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 560, 640));
            stage.setTitle("UniRide - Offri Passaggio");
        } catch (IOException e) {
            com.ispw.uniride.utils.LoggerCustom.error("Errore caricamento Offri Passaggio", e);
        }
    }

    @FXML
    public void handleSearchRide(ActionEvent event) {
        com.ispw.uniride.utils.LoggerCustom.info("Opening Search Ride view...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ispw/uniride/SearchRide.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 820, 600));
            stage.setTitle("UniRide - Cerca Passaggio");
        } catch (IOException e) {
            com.ispw.uniride.utils.LoggerCustom.error("Errore caricamento Cerca Passaggio", e);
        }
    }

    @FXML
    public void handleMyRides(ActionEvent event) {
        com.ispw.uniride.utils.LoggerCustom.info("Opening My Rides view...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ispw/uniride/ManageRides.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 780, 600));
            stage.setTitle("UniRide - Le Mie Corse");
        } catch (IOException e) {
            com.ispw.uniride.utils.LoggerCustom.error("Errore caricamento Manage Rides", e);
        }
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        new LoginController().logout();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ispw/uniride/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 420, 480));
            stage.setTitle("UniRide - Login");
        } catch (IOException e) {
            com.ispw.uniride.utils.LoggerCustom.error("Errore caricamento Login", e);
        }
    }
}
