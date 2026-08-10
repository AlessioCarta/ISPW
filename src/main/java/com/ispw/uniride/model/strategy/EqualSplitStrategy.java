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
        // Prevenzione matematica: un numero di passeggeri negativo non ha senso nel dominio,
        // quindi non proviamo nemmeno a dividere e restituiamo il prezzo pieno.
        if (numPassengers < 0) return basePrice;

        // Divisione logica su "Tutti": il +1.0 rappresenta il guidatore stesso, che paga la
        // sua quota esattamente come ogni passeggero (uso di 1.0 e non 1 per forzare la
        // divisione in virgola mobile ed evitare un troncamento intero).
        return basePrice / (numPassengers + 1.0);
    }
}
