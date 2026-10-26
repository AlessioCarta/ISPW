package com.ispw.uniride.boundary;

import com.ispw.uniride.bean.BookingBean;
import com.ispw.uniride.bean.RideBean;
import com.ispw.uniride.controller.ManageRidesController;
import com.ispw.uniride.exceptions.RideActionException;
import com.ispw.uniride.exceptions.RideNotFoundException;
import com.ispw.uniride.exceptions.UserNotAuthorizedException;

import java.util.List;

/**
 * Boundary testuale del caso d'uso "Le Mie Corse" (equivalente CLI di {@link ManageRidesBoundary}).
 * Offre le stesse operazioni della controparte grafica: come guidatore, conferma/rifiuto delle
 * richieste pendenti, completamento e annullamento di una corsa offerta; come passeggero,
 * annullamento di una prenotazione. Invoca lo stesso Controller Applicativo, senza alcuna
 * logica di dominio ripetuta qui.
 */
public class ManageRidesCLI {
    private static final String ERROR_PREFIX = "Errore: ";
    private static final String NONE_SELECTED = "Nessuna corsa selezionata.";

    private final ManageRidesController controller = new ManageRidesController();

    public void start() {
        while (true) {
            System.out.println("\n--- Le Mie Corse ---");
            System.out.println("1. Corse offerte (come guidatore)");
            System.out.println("2. Prenotazioni (come passeggero)");
            System.out.println("0. Torna alla Dashboard");
            int choice = InputCLI.readChoice("Scegli un'opzione: ");
            if (choice == 0) {
                return;
            }
            try {
                if (choice == 1) {
                    manageOfferedRides();
                } else if (choice == 2) {
                    manageBookings();
                } else {
                    System.out.println("Opzione non valida.");
                }
            } catch (Exception e) {
                // Eccezioni di dominio e di persistenza risalite da ManageRidesController
                // (es. richiesta già gestita, corsa già annullata) tradotte in un messaggio.
                System.out.println(ERROR_PREFIX + ErrorMessages.forUser(e));
            }
        }
    }

    private void manageOfferedRides() throws UserNotAuthorizedException, RideNotFoundException, RideActionException {
        List<RideBean> offered = controller.getMyOfferedRides();
        if (offered.isEmpty()) {
            System.out.println("Nessuna corsa offerta.");
            return;
        }
        for (int i = 0; i < offered.size(); i++) {
            RideBean r = offered.get(i);
            System.out.printf("%d. %s -> %s (%s %s) [%s] posti liberi: %d%n", i + 1, r.getDeparture(),
                    r.getDestination(), r.getDate(), r.getDepartureTime(), r.getStatus(), r.getAvailableSeats());
        }
        RideBean ride = select(offered, InputCLI.readChoice("Numero della corsa (0 per uscire): "));
        if (ride == null) {
            System.out.println(NONE_SELECTED);
            return;
        }
        System.out.println("1. Gestisci le richieste in attesa");
        System.out.println("2. Segna come completata");
        System.out.println("3. Annulla la corsa");
        switch (InputCLI.readChoice("Azione: ")) {
            case 1 -> manageRequests(ride);
            case 2 -> {
                controller.completeRide(ride.getId());
                System.out.println("Corsa segnata come completata.");
            }
            case 3 -> {
                controller.cancelOfferedRide(ride.getId());
                System.out.println("Corsa annullata: i passeggeri prenotati sono stati avvisati.");
            }
            default -> System.out.println("Opzione non valida.");
        }
    }

    private void manageRequests(RideBean ride) throws UserNotAuthorizedException, RideNotFoundException, RideActionException {
        List<BookingBean> pending = controller.getPendingRequestsForRide(ride.getId());
        if (pending.isEmpty()) {
            System.out.println("Nessuna richiesta in attesa per questa corsa.");
            return;
        }
        for (int i = 0; i < pending.size(); i++) {
            BookingBean b = pending.get(i);
            System.out.printf("%d. %s | Tel: %s%n", i + 1, b.getCounterpartName(),
                    b.getCounterpartPhone() != null ? b.getCounterpartPhone() : "non fornito");
        }
        BookingBean request = select(pending, InputCLI.readChoice("Numero della richiesta (0 per uscire): "));
        if (request == null) {
            return;
        }
        String answer = InputCLI.readLine("Confermi (C) o rifiuti (R)? ").trim();
        if (answer.equalsIgnoreCase("C")) {
            controller.confirmBooking(request.getBookingId());
            System.out.println("Richiesta confermata.");
        } else if (answer.equalsIgnoreCase("R")) {
            controller.rejectBooking(request.getBookingId());
            System.out.println("Richiesta rifiutata: il posto è di nuovo disponibile.");
        } else {
            System.out.println("Nessuna azione eseguita.");
        }
    }

    /**
     * Stampa le prenotazioni attive del passeggero, telefono del guidatore compreso: è mostrato
     * solo qui, dove esiste già una richiesta di passaggio concreta fra i due, mai in un elenco
     * di ricerca.
     */
    private void manageBookings() throws UserNotAuthorizedException, RideNotFoundException, RideActionException {
        List<BookingBean> booked = controller.getMyBookedRides();
        if (booked.isEmpty()) {
            System.out.println("Nessuna prenotazione attiva.");
            return;
        }
        for (int i = 0; i < booked.size(); i++) {
            BookingBean b = booked.get(i);
            System.out.printf("%d. Guidatore: %s | Tel: %s | %s -> %s (%s) [%s]%n",
                    i + 1, b.getCounterpartName(),
                    b.getCounterpartPhone() != null ? b.getCounterpartPhone() : "non fornito",
                    b.getDeparture(), b.getDestination(), b.getDate(), b.getState());
        }
        BookingBean selected = select(booked, InputCLI.readChoice("Numero della prenotazione da annullare (0 per uscire): "));
        if (selected != null) {
            controller.cancelBooking(selected.getRideId());
            System.out.println("Prenotazione annullata con successo!");
        }
    }

    /**
     * @return l'elemento scelto (scelta 1-based), oppure {@code null} se la scelta è 0 o fuori range.
     */
    private static <T> T select(List<T> items, int choice) {
        return choice > 0 && choice <= items.size() ? items.get(choice - 1) : null;
    }
}
