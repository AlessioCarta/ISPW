package com.ispw.uniride.controller;

import com.ispw.uniride.bean.RideBean;
import com.ispw.uniride.dao.DAOFactory;
import com.ispw.uniride.dao.RideDAO;
import com.ispw.uniride.exceptions.RideActionException;
import com.ispw.uniride.exceptions.UserNotAuthorizedException;
import com.ispw.uniride.model.Ride;
import com.ispw.uniride.model.Student;

import java.util.regex.Pattern;

/**
 * Controller logico dedicato alla registrazione formale (inserimento/creazione)
 * di nuove offerte per passaggi (Use Case: "Offerta Passaggio").
 * Rientra nello standard BCE.
 */
public class OfferRideController {

    // Formato orario a 24 ore, ore 0-23 e minuti 00-59: lo zero iniziale sull'ora è accettato ma
    // non obbligatorio ("9:30" e "09:30" sono entrambi validi), perché è la digitazione più
    // naturale per chi scrive un orario senza guardare il prompt di esempio. Il valore viene poi
    // sempre normalizzato a due cifre da normalizeDepartureTime prima di essere persistito, così
    // che liste e ricerche mostrino sempre lo stesso formato "HH:mm".
    private static final Pattern TIME_PATTERN = Pattern.compile("^([01]?\\d|2[0-3]):[0-5]\\d$");

    /**
     * Valida un nuovo tragitto proposto convertendo le specifiche del DTO `RideBean` in
     * un'istanza dell'entità Dominio vera e propria, gestendo le logiche implicite di collegamento utente.
     * @param rideBean contenitore base con parametri di viaggio inviati dalle View/CLI GUI.
     * @throws UserNotAuthorizedException se manca l'autenticazione.
     * @throws RideActionException se i dati del tragitto non sono logicamente validi (es. partenza e
     * destinazione coincidenti, posti o costo non positivi).
     */
    public void offerRide(RideBean rideBean) throws UserNotAuthorizedException, RideActionException {

        // Autenticazione: preleva la sessione creata in precedenza per certificare
        // a quale driver o entità "Student" apparterrà l'oggetto "Ride" appena istanziato.
        Student loggedUser = Session.getInstance().getLoggedUser();
        if (loggedUser == null) {
            throw new UserNotAuthorizedException("Utente non loggato! Accesso Negato alle funzioni dell'App.");
        }

        validateRide(rideBean);

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
                rideBean.getBasePrice(),
                normalizeDepartureTime(rideBean.getDepartureTime())
        );

        // Operazione CRUD finale tramite lo Storage specificato dal file di configurazione
        rideDAO.saveRide(newRide);
    }

    /**
     * Controlli logici minimi di dominio sui dati del passaggio prima di persisterlo.
     * @throws RideActionException se una delle regole di business non è rispettata.
     */
    private void validateRide(RideBean rideBean) throws RideActionException {
        // Entrambi i campi obbligatori: null o stringa vuota/di soli spazi non sono ammessi.
        if (rideBean.getDeparture() == null || rideBean.getDeparture().trim().isEmpty()
                || rideBean.getDestination() == null || rideBean.getDestination().trim().isEmpty()) {
            throw new RideActionException("Partenza e destinazione sono obbligatorie.");
        }
        // Un passaggio con partenza e destinazione identiche non ha senso nel dominio.
        if (rideBean.getDeparture().trim().equalsIgnoreCase(rideBean.getDestination().trim())) {
            throw new RideActionException("Partenza e destinazione non possono coincidere.");
        }
        // Zero o meno posti renderebbero il passaggio inutilizzabile fin da subito.
        if (rideBean.getTotalSeats() <= 0) {
            throw new RideActionException("Il numero di posti deve essere maggiore di zero.");
        }
        // Un costo negativo non ha significato economico nel dominio del carpooling.
        if (rideBean.getBasePrice() < 0) {
            throw new RideActionException("Il costo base non può essere negativo.");
        }
        // L'orario di partenza è deciso dal guidatore ed è obbligatorio: i passeggeri lo vedono
        // già nell'elenco risultati, prima ancora di richiedere il passaggio.
        if (rideBean.getDepartureTime() == null || !TIME_PATTERN.matcher(rideBean.getDepartureTime().trim()).matches()) {
            throw new RideActionException("Inserisci un orario di partenza valido (formato HH:mm, es. 08:30).");
        }
    }

    /**
     * Riporta un orario già validato da {@link #TIME_PATTERN} alla forma canonica "HH:mm" (ora
     * sempre a due cifre), così che un guidatore che digita "9:30" e uno che digita "09:30"
     * producano lo stesso valore persistito, mostrato in modo coerente in ricerche e liste.
     * @param departureTime l'orario grezzo, già garantito conforme al pattern dal chiamante.
     */
    private String normalizeDepartureTime(String departureTime) {
        String trimmed = departureTime.trim();
        // Il pattern garantisce sempre un solo ":" con ore e minuti validi: split è quindi sicuro.
        String[] parts = trimmed.split(":");
        String hour = parts[0].length() == 1 ? "0" + parts[0] : parts[0];
        return hour + ":" + parts[1];
    }
}
