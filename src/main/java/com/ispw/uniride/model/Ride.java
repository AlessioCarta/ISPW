package com.ispw.uniride.model;

import com.ispw.uniride.model.observer.Observer;
import com.ispw.uniride.model.observer.Subject;
import com.ispw.uniride.model.state.AvailableState;
import com.ispw.uniride.model.state.FullState;
import com.ispw.uniride.model.state.RideState;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entità concettuale che definisce un "Passaggio" offerto all'interno del sistema di Carpooling.
 * Implementa l'interfaccia Subject per il GoF Pattern Observer (pubblica eventi verso i passeggeri prenotati).
 * Mantiene il proprio stato ciclico tramite il GoF Pattern State (AvailableState, FullState).
 */
public class Ride implements Subject {

    // Identificativo automatico casuale
    private String id;

    // Username del guidatore (ForeignKey virtuale alla classe Student)
    private String driverUsername;

    // Attributi descrittivi del percorso e tempi
    private String departure;
    private String destination;
    private String date; // Data mantenuta testualmente per comodità prototipale

    // Statistiche posti a sedere autovettura
    private int totalSeats;
    private int availableSeats;

    // Costo stimato base della spesa del viaggio intero (benzina + autostrada)
    private double basePrice;

    // Riferimento di tipo State-Pattern per cambiare il comportamento in base allo stato
    private RideState state;

    // Lista di listener (Passeggeri) in ascolto di modifiche sul passaggio per l'Observer Pattern
    private List<Observer> passengers = new ArrayList<>();

    /**
     * Costruisce un Passaggio nuovo generando l'UUID interno ed impostando
     * i dati raccolti.
     * Inizializza automaticamente lo State iniziale in Available o Full.
     */
    public Ride(String driverUsername, String departure, String destination, String date, int totalSeats, double basePrice) {
        this.id = UUID.randomUUID().toString();
        this.driverUsername = driverUsername;
        this.departure = departure;
        this.destination = destination;
        this.date = date;
        this.totalSeats = totalSeats;
        this.availableSeats = totalSeats;
        this.basePrice = basePrice;

        // Assegnazione logica dello stato tramite il Design Pattern State
        this.state = (totalSeats > 0) ? new AvailableState() : new FullState();
    }

    // Boilerplate getter ed event-setter per la logica di dominio e serializzazione DAO
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

    // Metodi ponte per far evolvere lo stato dinamico del viaggio
    public void setState(RideState state) { this.state = state; }
    public RideState getState() { return state; }

    /**
     * @return Ritorna un identificativo stringa ricavandolo dall'istanza State concreta.
     */
    public String getStatus() { return state.getStatus(); }

    /**
     * Tenta di prenotare un posto per lo studente chiamante.
     * Delega l'azione di convalida alla classe di Stato in modo che questa classe (Subject)
     * non abbia if/else ciclomici (State Pattern). Se va a buon fine, si attacca all'Observer e lo notifica.
     * @param passenger L'entità (qui simulata come lambda callback) che desidera aderire e ricevere update
     * @return true se c'era un posto vuoto e lo status lo ha permesso.
     */
    public boolean bookSeat(Observer passenger) {
        boolean success = state.bookSeat(this);
        if (success) {
            attach(passenger);
            // Invia evento ai Listener/Observer
            notifyObservers("Un nuovo passeggero si è unito al passaggio da " + departure + " a " + destination);
        }
        return success;
    }

    /**
     * Pattern Observer: Aggiunge un abbonato/sottoscrittore.
     * @param observer l'entità che ha un metodo update da chiamare.
     */
    @Override
    public void attach(Observer observer) {
        if (!passengers.contains(observer)) {
            passengers.add(observer);
        }
    }

    /**
     * Pattern Observer: Toglie un abbonato dalla notifica broadcast.
     * @param observer l'entità abbonata da disiscrivere.
     */
    @Override
    public void detach(Observer observer) {
        passengers.remove(observer);
    }

    /**
     * Pattern Observer: Triggera la funzione di ricezione notifica per ogni passaggero.
     * @param message il testo dell'aggiornamento.
     */
    @Override
    public void notifyObservers(String message) {
        for (Observer obs : passengers) {
            obs.update(message);
        }
    }
}
