package com.ispw.uniride.boundary;

import com.ispw.uniride.bean.RideBean;
import com.ispw.uniride.config.LocationCatalog;
import com.ispw.uniride.config.LocationInfo;
import com.ispw.uniride.controller.SearchRideController;
import com.ispw.uniride.controller.Session;
import com.ispw.uniride.model.Student;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class SearchRideBoundary {

    @FXML private ComboBox<String> departureField;
    @FXML private ComboBox<String> destinationField;
    @FXML private ListView<RideBean> resultsList;
    @FXML private Label messageLabel;

    private SearchRideController searchRideController = new SearchRideController();

    @FXML
    public void initialize() {
        // Se lo studente ha dichiarato una posizione riconosciuta, mostra prima le località più
        // vicine a lui e preseleziona partenza/destinazione plausibili (paese di residenza e
        // ateneo più vicino), lasciando comunque piena libertà di cercare qualunque altra tratta.
        Student loggedUser = Session.getInstance().getLoggedUser();
        LocationInfo home = loggedUser != null ? LocationCatalog.findByName(loggedUser.getHomeLocation()) : null;

        List<String> locationNames = home != null
                ? LocationCatalog.getNamesSortedByDistanceFrom(home)
                : LocationCatalog.getAllNames();

        departureField.setItems(FXCollections.observableArrayList(locationNames));
        destinationField.setItems(FXCollections.observableArrayList(locationNames));

        if (home != null) {
            departureField.setValue(home.name());
            String nearestUniversity = LocationCatalog.getNearestUniversityCity(home);
            if (nearestUniversity != null) {
                destinationField.setValue(nearestUniversity);
            }
        }

        resultsList.setCellFactory(param -> new ListCell<RideBean>() {
            @Override
            protected void updateItem(RideBean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("Da: %s A: %s | Data: %s | Guidatore: %s | Posti: %d | Costo Stimato Attuale: %.2f€",
                            item.getDeparture(), item.getDestination(), item.getDate(),
                            item.getDriver(), item.getAvailableSeats(), item.getComputedPrice()));
                }
            }
        });
    }

    @FXML
    public void handleSearch(ActionEvent event) {
        String departure = readCity(departureField);
        String destination = readCity(destinationField);

        List<RideBean> rides = searchRideController.searchRides(departure, destination);

        ObservableList<RideBean> observableList = FXCollections.observableArrayList(rides);
        resultsList.setItems(observableList);

        if (rides.isEmpty()) {
            messageLabel.setText("Nessun passaggio trovato.");
            messageLabel.setTextFill(javafx.scene.paint.Color.RED);
        } else {
            messageLabel.setText("Risultati aggiornati.");
            messageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
        }
    }

    /**
     * Estrae il testo scelto/digitato in una ComboBox editabile, includendo l'input dell'utente
     * anche quando non ha ancora premuto Invio o spostato il focus.
     */
    private String readCity(ComboBox<String> comboBox) {
        String editorText = comboBox.getEditor().getText();
        return editorText != null ? editorText.trim() : "";
    }

    @FXML
    public void handleBook(ActionEvent event) {
        RideBean selected = resultsList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Seleziona un passaggio dalla lista prima di prenotare.");
            messageLabel.setTextFill(javafx.scene.paint.Color.RED);
            return;
        }

        try {
            boolean success = searchRideController.bookRide(selected.getId());
            if (success) {
                messageLabel.setText("Richiesta inviata! In attesa di conferma del guidatore.");
                messageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
                // Refresh list
                handleSearch(null);
            } else {
                messageLabel.setText("Posti esauriti!");
                messageLabel.setTextFill(javafx.scene.paint.Color.RED);
            }
        } catch (Exception e) {
            messageLabel.setText("Errore: " + e.getMessage());
            messageLabel.setTextFill(javafx.scene.paint.Color.RED);
        }
    }

    @FXML
    public void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ispw/uniride/StudentDashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) messageLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 760, 520));
            stage.setTitle("UniRide - Dashboard");
        } catch (IOException e) {
            com.ispw.uniride.utils.LoggerCustom.error("Errore ritorno alla Dashboard", e);
        }
    }
}
