package com.ispw.uniride.model.observer;

/**
 * Ruolo di Oggetto Osservabile (Soggetto) nel GoF Pattern Observer.
 * Qualunque entità di dominio che cambia stato dinamicamente può estendere
 * questa interfaccia per pubblicare eventi sul suo stato a una platea di listener.
 */
public interface Subject {

    /**
     * Sottoscrizione di un nuovo ascoltatore per ricevere le notifiche.
     * @param observer l'ascoltatore che si intende aggiungere (es. Passeggero)
     */
    void attach(Observer observer);

    /**
     * Rimozione dalla lista broadcast.
     * @param observer l'ascoltatore da scartare
     */
    void detach(Observer observer);

    /**
     * Itera sulla lista e invia ai sottoscrittori il messaggio.
     * @param message il messaggio (payload) scambiato nell'evento
     */
    void notifyObservers(String message);
}
