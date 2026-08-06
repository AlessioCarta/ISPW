package com.ispw.uniride.model.state;

import com.ispw.uniride.model.Ride;

/**
 * Interfaccia base per il Design Pattern State.
 * Consente di incapsulare il comportamento dinamico di un oggetto Ride che varia
 * a seconda del suo stato (ad esempio se è pieno o se ha ancora posti).
 */
public interface RideState {

    /**
     * Tenta di scalare un posto libero dalla capienza dell'auto per la tratta selezionata.
     * @param ride il riferimento all'oggetto di contesto (il Passaggio su cui agire).
     * @return boolean positivo se la logica interna di quello stato lo permette.
     */
    boolean bookSeat(Ride ride);

    /**
     * Tenta di liberare un posto precedentemente prenotato sull'auto.
     * @param ride riferimento all'entità di passaggio bersaglio.
     * @return true se l'operazione di rollback ha esito positivo.
     */
    boolean cancelSeat(Ride ride);

    /**
     * Metodo testuale utile alle interfacce utente per renderizzare univocamente lo stato corrente.
     * @return una sigla (es. "AVAILABLE", "FULL", "COMPLETED" o "CANCELLED")
     */
    String getStatus();

    /**
     * Il guidatore segna il passaggio come concluso (viaggio avvenuto). Consentito solo dagli
     * stati attivi ({@code AVAILABLE}/{@code FULL}), mai da uno stato già terminale.
     * @param ride il riferimento all'oggetto di contesto.
     * @return true se la transizione verso {@code CompletedState} è avvenuta.
     */
    boolean complete(Ride ride);

    /**
     * Il guidatore annulla il passaggio. Consentito anche se ci sono già passeggeri prenotati
     * (a differenza della cancellazione/eliminazione fisica del record), mai da uno stato
     * già terminale. I passeggeri già a bordo vengono notificati tramite il Pattern Observer.
     * @param ride il riferimento all'oggetto di contesto.
     * @return true se la transizione verso {@code CancelledState} è avvenuta.
     */
    boolean cancel(Ride ride);
}
