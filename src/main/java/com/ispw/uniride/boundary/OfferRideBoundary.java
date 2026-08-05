package com.ispw.uniride.boundary;

import com.ispw.uniride.bean.RideBean;
import com.ispw.uniride.config.LocationCatalog;
import com.ispw.uniride.config.LocationInfo;
import com.ispw.uniride.controller.OfferRideController;
import com.ispw.uniride.controller.Session;
import com.ispw.uniride.model.Student;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OfferRideBoundary {

    // Formato testuale coerente con quello atteso storicamente dal dominio (gg/mm/aaaa)
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML private ComboBox<String> departureField;
    @FXML private ComboBox<String> destinationField;
    @FXML private DatePicker dateField;
    @FXML private Spinner<Integer> seatsField;
    @FXML private TextField priceField;
    @FXML private Label messageLabel;

    private OfferRideController offerRideController = new OfferRideController();

    /**
     * Popola i menu a tendina delle località e il contatore dei posti al caricamento della vista.
     * Se lo studente loggato ha dichiarato una posizione riconosciuta dal catalogo, le località
     * vengono ordinate dalla più vicina alla più lontana e la partenza/destinazione predefinite
     * vengono preimpostate (partenza = paese dello studente, destinazione = ateneo più vicino),
     * restando comunque liberamente modificabili per cercare qualunque altra tratta.
     */
    @FXML
    public void initialize() {
        Student loggedUser = Session.getInstance().getLoggedUser();
        LocationInfo home = loggedUser != null ? LocationCatalog.findByName(loggedUser.getHomeLocation()) : null;

        List<String> locationNames = home != null
                ? LocationCatalog.getNamesSortedByDistanceFrom(home)
                : LocationCatalog.getAllNames();

        departureField.setItems(FXCollections.observableArrayList(locationNames));
        destinationField.setItems(FXCollections.observableArrayList(locationNames));

        if (home != null) {
            departureField.setValue(home.getName());
            String nearestUniversity = LocationCatalog.getNearestUniversityCity(home);
            if (nearestUniversity != null) {
                destinationField.setValue(nearestUniversity);
            }
        }

        seatsField.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 8, 3));
    }

    @FXML
    public void handleSubmit(ActionEvent event) {
        try {
            String departure = readCity(departureField);
            String destination = readCity(destinationField);
            String date = dateField.getValue() != null ? dateField.getValue().format(DATE_FORMAT) : "";
            int seats = seatsField.getValue();
            double price = Double.parseDouble(priceField.getText());

            RideBean bean = new RideBean(departure, destination, date, seats, price);
            offerRideController.offerRide(bean);

            messageLabel.setText("Passaggio offerto con successo!");
            messageLabel.setTextFill(javafx.scene.paint.Color.GREEN);

            // Clear form
            departureField.getEditor().clear();
            departureField.setValue(null);
            destinationField.getEditor().clear();
            destinationField.setValue(null);
            dateField.setValue(null);
            seatsField.getValueFactory().setValue(3);
            priceField.clear();

        } catch (NumberFormatException e) {
            messageLabel.setText("Errore: I campi numerici non sono validi.");
            messageLabel.setTextFill(javafx.scene.paint.Color.RED);
        } catch (Exception e) {
            messageLabel.setText("Errore: " + e.getMessage());
            messageLabel.setTextFill(javafx.scene.paint.Color.RED);
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
