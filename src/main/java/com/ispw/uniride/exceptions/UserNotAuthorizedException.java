package com.ispw.uniride.exceptions;

/**
 * Eccezione specifica per problemi di autenticazione o permessi nel sistema UniRide.
 */
public class UserNotAuthorizedException extends Exception {
    public UserNotAuthorizedException(String message) {
        super(message);
    }
}
