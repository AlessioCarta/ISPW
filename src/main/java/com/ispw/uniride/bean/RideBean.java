package com.ispw.uniride.bean;

/**
 * Data Transfer Object (DTO) destinato a trasportare logicamente informazioni
 * riguardanti un Passaggio (Ride) tra i confini architetturali (ad es. da Controller Applicativo alla Boundary).
 * Utilizzato per schermare il modulo del Dominio interno dell'Applicativo ed esporre alla UI grafica soltanto
 * i dati sicuri.
 */
public class RideBean {
    private String id;
    private String departure;
    private String destination;
    private String date;
    // Orario di partenza deciso dal guidatore (formato "HH:mm"), non sensibile: visibile a
    // chiunque veda il passaggio (elenco ricerca compreso), a differenza del numero di telefono.
    private String departureTime;
    private int totalSeats;
    private double basePrice;

    // Campi calcolati a runtime e letti esclusivamente in output (visualizzazione dashboard)
    private int availableSeats;
    private String status;
    private String driver;
    private double computedPrice;

    /**
     * Costruttore DTO parziale per il caso d'uso "Offri Passaggio".
     * Il boundary grafico racchiude qui tutti i campi compilati prima di passarli al controller
     * dedicato alla loro validazione.
     * @param departure la città o l'indirizzo dal quale si intende partire.
     * @param destination la destinazione preimpostata per questo tragitto carpooling.
     * @param date momento in cui il guidatore organizza la partenza (formato gg/mm/aaaa o custom).
     * @param departureTime l'orario di partenza deciso dal guidatore (formato "HH:mm").
     * @param totalSeats posti a sedere fisici della vettura (eccetto l'autista).
     * @param basePrice costo puro di partenza per benzina, usura o pedaggi da coprire globalmente.
     */
    public RideBean(String departure, String destination, String date, String departureTime, int totalSeats, double basePrice) {
        this.departure = departure;
        this.destination = destination;
        this.date = date;
        this.departureTime = departureTime;
        this.totalSeats = totalSeats;
        this.basePrice = basePrice;
    }

    /**
     * Costruttore DTO completo per il caso d'uso "Ricerca Passaggio" o "Elenco dashboard".
     * In questo caso la logica applicativa ha già interrogato il Database e lo incapsula in
     * un oggetto fittizio contenente tutti gli stati generati (come id o disponibili).
     * @param id identificativo univoco assegnato (es. hash in csv o in RAM) alla Ride.
     * @param driver nome autista assegnato alla sessione corrente della creazione.
     * @param departure l'origine prefissata dal conducente.
     * @param destination meta stabilita del carpooling.
     * @param date la schedulazione testuale o oggettuale indicante il giorno d'avvio.
     * @param departureTime l'orario di partenza deciso dal guidatore (formato "HH:mm"), o {@code null}.
     * @param totalSeats i posti a sedere massimi inseriti alla creazione.
     * @param availableSeats il conteggio dei passeggeri in tempo reale rispetto agli slot vuoti.
     * @param basePrice spesa base totale che sarà successivamente elaborata dallo Strategy pattern.
     * @param status stringa testuale equivalente allo stato della macchina (AVAILABLE o FULL).
     */
    // I 10 parametri sono tutti campi primitivi/String indipendenti di sola visualizzazione (una
    // riga della ListView): introdurre un oggetto Builder o un value-object annidato avrebbe
    // aggiunto complessità senza reali benefici per un semplice DTO come questo.
    @SuppressWarnings("java:S107")
    public RideBean(String id, String driver, String departure, String destination, String date, String departureTime, int totalSeats, int availableSeats, double basePrice, String status) {
        this.id = id;
        this.driver = driver;
        this.departure = departure;
        this.destination = destination;
        this.date = date;
        this.departureTime = departureTime;
        this.totalSeats = totalSeats;
        this.availableSeats = availableSeats;
        this.basePrice = basePrice;
        this.status = status;
    }

    // Boilerplate getter ed event-setter incapsulati per mascherare i campi originali
    public String getId() { return id; }
    public String getDeparture() { return departure; }
    public String getDestination() { return destination; }
    public String getDate() { return date; }
    public String getDepartureTime() { return departureTime; }
    public int getTotalSeats() { return totalSeats; }
    public double getBasePrice() { return basePrice; }
    public int getAvailableSeats() { return availableSeats; }
    public String getStatus() { return status; }
    public String getDriver() { return driver; }
    public double getComputedPrice() { return computedPrice; }

    /**
     * @param computedPrice campo non dipendente dal database, viene popolato a runtime in fase
     * di visualizzazione in base al pattern Strategy.
     */
    public void setComputedPrice(double computedPrice) { this.computedPrice = computedPrice; }
}
