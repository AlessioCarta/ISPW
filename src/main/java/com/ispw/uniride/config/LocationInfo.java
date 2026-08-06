package com.ispw.uniride.config;

/**
 * Voce statica del {@link LocationCatalog}: rappresenta un comune/paese selezionabile
 * nelle ComboBox di UniRide, con le coordinate geografiche approssimate necessarie
 * a calcolare la vicinanza rispetto alla posizione dichiarata dallo studente.
 * <p>
 * Implementato come {@code record} in quanto valore immutabile a sola lettura, senza
 * comportamento oltre l'esposizione dei propri campi (nessuna logica di identità/uguaglianza
 * personalizzata necessaria oltre a quella generata automaticamente).
 *
 * @param name il nome della località (es. "Roma", "Palestrina").
 * @param latitude latitudine approssimata del centro abitato, in gradi decimali.
 * @param longitude longitudine approssimata del centro abitato, in gradi decimali.
 * @param universityCity {@code true} se nella località è presente un polo universitario di
 * rilievo: usato per suggerire automaticamente la destinazione più plausibile in
 * "Offri/Cerca Passaggio".
 */
public record LocationInfo(String name, double latitude, double longitude, boolean universityCity) {
}
