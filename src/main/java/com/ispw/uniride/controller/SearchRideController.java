package com.ispw.uniride.controller;

import com.ispw.uniride.bean.RideBean;
import com.ispw.uniride.dao.DAOFactory;
import com.ispw.uniride.dao.RideDAO;
import com.ispw.uniride.model.Ride;
import com.ispw.uniride.model.Student;
import com.ispw.uniride.model.strategy.CostStrategy;
import com.ispw.uniride.model.strategy.EqualSplitStrategy;

import java.util.ArrayList;
import java.util.List;

public class SearchRideController {

    public List<RideBean> searchRides(String departure, String destination) {
        RideDAO rideDAO = DAOFactory.getInstance().getRideDAO();
        List<Ride> availableRides = rideDAO.getAvailableRides(departure, destination);

        List<RideBean> rideBeans = new ArrayList<>();
        CostStrategy costStrategy = new EqualSplitStrategy();

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

            // Calculate estimated cost for current passengers
            int numPassengers = ride.getTotalSeats() - ride.getAvailableSeats();
            double estimatedCost = costStrategy.calculateCost(ride.getBasePrice(), numPassengers);
            bean.setComputedPrice(estimatedCost);

            rideBeans.add(bean);
        }

        return rideBeans;
    }

    public boolean bookRide(String rideId) throws Exception {
        Student loggedUser = Session.getInstance().getLoggedUser();
        if (loggedUser == null) {
            throw new Exception("Utente non loggato!");
        }

        RideDAO rideDAO = DAOFactory.getInstance().getRideDAO();
        Ride ride = rideDAO.getRideById(rideId);

        if (ride == null) {
            throw new Exception("Passaggio non trovato!");
        }

        // Simplification for prototype: passenger is not a complex observer here,
        // we just execute the booking logic using State pattern via Ride.
        boolean success = ride.bookSeat(message -> {
            // Observer callback simulation
            com.ispw.uniride.utils.LoggerCustom.info("Notification for " + loggedUser.getUsername() + ": " + message);
        });

        if (success) {
            rideDAO.updateRide(ride);
            return true;
        }

        return false;
    }
}
