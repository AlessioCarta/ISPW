package com.ispw.uniride.bean;

public class RideBean {
    private String id;
    private String departure;
    private String destination;
    private String date;
    private int totalSeats;
    private double basePrice;

    // Output only
    private int availableSeats;
    private String status;
    private String driver;
    private double computedPrice;

    // For creating new ride
    public RideBean(String departure, String destination, String date, int totalSeats, double basePrice) {
        this.departure = departure;
        this.destination = destination;
        this.date = date;
        this.totalSeats = totalSeats;
        this.basePrice = basePrice;
    }

    // For retrieving rides
    public RideBean(String id, String driver, String departure, String destination, String date, int totalSeats, int availableSeats, double basePrice, String status) {
        this.id = id;
        this.driver = driver;
        this.departure = departure;
        this.destination = destination;
        this.date = date;
        this.totalSeats = totalSeats;
        this.availableSeats = availableSeats;
        this.basePrice = basePrice;
        this.status = status;
    }

    public String getId() { return id; }
    public String getDeparture() { return departure; }
    public String getDestination() { return destination; }
    public String getDate() { return date; }
    public int getTotalSeats() { return totalSeats; }
    public double getBasePrice() { return basePrice; }
    public int getAvailableSeats() { return availableSeats; }
    public String getStatus() { return status; }
    public String getDriver() { return driver; }
    public double getComputedPrice() { return computedPrice; }
    public void setComputedPrice(double computedPrice) { this.computedPrice = computedPrice; }
}
