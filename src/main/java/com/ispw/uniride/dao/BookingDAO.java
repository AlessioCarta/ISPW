package com.ispw.uniride.dao;

import com.ispw.uniride.model.Booking;
import java.util.List;

/**
 * Interfaccia DAO per l'entità {@link Booking}, che modella lo stato di approvazione
 * (richiesta/confermata/rifiutata/annullata) di ogni singola prenotazione su un passaggio.
 */
public interface BookingDAO {

    /**
     * Crea un nuovo record di richiesta di prenotazione.
     */
    void saveBooking(Booking booking);

    /**
     * Preleva una singola prenotazione tramite il suo identificativo.
     */
    Booking getBookingById(String id);

    /**
     * Recupera tutte le richieste di prenotazione relative a un determinato passaggio
     * (usato dal guidatore per vedere ed evadere le richieste pendenti).
     */
    List<Booking> getBookingsByRide(String rideId);

    /**
     * Recupera tutte le richieste di prenotazione effettuate da un determinato passeggero.
     */
    List<Booking> getBookingsByPassenger(String passengerUsername);

    /**
     * Sovrascrive un record preesistente aggiornandone lo stato (conferma/rifiuto/annullamento).
     */
    void updateBooking(Booking booking);
}
