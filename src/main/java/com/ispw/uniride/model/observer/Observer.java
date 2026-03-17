package com.ispw.uniride.model.observer;

/**
 * Ruolo di Ascoltatore (Listener o Abbonato) all'interno del Pattern Observer.
 * Fornisce il metodo standard per la ricezione passiva di messaggi dal Subject.
 */
public interface Observer {

    /**
     * Funzione callback invocata dal Soggetto osservato in seguito a variazioni di stato.
     * @param message il messaggio o le specifiche della mutazione notificata
     */
    void update(String message);
}
