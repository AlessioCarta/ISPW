package com.ispw.uniride.boundary;

import com.ispw.uniride.bean.BookingBean;
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
import java.util.Collections;
import java.util.List;

public class ManageRidesBoundary {

    private static final String ERROR_PREFIX = "Errore: ";

    @FXML private ListView<RideBean> offeredList;
    @FXML private ListView<BookingBean> pendingRequestsList;
    @FXML private ListView<BookingBean> bookedList;
    @FXML private Label messageLabel;

    private ManageRidesController controller = new ManageRidesController();

    @FXML
    public void initialize() {
        offeredList.setCellFactory(param -> new ListCell<RideBean>() {
            @Override
            protected void updateItem(RideBean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("Da: %s A: %s | Data: %s | Stato: %s",
                            item.getDeparture(), item.getDestination(), item.getDate(), translateRideStatus(item.getStatus())));
                }
            }
        });

        pendingRequestsList.setCellFactory(param -> new ListCell<BookingBean>() {
            @Override
            protected void updateItem(BookingBean item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : "Richiesta di: " + item.getCounterpartName());
            }
        });

        bookedList.setCellFactory(param -> new ListCell<BookingBean>() {
            @Override
            protected void updateItem(BookingBean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("Da: %s A: %s | Data: %s | Guidatore: %s | Stato: %s",
                            item.getDeparture(), item.getDestination(), item.getDate(),
                            item.getCounterpartName(), translateBookingStatus(item.getState())));
                }
            }
        });

        // Selezionando una corsa offerta si ricaricano le sue richieste di prenotazione pendenti.
        offeredList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> refreshPendingRequests());

        refreshData();
    }

    private String translateRideStatus(String status) {
        if (status == null) return "";
        switch (status) {
            case "AVAILABLE": return "Disponibile";
            case "FULL": return "Al completo";
            case "COMPLETED": return "Completata";
            case "CANCELLED": return "Annullata";
            default: return status;
        }
    }

    private String translateBookingStatus(String state) {
        if (state == null) return "";
        switch (state) {
            case "REQUESTED": return "In attesa";
            case "CONFIRMED": return "Confermata";
            case "REJECTED": return "Rifiutata";
            case "CANCELLED": return "Annullata";
            default: return state;
        }
    }

    private void refreshData() {
        try {
            List<RideBean> offered = controller.getMyOfferedRides();
            offeredList.setItems(FXCollections.observableArrayList(offered));

            List<BookingBean> booked = controller.getMyBookedRides();
            bookedList.setItems(FXCollections.observableArrayList(booked));

            refreshPendingRequests();
        } catch (Exception e) {
            messageLabel.setText(e.getMessage());
            messageLabel.setTextFill(javafx.scene.paint.Color.RED);
        }
    }

    private void refreshPendingRequests() {
        RideBean selectedRide = offeredList.getSelectionModel().getSelectedItem();
        if (selectedRide == null) {
            pendingRequestsList.setItems(FXCollections.observableArrayList(Collections.emptyList()));
            return;
        }
        try {
            List<BookingBean> pending = controller.getPendingRequestsForRide(selectedRide.getId());
            pendingRequestsList.setItems(FXCollections.observableArrayList(pending));
        } catch (Exception e) {
            messageLabel.setText(e.getMessage());
            messageLabel.setTextFill(javafx.scene.paint.Color.RED);
        }
    }

    @FXML
    public void handleConfirmRequest(ActionEvent event) {
        BookingBean selected = pendingRequestsList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Seleziona una richiesta dalla lista!");
            messageLabel.setTextFill(javafx.scene.paint.Color.RED);
            return;
        }
        try {
            controller.confirmBooking(selected.getBookingId());
            messageLabel.setText("Richiesta confermata.");
            messageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
            refreshData();
        } catch (Exception e) {
            messageLabel.setText(ERROR_PREFIX + e.getMessage());
            messageLabel.setTextFill(javafx.scene.paint.Color.RED);
        }
    }

    @FXML
    public void handleRejectRequest(ActionEvent event) {
        BookingBean selected = pendingRequestsList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Seleziona una richiesta dalla lista!");
            messageLabel.setTextFill(javafx.scene.paint.Color.RED);
            return;
        }
        try {
            controller.rejectBooking(selected.getBookingId());
            messageLabel.setText("Richiesta rifiutata: il posto è tornato disponibile.");
            messageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
            refreshData();
        } catch (Exception e) {
            messageLabel.setText(ERROR_PREFIX + e.getMessage());
            messageLabel.setTextFill(javafx.scene.paint.Color.RED);
        }
    }

    @FXML
    public void handleCompleteRide(ActionEvent event) {
        RideBean selected = offeredList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Seleziona una corsa offerta dalla lista!");
            messageLabel.setTextFill(javafx.scene.paint.Color.RED);
            return;
        }
        try {
            controller.completeRide(selected.getId());
            messageLabel.setText("Corsa segnata come completata.");
            messageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
            refreshData();
        } catch (Exception e) {
            messageLabel.setText(ERROR_PREFIX + e.getMessage());
            messageLabel.setTextFill(javafx.scene.paint.Color.RED);
        }
    }

    @FXML
    public void handleCancelBooking(ActionEvent event) {
        BookingBean selected = bookedList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Seleziona una prenotazione dalla lista!");
            messageLabel.setTextFill(javafx.scene.paint.Color.RED);
            return;
        }

        try {
            controller.cancelBooking(selected.getRideId());
            messageLabel.setText("Prenotazione annullata con successo.");
            messageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
            refreshData(); // ricarica le liste
        } catch (Exception e) {
            messageLabel.setText(ERROR_PREFIX + e.getMessage());
            messageLabel.setTextFill(javafx.scene.paint.Color.RED);
        }
    }

    @FXML
    public void handleCancelOfferedRide(ActionEvent event) {
        RideBean selected = offeredList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Seleziona una corsa offerta dalla lista!");
            messageLabel.setTextFill(javafx.scene.paint.Color.RED);
            return;
        }

        try {
            controller.cancelOfferedRide(selected.getId());
            messageLabel.setText("Corsa annullata con successo.");
            messageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
            refreshData(); // ricarica le liste
        } catch (Exception e) {
            messageLabel.setText(ERROR_PREFIX + e.getMessage());
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
