package com.ispw.uniride.controller;

import com.ispw.uniride.bean.RideBean;
import com.ispw.uniride.config.Config;
import com.ispw.uniride.dao.BookingDAO;
import com.ispw.uniride.dao.DAOFactory;
import com.ispw.uniride.dao.RideDAO;
import com.ispw.uniride.exceptions.RideActionException;
import com.ispw.uniride.exceptions.RideNotFoundException;
import com.ispw.uniride.exceptions.UserNotAuthorizedException;
import com.ispw.uniride.model.Booking;
import com.ispw.uniride.model.Ride;
import com.ispw.uniride.model.Student;
import com.ispw.uniride.model.strategy.CostStrategy;
import com.ispw.uniride.model.strategy.DistanceCostStrategy;
import com.ispw.uniride.model.strategy.EqualSplitStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller logico dedicato alla risoluzione della ricerca attiva di mezzi e della successiva prenotazione (Use Cases).
 * Confeziona le logiche più corpose usando svariati Design Pattern come Strategy per l'addebito dinamico dei costi.
 */
public class SearchRideController {

    /**
     * Interroga lo strato astratto del database e processa ogni risultato traducendolo in oggetti "sicuri" e precalcolati
     * da inviare alla Boundary grafica.
     * @param departure origine logica del path scelto dall'utente UI
     * @param destination meta esatta della destinazione
     * @return Una collezione di oggetti Bean pronti per esser mappati in View (ListView o console iterativa)
     */
    public List<RideBean> searchRides(String departure, String destination) {

        // Estrazione risultati di dominio usando DAO Factory standard
        RideDAO rideDAO = DAOFactory.getInstance().getRideDAO();
        List<Ride> availableRides = rideDAO.getAvailableRides(departure, destination);

        List<RideBean> rideBeans = new ArrayList<>();

        // Implementazione Strategy dinamica via switch architetturale (Config.COST_STRATEGY_TYPE)
        CostStrategy costStrategy;
        if (Config.COST_STRATEGY_TYPE == Config.StrategyType.DISTANCE_WEIGHTED) {
            costStrategy = new DistanceCostStrategy();
        } else {
            costStrategy = new EqualSplitStrategy();
        }

        // Ciclo Mappatura logica tra l'Entità di Business e il DTO da esporre alla UI Grafica
        for (Ride ride : availableRides) {
            RideBean bean = new RideBean(
                    ride.getId(),
                    ride.getDriverUsername(),
                    ride.getDeparture(),
                    ride.getDestination(),
                    ride.getDate(),
                    ride.getTotalSeats(),
                    ride.getAvailableSeats(),
                    ride.getBasePrice(),
                    ride.getStatus()
            );

            // Calcolo stima economica usando lo Strategy pattern applicato sul momento del rendering in base al volume passegeri
            int numPassengers = ride.getTotalSeats() - ride.getAvailableSeats();
            double estimatedCost = costStrategy.calculateCost(ride.getBasePrice(), numPassengers);

            // Applica parametro custom per rendering grafico
            bean.setComputedPrice(estimatedCost);

            rideBeans.add(bean);
        }

        return rideBeans; // Invio della lista generata di View object alla Boundary che l'ha chiamata.
    }

    /**
     * Operazione transazionale successiva alla "Search". Modella la richiesta concreta di aggiungersi a un viaggio:
     * il posto viene riservato subito (per evitare overbooking durante l'attesa), ma la richiesta resta in stato
     * {@code REQUESTED} finché il guidatore non la conferma o la rifiuta esplicitamente dalla propria dashboard
     * (vedi {@code ManageRidesController.confirmBooking}/{@code rejectBooking}).
     * Applica internamente sia il Design Pattern State che il Design Pattern Observer invocando metodi della Entity `Ride`.
     * @param rideId ID hash ricavato dal bean visualizzato e su cui l'utente ha manifestato intenzione.
     * @return true se la richiesta (la scalata del posto a sedere in State Pattern) è valida, false in caso concorrente d'esaurimento slot.
     * @throws UserNotAuthorizedException se cade l'autenticazione utente.
     * @throws RideNotFoundException se l'ID in query non mappa alcuna entità.
     * @throws RideActionException se non è possibile ultimare logicamente lo stato o l'utente è già a bordo.
     */
    public boolean bookRide(String rideId) throws UserNotAuthorizedException, RideNotFoundException, RideActionException {

        // Step Sicurezza Base
        Student loggedUser = Session.getInstance().getLoggedUser();
        if (loggedUser == null) {
            throw new UserNotAuthorizedException("Utente non loggato!");
        }

        // Ritrova il record "reale" tramite DBMS Abstract Factory
        RideDAO rideDAO = DAOFactory.getInstance().getRideDAO();
        Ride ride = rideDAO.getRideById(rideId);

        if (ride == null) {
            throw new RideNotFoundException("Passaggio non trovato! Forse è già stato eliminato.");
        }

        // Regola di dominio: il conducente non può prenotare un posto sul passaggio che lui stesso offre.
        if (ride.getDriverUsername().equals(loggedUser.getUsername())) {
            throw new RideActionException("Non puoi prenotare un posto sul passaggio che hai offerto tu stesso.");
        }

        if (ride.getPassengerUsernames().contains(loggedUser.getUsername())) {
            throw new RideActionException("Hai già una richiesta attiva per questa corsa.");
        }

        // Esegue fisicamente la scalata. Notare il passaggio della funzione anonima Lambda
        // che fa le veci dell'interfaccia "Observer" ai fini prototipali (reazione ad eventi inviati in Push dal Subject "Ride").
        // Simulazione ricezione notifica Observer tramite Callback (Push mechanism di eventi testuali)
        boolean success = ride.bookSeat(message ->
                com.ispw.uniride.utils.LoggerCustom.info("Notifica Observer ricevuta da " + loggedUser.getUsername() + ": " + message),
                loggedUser.getUsername());

        // Solo in caso di risposta logica positiva da parte dell'Entità e del suo Pattern State, viene concretizzato l'Update sul database Storage.
        if (success) {
            rideDAO.updateRide(ride);

            // Il posto è riservato, ma la richiesta resta "in sospeso" fino alla decisione del guidatore.
            Booking booking = new Booking(ride.getId(), loggedUser.getUsername());
            BookingDAO bookingDAO = DAOFactory.getInstance().getBookingDAO();
            bookingDAO.saveBooking(booking);

            return true;
        }

        return false; // Altrimenti ritorna l'informazione di rigetto dell'iscrizione.
    }
}
