package com.ispw.uniride.exceptions;

/**
 * Eccezione innescata nel momento in cui una logica di transizione logica del Pattern State
 * o un'azione su una Ride viene annullata.
 */
public class RideActionException extends Exception {
    // Checked exception (extends Exception, non RuntimeException): il Controller Applicativo
    // deve dichiararla esplicitamente nella firma del metodo, e il Controller Grafico è
    // costretto a gestirla, coerentemente con la regola BCE "le eccezioni risalgono dal basso
    // e vengono intercettate/tradotte in messaggio dal Controller Grafico".
    public RideActionException(String message) {
        super(message);
    }
}
