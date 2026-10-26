package com.ispw.uniride.exceptions;

/**
 * Eccezione innescata da un Bean quando un dato inserito dall'utente non rispetta il formato
 * richiesto (campo obbligatorio vuoto, numero non leggibile, data o orario malformati).
 */
public class InvalidInputException extends Exception {
    // Checked, come le altre eccezioni di dominio: la Boundary che compila il Bean è obbligata a
    // gestirla e a mostrarne il messaggio. A differenza di RideActionException non riguarda una
    // regola di business, ma solo la forma del dato: per questo nasce nel Bean e non nel
    // Controller Applicativo.
    public InvalidInputException(String message) {
        super(message);
    }
}
