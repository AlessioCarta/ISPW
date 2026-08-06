package com.ispw.uniride;

import com.ispw.uniride.model.strategy.DistanceCostStrategy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Suite di Test per validare le regole di calcolo imposte dalla Strategy alternativa
 * basata su un ricarico a favore del guidatore.
 */
class DistanceCostStrategyTest {

    private final DistanceCostStrategy strategy = new DistanceCostStrategy();

    /**
     * I passeggeri si accollano l'80% del costo base, diviso in parti uguali fra loro.
     */
    @Test
    void testCalculateCostSplitsEightyPercentAmongPassengers() {
        double cost = strategy.calculateCost(100.0, 4);
        assertEquals(20.0, cost, "80,00 di quota passeggeri diviso 4 fa 20,00 a testa");
    }

    /**
     * Senza passeggeri il costo intero resta a carico del guidatore (nessuna divisione).
     */
    @Test
    void testCalculateCostWithNoPassengersReturnsFullBasePrice() {
        double cost = strategy.calculateCost(50.0, 0);
        assertEquals(50.0, cost, "Senza passeggeri il guidatore sostiene l'intera spesa");
    }

    /**
     * Con un solo passeggero, questo si accolla l'80% del costo base per intero.
     */
    @Test
    void testCalculateCostWithSinglePassenger() {
        double cost = strategy.calculateCost(30.0, 1);
        assertEquals(24.0, cost, "80% di 30,00 a carico dell'unico passeggero");
    }
}
