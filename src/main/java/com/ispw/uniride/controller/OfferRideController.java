package com.ispw.uniride.controller;

import com.ispw.uniride.bean.RideBean;
import com.ispw.uniride.dao.DAOFactory;
import com.ispw.uniride.dao.RideDAO;
import com.ispw.uniride.exceptions.UserNotAuthorizedException;
import com.ispw.uniride.model.Ride;
import com.ispw.uniride.model.Student;

/**
 * Controller logico dedicato alla registrazione formale (inserimento/creazione)
 * di nuove offerte per passaggi (Use Case: "Offerta Passaggio").
 * Rientra nello standard BCE.
 */
public class OfferRideController {

    /**
     * Valida un nuovo tragitto proposto convertendo le specifiche del DTO `RideBean` in
     * un'istanza dell'entità Dominio vera e propria, gestendo le logiche implicite di collegamento utente.
     * @param rideBean contenitore base con parametri di viaggio inviati dalle View/CLI GUI.
     * @throws UserNotAuthorizedException se manca l'autenticazione.
     */
    public void offerRide(RideBean rideBean) throws UserNotAuthorizedException {

        // Autenticazione: preleva la sessione creata in precedenza per certificare
        // a quale driver o entità "Student" apparterrà l'oggetto "Ride" appena istanziato.
        Student loggedUser = Session.getInstance().getLoggedUser();
        if (loggedUser == null) {
            throw new UserNotAuthorizedException("Utente non loggato! Accesso Negato alle funzioni dell'App.");
        }

        // Il livello DAO si svincola dalle scelte implementative estraendo dinamicamente in Factory la gestione
        RideDAO rideDAO = DAOFactory.getInstance().getRideDAO();

        // Trasformazione concettuale DTO (Bean) -> Entità di Business Logic e dominio (Ride Object)
        // La generazione interna dell'ID è responsabilità unica del costruttore Ride().
        Ride newRide = new Ride(
                loggedUser.getUsername(),
                rideBean.getDeparture(),
                rideBean.getDestination(),
                rideBean.getDate(),
                rideBean.getTotalSeats(),
                rideBean.getBasePrice()
        );

        // Operazione CRUD finale tramite lo Storage specificato dal file di configurazione
        rideDAO.saveRide(newRide);
    }
}
