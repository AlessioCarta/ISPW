package com.ispw.uniride.boundary;

import com.ispw.uniride.bean.RideBean;
import com.ispw.uniride.controller.ManageRidesController;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class ManageRidesBoundary {

    @FXML private ListView<RideBean> offeredList;
    @FXML private ListView<RideBean> bookedList;
    @FXML private Label messageLabel;

    private ManageRidesController controller = new ManageRidesController();

    @FXML
    public void initialize() {
        setupListView(offeredList);
        setupListView(bookedList);
        refreshData();
    }

    private void setupListView(ListView<RideBean> listView) {
        listView.setCellFactory(param -> new ListCell<RideBean>() {
            @Override
            protected void updateItem(RideBean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("Da: %s A: %s | Data: %s | Guidatore: %s | Stato: %s",
                            item.getDeparture(), item.getDestination(), item.getDate(),
                            item.getDriver(), item.getStatus()));
                }
            }
        });
    }

    private void refreshData() {
        try {
            List<RideBean> offered = controller.getMyOfferedRides();
            offeredList.setItems(FXCollections.observableArrayList(offered));

            List<RideBean> booked = controller.getMyBookedRides();
            bookedList.setItems(FXCollections.observableArrayList(booked));

        } catch (Exception e) {
            messageLabel.setText(e.getMessage());
            messageLabel.setTextFill(javafx.scene.paint.Color.RED);
        }
    }

    @FXML
    public void handleCancelBooking(ActionEvent event) {
        RideBean selected = bookedList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Seleziona una prenotazione dalla lista!");
            messageLabel.setTextFill(javafx.scene.paint.Color.RED);
            return;
        }

        try {
            controller.cancelBooking(selected.getId());
            messageLabel.setText("Prenotazione annullata con successo.");
            messageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
            refreshData(); // ricarica le liste
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
