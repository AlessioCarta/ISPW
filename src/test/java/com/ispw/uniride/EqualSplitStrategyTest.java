package com.ispw.uniride;

import com.ispw.uniride.model.strategy.EqualSplitStrategy;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Suite di Test per validare le regole di calcolo imposte dallo Strategy Pattern.
 */
class EqualSplitStrategyTest {

    /**
     * Sottopone allo Strategy vari casi d'uso simulando preventivi
     * su base equipartita dei costi (Equal Split).
     */
    @Test
    void testCalculateCostSplitEqually() {
        EqualSplitStrategy strategy = new EqualSplitStrategy();

        // Scenario 1: Costo base di 100,00 diviso con 3 ospiti (per un totale di 4 quote)
        double cost = strategy.calculateCost(100.0, 3);
        assertEquals(25.0, cost, "La spesa andrebbe suddivisa per 4 (3 ospiti + 1 guidatore = 25.0 euro a testa)");

        // Scenario 2: Tragitto solitario a 50,00. Nessun passeggero.
        cost = strategy.calculateCost(50.0, 0);
        assertEquals(50.0, cost, "Il conducente sostiene tutta la spesa per sé se viaggia vuoto.");

        // Scenario 3: Condivisione dei costi 50-50 per un viaggio a 120,00 (1 passeggero)
        cost = strategy.calculateCost(120.0, 1);
        assertEquals(60.0, cost, "La divisione perfetta 1+1 dovrebbe risultare in due metà");
    }
}
