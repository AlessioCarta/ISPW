package com.ispw.uniride.model.strategy;

/**
 * Calculates cost by splitting the base price equally among all passengers
 * plus the driver (numPassengers + 1).
 */
public class EqualSplitStrategy implements CostStrategy {
    @Override
    public double calculateCost(double basePrice, int numPassengers) {
        if (numPassengers < 0) return basePrice;
        return basePrice / (numPassengers + 1.0);
    }
}
