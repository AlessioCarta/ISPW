package com.ispw.uniride.model;

import com.ispw.uniride.model.observer.Observer;
import com.ispw.uniride.model.observer.Subject;
import com.ispw.uniride.model.state.AvailableState;
import com.ispw.uniride.model.state.FullState;
import com.ispw.uniride.model.state.RideState;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Ride implements Subject {
    private String id;
    private String driverUsername;
    private String departure;
    private String destination;
    private String date; // Simple String for prototype
    private int totalSeats;
    private int availableSeats;
    private double basePrice;

    private RideState state;
    private List<Observer> passengers = new ArrayList<>();

    public Ride(String driverUsername, String departure, String destination, String date, int totalSeats, double basePrice) {
        this.id = UUID.randomUUID().toString();
        this.driverUsername = driverUsername;
        this.departure = departure;
        this.destination = destination;
        this.date = date;
        this.totalSeats = totalSeats;
        this.availableSeats = totalSeats;
        this.basePrice = basePrice;
        this.state = (totalSeats > 0) ? new AvailableState() : new FullState();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDriverUsername() { return driverUsername; }
    public String getDeparture() { return departure; }
    public String getDestination() { return destination; }
    public String getDate() { return date; }
    public int getTotalSeats() { return totalSeats; }
    public double getBasePrice() { return basePrice; }

    public int getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }

    public void setState(RideState state) { this.state = state; }
    public RideState getState() { return state; }
    public String getStatus() { return state.getStatus(); }

    public boolean bookSeat(Observer passenger) {
        boolean success = state.bookSeat(this);
        if (success) {
            attach(passenger);
            notifyObservers("A new passenger joined the ride from " + departure + " to " + destination);
        }
        return success;
    }

    @Override
    public void attach(Observer observer) {
        if (!passengers.contains(observer)) {
            passengers.add(observer);
        }
    }

    @Override
    public void detach(Observer observer) {
        passengers.remove(observer);
    }

    @Override
    public void notifyObservers(String message) {
        for (Observer obs : passengers) {
            obs.update(message);
        }
    }
}
