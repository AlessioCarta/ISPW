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

/**
 * Boundary grafica (JavaFX Controller) mappata sull'XML {@code StudentDashboard.fxml}.
 * Funge da "hub" centrale dopo il login: ogni bottone carica una diversa schermata sullo stesso
 * Stage (la finestra), sostituendone la Scene. Ogni {@code handleXxx} segue lo stesso schema:
 * carica l'FXML della destinazione, ne sostituisce la Scene sullo Stage corrente, imposta il
 * titolo — nessuna logica applicativa qui, solo navigazione fra le View.
 */
public class StudentDashboardBoundary {

    @FXML
    private Label welcomeLabel;

    @FXML
    public void initialize() {
        // Se la Sessione risulta valorizzata (login avvenuto con successo prima di arrivare
        // qui), personalizza il messaggio di benvenuto col nome dello studente autenticato.
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
            // Risale allo Stage corrente tramite un nodo già in scena (welcomeLabel), per
            // sostituirne la Scene con quella appena caricata.
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
        // A differenza degli altri handler, qui c'è anche un'azione applicativa (svuotare la
        // Sessione) PRIMA della navigazione verso la schermata di Login.
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
