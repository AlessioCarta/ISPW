package com.ispw.uniride.controller;

import com.ispw.uniride.model.Booking;
import com.ispw.uniride.model.observer.Observer;
import com.ispw.uniride.utils.LoggerCustom;

/**
 * Observer concreto (Pattern Observer) che rappresenta la prenotazione attiva di un passeggero
 * su un {@code Ride}: quando il guidatore annulla la corsa, il Subject notifica questo observer,
 * che porta la prenotazione in stato {@code CANCELLED}.
 * Vive nel package controller e non nel model perché è il Controller Applicativo a decidere
 * quali prenotazioni iscrivere; l'observer non accede ai DAO, si limita a modificare l'entità
 * in memoria, e la persistenza resta compito del Controller dopo il salvataggio della corsa.
 */
class PassengerBookingObserver implements Observer {

    private final Booking booking;
    private boolean cancelled = false;

    PassengerBookingObserver(Booking booking) {
        this.booking = booking;
    }

    @Override
    public void update(String message) {
        // Il Subject invia la notifica a ogni evento; solo il primo annullamento effettivo conta.
        if (!cancelled && booking.cancel()) {
            cancelled = true;
            LoggerCustom.info("Notifica a " + booking.getPassengerUsername() + ": " + message);
        }
    }

    /** @return {@code true} se la notifica ricevuta ha annullato la prenotazione, che va quindi salvata. */
    boolean hasCancelledBooking() {
        return cancelled;
    }

    Booking getBooking() {
        return booking;
    }
}
