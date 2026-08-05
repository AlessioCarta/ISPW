package com.ispw.uniride.config;

/**
 * Voce statica del {@link LocationCatalog}: rappresenta un comune/paese selezionabile
 * nelle ComboBox di UniRide, con le coordinate geografiche approssimate necessarie
 * a calcolare la vicinanza rispetto alla posizione dichiarata dallo studente.
 */
public final class LocationInfo {

    private final String name;
    private final double latitude;
    private final double longitude;

    // Vero se nella località è presente un polo universitario di rilievo: usato per
    // suggerire automaticamente la destinazione più plausibile in "Offri/Cerca Passaggio".
    private final boolean universityCity;

    public LocationInfo(String name, double latitude, double longitude, boolean universityCity) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.universityCity = universityCity;
    }

    public String getName() { return name; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public boolean isUniversityCity() { return universityCity; }
}
