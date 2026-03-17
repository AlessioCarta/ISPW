package com.ispw.uniride.model.strategy;

/**
 * Implementazione concreta dello Strategy Pattern.
 * Calcola la stima dei costi ripartendola in porzioni logicamente uguali
 * sia tra il conducente dell'auto, sia tra tutti gli ospiti a bordo.
 */
public class EqualSplitStrategy implements CostStrategy {

    /**
     * @param basePrice Costo globale stimato per l'intero viaggio.
     * @param numPassengers i passeggeri saliti a bordo.
     * @return Costo di base diviso equamente per il numero di passeggeri e il +1 del guidatore.
     */
    @Override
    public double calculateCost(double basePrice, int numPassengers) {
        // Prevenzione matematica (se negativo torna al costo originale)
        if (numPassengers < 0) return basePrice;

        // Divisione logica su "Tutti"
        return basePrice / (numPassengers + 1.0);
    }
}
