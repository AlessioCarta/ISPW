package com.ispw.uniride.dao;

import com.ispw.uniride.model.Ride;
import java.util.List;

/**
 * Interfaccia DAO (Data Access Object) per la classe Ride.
 * Implementa i canonici metodi CRUD slegati dal meccanismo di memorizzazione finale,
 * consentendo flessibilità per cambiare database (es. da RAM a File System o SQL)
 * senza toccare la Logica Applicativa (Controller).
 */
public interface RideDAO {
    /**
     * Crea un nuovo record nel livello di persistenza per l'entità indicata.
     */
    void saveRide(Ride ride);

    /**
     * Recupera ogni singolo passaggio a prescindere dal filtro o dallo stato di prenotabilità.
     * @return Una collezione ArrayList contenente tutti i risultati letti.
     */
    List<Ride> getAllRides();

    /**
     * Filtra logicamente l'archivio dei passaggi trovando le auto non piene
     * in base alla tratta esatta specificata dall'utente.
     * @param departure partenza desiderata.
     * @param destination destinazione d'arrivo cercata.
     */
    List<Ride> getAvailableRides(String departure, String destination);

    /**
     * Preleva l'oggetto puntuale interrogando il database per mezzo dell'identificativo.
     * @param id chiave univoca.
     */
    Ride getRideById(String id);

    /**
     * Sovrascrive un record preesistente aggiornandone lo Stato interno (es. posti a sedere residui).
     */
    void updateRide(Ride ride);

    /**
     * Recupera la lista dei passaggi in cui l'utente figura come autista.
     * @param username lo username dell'utente.
     */
    List<Ride> getRidesByDriver(String username);

    /**
     * Recupera la lista dei passaggi a cui l'utente si è prenotato come passeggero.
     * @param username lo username dell'utente.
     */
    List<Ride> getRidesByPassenger(String username);
}
