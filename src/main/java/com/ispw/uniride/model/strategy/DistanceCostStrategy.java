package com.ispw.uniride.model.strategy;

/**
 * Nuova strategia che potrebbe essere iniettata al posto della EqualSplitStrategy
 * per favorire chi guida l'auto addebitando un piccolo ricarico a ciascun passeggero.
 */
public class DistanceCostStrategy implements CostStrategy {
    @Override
    public double calculateCost(double basePrice, int numPassengers) {
        if (numPassengers <= 0) return basePrice;

        // Esempio mock di calcolo: i passeggeri si accollano l'80% della spesa e il guidatore solo il 20% come compenso per usura auto.
        double quotaPasseggeri = basePrice * 0.8;
        return quotaPasseggeri / numPassengers;
    }
}
