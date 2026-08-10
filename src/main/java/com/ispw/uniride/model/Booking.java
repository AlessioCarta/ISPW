package com.ispw.uniride.model;

import java.util.UUID;

/**
 * Entità che rappresenta la richiesta di un passeggero di unirsi a un {@link Ride}.
 * Il posto sulla vettura viene riservato immediatamente alla richiesta (tramite
 * {@code Ride.bookSeat}), ma la richiesta resta in stato {@code REQUESTED} finché il
 * guidatore non la conferma o la rifiuta esplicitamente: {@code Booking} modella soltanto
 * questo stato di approvazione "sopra" il posto già fisicamente riservato su {@code Ride}.
 */
public class Booking {

    /** Stato iniziale: richiesta inviata, in attesa della decisione del guidatore. */
    public static final String REQUESTED = "REQUESTED";
    /** Il guidatore ha accettato: il posto già riservato resta confermato. */
    public static final String CONFIRMED = "CONFIRMED";
    /** Il guidatore ha rifiutato: il posto va liberato su {@code Ride}. */
    public static final String REJECTED = "REJECTED";
    /** Il passeggero ha annullato di propria iniziativa: il posto va liberato su {@code Ride}. */
    public static final String CANCELLED = "CANCELLED";

    private String id;
    private String rideId;
    private String passengerUsername;
    private String state;

    /**
     * Crea una nuova richiesta di prenotazione, generando un identificativo univoco e
     * impostando lo stato iniziale a {@link #REQUESTED}.
     * @param rideId identificativo del passaggio su cui è stato riservato il posto.
     * @param passengerUsername lo username del passeggero richiedente.
     */
    public Booking(String rideId, String passengerUsername) {
        // Id generato internamente, mai passato dall'esterno: garantisce unicità senza
        // affidarsi a un contatore condiviso o alla persistenza.
        this.id = UUID.randomUUID().toString();
        this.rideId = rideId;
        this.passengerUsername = passengerUsername;
        // Ogni nuova richiesta nasce sempre in attesa di decisione: non esiste un percorso
        // di creazione che salti direttamente a CONFIRMED/REJECTED.
        this.state = REQUESTED;
    }

    // Getter e setter di base.
    public String getId() { return id; }
    // Usato dai DAO in fase di ricostruzione dell'oggetto da storage.
    public void setId(String id) { this.id = id; }
    public String getRideId() { return rideId; }
    public String getPassengerUsername() { return passengerUsername; }
    public String getState() { return state; }

    /**
     * Il guidatore accetta la richiesta: il posto, già riservato su {@code Ride}, resta tale.
     * @return {@code true} se la transizione è avvenuta, {@code false} se la richiesta non era più in {@code REQUESTED}.
     */
    public boolean confirm() {
        // Transizione ammessa solo da REQUESTED: non si può confermare una richiesta già
        // gestita (rifiutata, annullata) né una già confermata in precedenza.
        if (!REQUESTED.equals(state)) {
            return false;
        }
        this.state = CONFIRMED;
        return true;
    }

    /**
     * Il guidatore rifiuta la richiesta. Il chiamante è responsabile di liberare il posto
     * corrispondente su {@code Ride} (tramite {@code cancelSeat}) dopo questa chiamata.
     * @return {@code true} se la transizione è avvenuta, {@code false} se la richiesta non era più in {@code REQUESTED}.
     */
    public boolean reject() {
        // Stessa regola di confirm(): si rifiuta solo una richiesta ancora pendente.
        if (!REQUESTED.equals(state)) {
            return false;
        }
        this.state = REJECTED;
        return true;
    }

    /**
     * Il passeggero annulla la propria richiesta/prenotazione (sia essa ancora in attesa o già
     * confermata). Il chiamante è responsabile di liberare il posto su {@code Ride}.
     * @return {@code true} se la transizione è avvenuta, {@code false} se la prenotazione era già chiusa (rifiutata o annullata).
     */
    public boolean cancel() {
        // A differenza di confirm()/reject(), cancel() è ammesso da REQUESTED o CONFIRMED
        // (il passeggero può ripensarci anche dopo l'ok del guidatore): resta vietato solo
        // da uno stato già chiuso (REJECTED/CANCELLED), per non ri-eseguire una transizione finale.
        if (REJECTED.equals(state) || CANCELLED.equals(state)) {
            return false;
        }
        this.state = CANCELLED;
        return true;
    }
}
