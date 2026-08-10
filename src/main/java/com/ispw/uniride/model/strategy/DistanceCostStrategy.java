package com.ispw.uniride.model.strategy;

/**
 * Nuova strategia che potrebbe essere iniettata al posto della EqualSplitStrategy
 * per favorire chi guida l'auto addebitando un piccolo ricarico a ciascun passeggero.
 */
public class DistanceCostStrategy implements CostStrategy {
    @Override
    public double calculateCost(double basePrice, int numPassengers) {
        // Senza passeggeri non c'è nulla da dividere: il guidatore sosterrebbe l'intera spesa,
        // ma restituiamo comunque basePrice per evitare una divisione per zero a valle.
        if (numPassengers <= 0) return basePrice;

        // Esempio mock di calcolo: i passeggeri si accollano l'80% della spesa (il guidatore
        // paga solo il restante 20% come compenso per l'usura dell'auto), suddiviso in parti
        // uguali fra tutti i passeggeri presenti.
        double quotaPasseggeri = basePrice * 0.8;
        return quotaPasseggeri / numPassengers;
    }
}
