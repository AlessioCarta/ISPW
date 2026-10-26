package com.ispw.uniride.model.state;

import com.ispw.uniride.model.Ride;
import com.ispw.uniride.utils.LoggerCustom;

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

    /**
     * Ricostruisce lo stato concreto a partire dalla sigla persistita da un DAO (CSV o database),
     * che può salvare solo l'etichetta testuale e mai un'istanza Java. Unico punto di traduzione
     * condiviso da tutte le famiglie di DAO, invece di una copia per ciascuna.
     * Una sigla non riconosciuta ricade su {@link FullState}: scelta prudente (una corsa
     * "sconosciuta" non è prenotabile), ma segnalata nel log invece che in silenzio.
     */
    static RideState fromStatus(String status) {
        if (status == null) {
            status = "";
        }
        switch (status) {
            case "AVAILABLE":
                return new AvailableState();
            case "FULL":
                return new FullState();
            case "COMPLETED":
                return new CompletedState();
            case "CANCELLED":
                return new CancelledState();
            default:
                LoggerCustom.warning("Stato di corsa non riconosciuto \"" + status + "\": trattato come FULL.");
                return new FullState();
        }
    }
}
