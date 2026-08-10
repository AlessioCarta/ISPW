package com.ispw.uniride.controller;

import com.ispw.uniride.bean.BookingBean;
import com.ispw.uniride.bean.RideBean;
import com.ispw.uniride.dao.BookingDAO;
import com.ispw.uniride.dao.DAOFactory;
import com.ispw.uniride.dao.RideDAO;
import com.ispw.uniride.exceptions.RideActionException;
import com.ispw.uniride.exceptions.RideNotFoundException;
import com.ispw.uniride.exceptions.UserNotAuthorizedException;
import com.ispw.uniride.model.Booking;
import com.ispw.uniride.model.Ride;
import com.ispw.uniride.model.Student;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller per la gestione delle proprie corse (sia quelle offerte che quelle prenotate),
 * incluso il workflow di conferma/rifiuto delle richieste di prenotazione da parte del guidatore.
 */
public class ManageRidesController {

    public List<RideBean> getMyOfferedRides() throws UserNotAuthorizedException {
        Student user = Session.getInstance().getLoggedUser();
        if (user == null) throw new UserNotAuthorizedException("Utente non loggato!");

        RideDAO dao = DAOFactory.getInstance().getRideDAO();
        List<Ride> rides = dao.getRidesByDriver(user.getUsername());
        return convertToBeans(rides);
    }

    /**
     * Le corse "prenotate" mostrate al passeggero riflettono ora lo stato reale della sua
     * richiesta (in attesa o confermata): una richiesta rifiutata o annullata non compare più,
     * perché il passeggero non detiene più un posto attivo su quella corsa.
     */
    public List<BookingBean> getMyBookedRides() throws UserNotAuthorizedException {
        Student user = Session.getInstance().getLoggedUser();
        if (user == null) throw new UserNotAuthorizedException("Utente non loggato!");

        BookingDAO bookingDAO = DAOFactory.getInstance().getBookingDAO();
        RideDAO rideDAO = DAOFactory.getInstance().getRideDAO();

        List<BookingBean> beans = new ArrayList<>();
        for (Booking booking : bookingDAO.getBookingsByPassenger(user.getUsername())) {
            // Rifiutate/annullate: il passeggero non ha più un posto attivo, quindi non compaiono.
            boolean isActive = Booking.REQUESTED.equals(booking.getState()) || Booking.CONFIRMED.equals(booking.getState());
            Ride ride = isActive ? rideDAO.getRideById(booking.getRideId()) : null;
            if (ride != null) {
                beans.add(new BookingBean(booking.getId(), ride.getId(), ride.getDeparture(), ride.getDestination(),
                        ride.getDate(), ride.getDriverUsername(), booking.getState()));
            }
        }
        return beans;
    }

    /**
     * Recupera le richieste di prenotazione ancora in sospeso su una corsa offerta dall'utente
     * loggato, così il guidatore può confermarle o rifiutarle.
     * @throws UserNotAuthorizedException se l'utente non è loggato o non è il conducente della corsa.
     * @throws RideNotFoundException se la corsa non esiste.
     */
    public List<BookingBean> getPendingRequestsForRide(String rideId) throws UserNotAuthorizedException, RideNotFoundException {
        Student user = Session.getInstance().getLoggedUser();
        if (user == null) throw new UserNotAuthorizedException("Utente non loggato!");

        RideDAO rideDAO = DAOFactory.getInstance().getRideDAO();
        Ride ride = rideDAO.getRideById(rideId);
        if (ride == null) throw new RideNotFoundException("Corsa inesistente.");
        if (!ride.getDriverUsername().equals(user.getUsername())) {
            throw new UserNotAuthorizedException("Non sei il conducente di questa corsa.");
        }

        BookingDAO bookingDAO = DAOFactory.getInstance().getBookingDAO();
        List<BookingBean> beans = new ArrayList<>();
        for (Booking booking : bookingDAO.getBookingsByRide(rideId)) {
            if (!Booking.REQUESTED.equals(booking.getState())) {
                continue; // Solo le richieste ancora da evadere interessano al guidatore.
            }
            beans.add(new BookingBean(booking.getId(), ride.getId(), ride.getDeparture(), ride.getDestination(),
                    ride.getDate(), booking.getPassengerUsername(), booking.getState()));
        }
        return beans;
    }

    /**
     * Il guidatore accetta una richiesta di prenotazione: il posto, già riservato al momento
     * della richiesta, resta confermato.
     */
    public void confirmBooking(String bookingId) throws UserNotAuthorizedException, RideNotFoundException, RideActionException {
        Booking booking = requireOwnedPendingBooking(bookingId);
        if (!booking.confirm()) {
            throw new RideActionException("Richiesta già gestita in precedenza.");
        }
        DAOFactory.getInstance().getBookingDAO().updateBooking(booking);
    }

    /**
     * Il guidatore rifiuta una richiesta di prenotazione: il posto riservato viene liberato
     * sul passaggio, tornando disponibile per altri studenti.
     */
    public void rejectBooking(String bookingId) throws UserNotAuthorizedException, RideNotFoundException, RideActionException {
        Booking booking = requireOwnedPendingBooking(bookingId);
        if (!booking.reject()) {
            throw new RideActionException("Richiesta già gestita in precedenza.");
        }

        // Il rifiuto libera il posto che era stato riservato fisicamente su Ride al momento
        // della richiesta (Pattern State): senza questa chiamata il posto resterebbe "bloccato"
        // pur non avendo più un passeggero confermato.
        RideDAO rideDAO = DAOFactory.getInstance().getRideDAO();
        Ride ride = rideDAO.getRideById(booking.getRideId());
        if (ride != null) {
            ride.cancelSeat(booking.getPassengerUsername());
            rideDAO.updateRide(ride);
        }

        DAOFactory.getInstance().getBookingDAO().updateBooking(booking);
    }

    /**
     * Recupera una richiesta di prenotazione verificando che l'utente loggato sia il conducente
     * della corsa a cui si riferisce (unico autorizzato a confermarla o rifiutarla).
     */
    private Booking requireOwnedPendingBooking(String bookingId) throws UserNotAuthorizedException, RideNotFoundException {
        Student user = Session.getInstance().getLoggedUser();
        if (user == null) throw new UserNotAuthorizedException("Utente non loggato!");

        BookingDAO bookingDAO = DAOFactory.getInstance().getBookingDAO();
        Booking booking = bookingDAO.getBookingById(bookingId);
        if (booking == null) throw new RideNotFoundException("Richiesta di prenotazione non trovata.");

        RideDAO rideDAO = DAOFactory.getInstance().getRideDAO();
        Ride ride = rideDAO.getRideById(booking.getRideId());
        if (ride == null) throw new RideNotFoundException("Corsa inesistente.");
        if (!ride.getDriverUsername().equals(user.getUsername())) {
            throw new UserNotAuthorizedException("Non sei il conducente di questa corsa.");
        }
        return booking;
    }

    /**
     * Il passeggero annulla di propria iniziativa una richiesta/prenotazione attiva (in attesa o
     * già confermata), liberando il posto sulla corsa.
     */
    public void cancelBooking(String rideId) throws UserNotAuthorizedException, RideNotFoundException, RideActionException {
        Student user = Session.getInstance().getLoggedUser();
        if (user == null) throw new UserNotAuthorizedException("Utente non loggato!");

        RideDAO rideDAO = DAOFactory.getInstance().getRideDAO();
        Ride ride = rideDAO.getRideById(rideId);
        if (ride == null) throw new RideNotFoundException("Corsa inesistente.");

        // Cerca, fra tutte le richieste sulla corsa, quella attiva (in attesa o confermata)
        // dell'utente loggato: una sola può esistere per definizione (SearchRideController
        // impedisce richieste duplicate), quindi ci si ferma alla prima trovata.
        BookingDAO bookingDAO = DAOFactory.getInstance().getBookingDAO();
        Booking activeBooking = null;
        for (Booking booking : bookingDAO.getBookingsByRide(rideId)) {
            if (booking.getPassengerUsername().equals(user.getUsername())
                    && (Booking.REQUESTED.equals(booking.getState()) || Booking.CONFIRMED.equals(booking.getState()))) {
                activeBooking = booking;
                break;
            }
        }

        if (activeBooking == null) {
            throw new RideActionException("Non sei prenotato a questa corsa.");
        }

        // Delega all'Entità l'incapsulamento del Pattern State e la rimozione passeggero.
        boolean success = ride.cancelSeat(user.getUsername());
        if (!success) {
            throw new RideActionException("Impossibile cancellare il posto in questo momento (logica di Stato).");
        }
        rideDAO.updateRide(ride);

        activeBooking.cancel();
        bookingDAO.updateBooking(activeBooking);
    }

    /**
     * Annulla una corsa offerta dall'utente loggato (in qualità di conducente). A differenza
     * della versione precedente, è consentito anche con passeggeri già prenotati: la corsa
     * passa allo stato {@code CANCELLED} (non viene più eliminata fisicamente) e i passeggeri
     * a bordo vengono notificati tramite il Pattern Observer.
     * @throws UserNotAuthorizedException se l'utente non è loggato o non è il conducente della corsa.
     * @throws RideNotFoundException se l'id non corrisponde ad alcuna corsa esistente.
     * @throws RideActionException se la corsa è già in uno stato terminale (completata o già annullata).
     */
    public void cancelOfferedRide(String rideId) throws UserNotAuthorizedException, RideNotFoundException, RideActionException {
        Ride ride = requireOwnedRide(rideId);

        if (!ride.cancel()) {
            throw new RideActionException("La corsa è già stata completata o annullata.");
        }
        DAOFactory.getInstance().getRideDAO().updateRide(ride);
    }

    /**
     * Segna una corsa offerta come conclusa (viaggio avvenuto).
     * @throws RideActionException se la corsa è già in uno stato terminale.
     */
    public void completeRide(String rideId) throws UserNotAuthorizedException, RideNotFoundException, RideActionException {
        Ride ride = requireOwnedRide(rideId);

        if (!ride.complete()) {
            throw new RideActionException("La corsa è già stata completata o annullata.");
        }
        DAOFactory.getInstance().getRideDAO().updateRide(ride);
    }

    private Ride requireOwnedRide(String rideId) throws UserNotAuthorizedException, RideNotFoundException {
        Student user = Session.getInstance().getLoggedUser();
        if (user == null) throw new UserNotAuthorizedException("Utente non loggato!");

        RideDAO dao = DAOFactory.getInstance().getRideDAO();
        Ride ride = dao.getRideById(rideId);
        if (ride == null) throw new RideNotFoundException("Corsa inesistente.");
        if (!ride.getDriverUsername().equals(user.getUsername())) {
            throw new UserNotAuthorizedException("Non sei il conducente di questa corsa.");
        }
        return ride;
    }

    /**
     * Confine BCE esplicito: un Controller Applicativo non lascia mai uscire un oggetto Model
     * (Ride) verso la Boundary, lo converte sempre in Bean prima.
     */
    private List<RideBean> convertToBeans(List<Ride> rides) {
        List<RideBean> beans = new ArrayList<>();
        for (Ride r : rides) {
            RideBean b = new RideBean(r.getId(), r.getDriverUsername(), r.getDeparture(), r.getDestination(),
                    r.getDate(), r.getTotalSeats(), r.getAvailableSeats(), r.getBasePrice(), r.getStatus());
            beans.add(b);
        }
        return beans;
    }
}
