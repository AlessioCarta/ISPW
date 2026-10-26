package com.ispw.uniride.controller;

import com.ispw.uniride.dao.BookingDAO;
import com.ispw.uniride.dao.DAOFactory;
import com.ispw.uniride.dao.RideDAO;
import com.ispw.uniride.model.Booking;
import com.ispw.uniride.model.Ride;

import java.util.List;

/**
 * Applica la scadenza automatica delle richieste di prenotazione a cui il guidatore non ha
 * risposto entro 24 ore (Pure Fabrication GRASP, condivisa dai Controller Applicativi).
 * La verifica è "pigra": avviene ogni volta che un Controller legge le prenotazioni di una
 * corsa, invece che con un processo in background, così il comportamento resta identico in
 * tutte e tre le modalità di persistenza.
 */
final class RequestExpiry {

    /**
     * Fa scadere le richieste ancora in attesa da troppo tempo fra quelle indicate, liberando il
     * posto che ciascuna teneva riservato sulla corsa.
     * @param bookings le prenotazioni appena lette dal DAO; quelle scadute vengono aggiornate in place.
     */
    static void expireOverdue(List<Booking> bookings) {
        long now = System.currentTimeMillis();
        BookingDAO bookingDAO = DAOFactory.getInstance().getBookingDAO();
        RideDAO rideDAO = DAOFactory.getInstance().getRideDAO();
        for (Booking booking : bookings) {
            if (booking.isResponseOverdue(now) && booking.expire()) {
                Ride ride = rideDAO.getRideById(booking.getRideId());
                if (ride != null && ride.cancelSeat(booking.getPassengerUsername())) {
                    rideDAO.updateRide(ride);
                }
                bookingDAO.updateBooking(booking);
            }
        }
    }

    private RequestExpiry() {
    }
}
