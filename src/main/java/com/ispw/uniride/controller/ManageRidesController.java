package com.ispw.uniride.controller;

import com.ispw.uniride.bean.RideBean;
import com.ispw.uniride.dao.DAOFactory;
import com.ispw.uniride.dao.RideDAO;
import com.ispw.uniride.exceptions.RideActionException;
import com.ispw.uniride.exceptions.RideNotFoundException;
import com.ispw.uniride.exceptions.UserNotAuthorizedException;
import com.ispw.uniride.model.Ride;
import com.ispw.uniride.model.Student;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller per la gestione delle proprie corse (sia quelle offerte che quelle prenotate).
 */
public class ManageRidesController {

    public List<RideBean> getMyOfferedRides() throws UserNotAuthorizedException {
        Student user = Session.getInstance().getLoggedUser();
        if (user == null) throw new UserNotAuthorizedException("Utente non loggato!");

        RideDAO dao = DAOFactory.getInstance().getRideDAO();
        List<Ride> rides = dao.getRidesByDriver(user.getUsername());
        return convertToBeans(rides);
    }

    public List<RideBean> getMyBookedRides() throws UserNotAuthorizedException {
        Student user = Session.getInstance().getLoggedUser();
        if (user == null) throw new UserNotAuthorizedException("Utente non loggato!");

        RideDAO dao = DAOFactory.getInstance().getRideDAO();
        List<Ride> rides = dao.getRidesByPassenger(user.getUsername());
        return convertToBeans(rides);
    }

    public void cancelBooking(String rideId) throws UserNotAuthorizedException, RideNotFoundException, RideActionException {
        Student user = Session.getInstance().getLoggedUser();
        if (user == null) throw new UserNotAuthorizedException("Utente non loggato!");

        RideDAO dao = DAOFactory.getInstance().getRideDAO();
        Ride ride = dao.getRideById(rideId);

        if (ride == null) throw new RideNotFoundException("Corsa inesistente.");

        if (!ride.getPassengerUsernames().contains(user.getUsername())) {
            throw new RideActionException("Non sei prenotato a questa corsa.");
        }

        // Delega all'Entità l'incapsulamento del Pattern State e la rimozione passeggero.
        boolean success = ride.cancelSeat(user.getUsername());
        if (success) {
            dao.updateRide(ride);
        } else {
            throw new RideActionException("Impossibile cancellare il posto in questo momento (logica di Stato).");
        }
    }

    /**
     * Annulla definitivamente una corsa offerta dall'utente loggato (in qualità di conducente).
     * Consentito soltanto se nessun passeggero si è ancora prenotato, per non lasciare compagni
     * di viaggio con una prenotazione confermata ma senza corsa.
     * @param rideId identificativo del passaggio offerto da rimuovere.
     * @throws UserNotAuthorizedException se l'utente non è loggato o non è il conducente della corsa.
     * @throws RideNotFoundException se l'id non corrisponde ad alcuna corsa esistente.
     * @throws RideActionException se la corsa ha già passeggeri prenotati.
     */
    public void cancelOfferedRide(String rideId) throws UserNotAuthorizedException, RideNotFoundException, RideActionException {
        Student user = Session.getInstance().getLoggedUser();
        if (user == null) throw new UserNotAuthorizedException("Utente non loggato!");

        RideDAO dao = DAOFactory.getInstance().getRideDAO();
        Ride ride = dao.getRideById(rideId);

        if (ride == null) throw new RideNotFoundException("Corsa inesistente.");

        if (!ride.getDriverUsername().equals(user.getUsername())) {
            throw new UserNotAuthorizedException("Non sei il conducente di questa corsa.");
        }

        if (!ride.getPassengerUsernames().isEmpty()) {
            throw new RideActionException("Non puoi annullare una corsa con passeggeri già prenotati.");
        }

        dao.deleteRide(rideId);
    }

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
