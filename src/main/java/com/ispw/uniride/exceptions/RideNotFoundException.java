package com.ispw.uniride.exceptions;

/**
 * Eccezione specifica per quando una risorsa "Passaggio" non è rintracciabile
 * nello storage o è stata dismessa dal driver.
 */
public class RideNotFoundException extends Exception {
    public RideNotFoundException(String message) {
        super(message);
    }
}
