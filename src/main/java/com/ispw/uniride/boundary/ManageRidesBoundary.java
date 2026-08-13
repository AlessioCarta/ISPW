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

/**
 * Boundary grafica (JavaFX Controller) mappata sull'XML {@code ManageRides.fxml}.
 * La schermata più corposa dell'applicazione: mostra insieme le corse offerte dall'utente
 * (come guidatore), le richieste pendenti su quelle corse, e le prenotazioni effettuate
 * dall'utente stesso (come passeggero). Ogni {@code handleXxx} segue lo stesso schema:
 * legge la selezione corrente da una ListView, valida che non sia vuota, invoca il metodo
 * corrispondente su {@link ManageRidesController} e infine ricarica i dati con {@code refreshData()}.
 */
public class ManageRidesBoundary {

    private static final String ERROR_PREFIX = "Errore: ";

    @FXML private ListView<RideBean> offeredList;
    @FXML private ListView<BookingBean> pendingRequestsList;
    @FXML private ListView<BookingBean> bookedList;
    @FXML private Label messageLabel;

    private ManageRidesController controller = new ManageRidesController();

    @FXML
    public void initialize() {
        // Ognuna delle 3 ListView ha una propria ListCell custom: definiscono come ogni bean
        // viene renderizzato come testo leggibile invece di usare il toString() di default.
        offeredList.setCellFactory(param -> new ListCell<RideBean>() {
            @Override
            protected void updateItem(RideBean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("Da: %s A: %s | Data: %s | Ore: %s | Stato: %s",
                            item.getDeparture(), item.getDestination(), item.getDate(), formatTime(item.getDepartureTime()), translateRideStatus(item.getStatus())));
                }
            }
        });

        pendingRequestsList.setCellFactory(param -> new ListCell<BookingBean>() {
            @Override
            protected void updateItem(BookingBean item, boolean empty) {
                super.updateItem(item, empty);
                // Il telefono del passeggero richiedente è già visibile qui: la richiesta stessa
                // è la prova del contatto fra i due, non serve attendere la conferma.
                setText(empty || item == null ? null : "Richiesta di: " + item.getCounterpartName() + " | Tel: " + formatPhone(item.getCounterpartPhone()));
            }
        });

        bookedList.setCellFactory(param -> new ListCell<BookingBean>() {
            @Override
            protected void updateItem(BookingBean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("Da: %s A: %s | Data: %s | Guidatore: %s | Tel: %s | Stato: %s",
                            item.getDeparture(), item.getDestination(), item.getDate(),
                            item.getCounterpartName(), formatPhone(item.getCounterpartPhone()), translateBookingStatus(item.getState())));
                }
            }
        });

        // Selezionando una corsa offerta si ricaricano le sue richieste di prenotazione pendenti.
        offeredList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> refreshPendingRequests());

        refreshData();
    }

    /**
     * Traduce la sigla testuale interna dello stato di un Ride (quella prodotta da
     * {@code RideState.getStatus()}) nell'etichetta in italiano da mostrare in interfaccia,
     * mantenendo il dominio applicativo indipendente dalla lingua di presentazione.
     */
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

    /**
     * Rende leggibile un orario di partenza eventualmente assente (passaggi creati prima
     * dell'introduzione di questo campo), invece di stampare "null" nella cella della lista.
     */
    private String formatTime(String departureTime) {
        return departureTime != null && !departureTime.isEmpty() ? departureTime : "n.d.";
    }

    /**
     * Rende leggibile un numero di telefono eventualmente non dichiarato dalla controparte,
     * invece di stampare "null" nella cella della lista.
     */
    private String formatPhone(String phoneNumber) {
        return phoneNumber != null && !phoneNumber.isEmpty() ? phoneNumber : "non fornito";
    }

    /** Come {@link #translateRideStatus}, ma per le sigle di stato di {@code Booking}. */
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

    /**
     * Ricarica dal Controller tutte e tre le liste mostrate in schermata: chiamato dopo ogni
     * azione che modifica lo stato di una corsa o di una prenotazione, così l'interfaccia
     * riflette sempre lo stato più recente del dominio.
     */
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

    /**
     * Ricarica le richieste pendenti relative alla corsa offerta attualmente selezionata
     * nella lista {@code offeredList} (nessuna selezione = lista pendenti vuota).
     */
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
        // Pattern comune a tutti gli handler di questa classe: niente selezionato -> messaggio
        // di guida per l'utente e uscita immediata, senza nemmeno interpellare il Controller.
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
            // Il posto era già riservato: nulla cambia sui posti disponibili, ma lo stato
            // della richiesta sì, quindi le liste vanno comunque ricaricate.
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
            // A differenza della conferma, il rifiuto libera anche il posto riservato su Ride
            // (gestito internamente da ManageRidesController.rejectBooking).
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
            // Transizione di stato (Pattern State) verso COMPLETED: fallisce se la corsa è già
            // in uno stato terminale (già completata o annullata).
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
            // Il passeggero rinuncia di propria iniziativa: libera il posto sulla Ride
            // associata, indipendentemente dal fatto che la richiesta fosse già confermata.
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
            // Consentito anche con passeggeri già a bordo: la corsa passa a CANCELLED (Pattern
            // State) e i passeggeri prenotati vengono avvisati tramite il Pattern Observer.
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
