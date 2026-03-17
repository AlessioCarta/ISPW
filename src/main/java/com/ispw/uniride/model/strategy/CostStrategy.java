package com.ispw.uniride.model.strategy;

/**
 * Interfaccia comune per il GoF Strategy Pattern sul calcolo dinamico dei costi.
 * Consente di incapsulare l'algoritmo matematico scelto per la suddivisione o
 * gestione di un costo totale.
 */
public interface CostStrategy {

    /**
     * Esegue l'algoritmo specifico implementato.
     * @param basePrice Costo globale stimato dal guidatore per coprire benzina/pedaggi.
     * @param numPassengers Quantità attiva di persone trasportate (escluso il conducente).
     * @return Costo pro-capite o parziale elaborato in double.
     */
    double calculateCost(double basePrice, int numPassengers);
}
