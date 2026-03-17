package com.ispw.uniride.controller;

import com.ispw.uniride.bean.RideBean;
import com.ispw.uniride.dao.DAOFactory;
import com.ispw.uniride.dao.RideDAO;
import com.ispw.uniride.model.Ride;
import com.ispw.uniride.model.Student;

public class OfferRideController {

    public void offerRide(RideBean rideBean) throws Exception {
        Student loggedUser = Session.getInstance().getLoggedUser();
        if (loggedUser == null) {
            throw new Exception("Utente non loggato!");
        }

        RideDAO rideDAO = DAOFactory.getInstance().getRideDAO();

        Ride newRide = new Ride(
                loggedUser.getUsername(),
                rideBean.getDeparture(),
                rideBean.getDestination(),
                rideBean.getDate(),
                rideBean.getTotalSeats(),
                rideBean.getBasePrice()
        );

        rideDAO.saveRide(newRide);
    }
}
