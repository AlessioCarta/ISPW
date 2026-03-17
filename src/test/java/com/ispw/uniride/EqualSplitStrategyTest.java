package com.ispw.uniride;

import com.ispw.uniride.model.strategy.EqualSplitStrategy;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EqualSplitStrategyTest {

    @Test
    void testCalculateCostSplitEqually() {
        EqualSplitStrategy strategy = new EqualSplitStrategy();

        // Base Price 100, 3 Passengers (total 4 people splitting)
        double cost = strategy.calculateCost(100.0, 3);
        assertEquals(25.0, cost, "Cost should be divided by 4 (3 passengers + 1 driver)");

        // Base Price 50, 0 Passengers (driver pays full)
        cost = strategy.calculateCost(50.0, 0);
        assertEquals(50.0, cost, "Driver pays full cost if no passengers");

        // Base Price 120, 1 Passenger (split in half)
        cost = strategy.calculateCost(120.0, 1);
        assertEquals(60.0, cost, "Cost should be split evenly between driver and passenger");
    }
}
