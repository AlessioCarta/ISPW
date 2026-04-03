package com.ispw.uniride.exceptions;

/**
 * Eccezione innescata nel momento in cui una logica di transizione logica del Pattern State
 * o un'azione su una Ride viene annullata.
 */
public class RideActionException extends Exception {
    public RideActionException(String message) {
        super(message);
    }
}
