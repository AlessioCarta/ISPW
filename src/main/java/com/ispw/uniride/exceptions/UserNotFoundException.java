package com.ispw.uniride.exceptions;

/**
 * Eccezione innescata nel momento in cui l'utente ricercato non è presente
 * nel sistema di persistenza dati.
 */
public class UserNotFoundException extends Exception {
    public UserNotFoundException(String message) {
        super(message);
    }
}
