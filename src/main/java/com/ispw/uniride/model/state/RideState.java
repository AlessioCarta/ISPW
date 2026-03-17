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
     * @return una sigla (es. "AVAILABLE" o "FULL")
     */
    String getStatus();
}
