package com.ispw.uniride.model.strategy;

public interface CostStrategy {
    double calculateCost(double basePrice, int numPassengers);
}
