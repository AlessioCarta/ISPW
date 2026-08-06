package com.ispw.uniride.bean;

/**
 * Data Transfer Object (DTO) per una singola richiesta/prenotazione, arricchita con i dati
 * del passaggio a cui si riferisce, pronta per essere mostrata dalla Boundary grafica senza
 * dover esporre direttamente le Entity {@code Booking}/{@code Ride}.
 */
public class BookingBean {
    private String bookingId;
    private String rideId;
    private String departure;
    private String destination;
    private String date;

    // Nome della "controparte": per il guidatore è il passeggero richiedente, per il
    // passeggero è il guidatore del passaggio.
    private String counterpartName;

    // Stato della richiesta: REQUESTED, CONFIRMED, REJECTED o CANCELLED (vedi Booking).
    private String state;

    public BookingBean(String bookingId, String rideId, String departure, String destination,
                        String date, String counterpartName, String state) {
        this.bookingId = bookingId;
        this.rideId = rideId;
        this.departure = departure;
        this.destination = destination;
        this.date = date;
        this.counterpartName = counterpartName;
        this.state = state;
    }

    public String getBookingId() { return bookingId; }
    public String getRideId() { return rideId; }
    public String getDeparture() { return departure; }
    public String getDestination() { return destination; }
    public String getDate() { return date; }
    public String getCounterpartName() { return counterpartName; }
    public String getState() { return state; }
}
