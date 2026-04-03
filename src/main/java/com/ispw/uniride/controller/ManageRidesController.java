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
