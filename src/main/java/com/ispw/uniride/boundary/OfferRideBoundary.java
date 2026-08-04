package com.ispw.uniride.boundary;

import com.ispw.uniride.bean.RideBean;
import com.ispw.uniride.controller.OfferRideController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class OfferRideBoundary {

    @FXML private TextField departureField;
    @FXML private TextField destinationField;
    @FXML private TextField dateField;
    @FXML private TextField seatsField;
    @FXML private TextField priceField;
    @FXML private Label messageLabel;

    private OfferRideController offerRideController = new OfferRideController();

    @FXML
    public void handleSubmit(ActionEvent event) {
        try {
            String departure = departureField.getText();
            String destination = destinationField.getText();
            String date = dateField.getText();
            int seats = Integer.parseInt(seatsField.getText());
            double price = Double.parseDouble(priceField.getText());

            RideBean bean = new RideBean(departure, destination, date, seats, price);
            offerRideController.offerRide(bean);

            messageLabel.setText("Passaggio offerto con successo!");
            messageLabel.setTextFill(javafx.scene.paint.Color.GREEN);

            // Clear form
            departureField.clear();
            destinationField.clear();
            dateField.clear();
            seatsField.clear();
            priceField.clear();

        } catch (NumberFormatException e) {
            messageLabel.setText("Errore: I campi numerici non sono validi.");
            messageLabel.setTextFill(javafx.scene.paint.Color.RED);
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
