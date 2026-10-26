package com.ispw.uniride.exceptions;

/**
 * Eccezione innescata quando lo strato di persistenza (database H2 o file CSV) non riesce a
 * leggere o scrivere i dati richiesti.
 */
public class PersistenceException extends RuntimeException {
    // Unchecked (extends RuntimeException), a differenza delle altre eccezioni di dominio: un
    // guasto di I/O non è una regola di business violata dall'utente, ma un errore tecnico che
    // può nascere in qualunque metodo DAO. Renderla checked obbligherebbe a modificare la firma
    // di tutte le interfacce DAO; in questo modo invece risale comunque fino al Controller
    // Grafico, che la intercetta nel proprio catch e la mostra all'utente, invece di far
    // credere che l'operazione sia andata a buon fine.
    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
