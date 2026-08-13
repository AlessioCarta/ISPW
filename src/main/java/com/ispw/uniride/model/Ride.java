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
 * Mantiene il proprio stato ciclico tramite il GoF Pattern State (AvailableState, FullState,
 * CompletedState, CancelledState): nessuna logica if/else sullo stato vive in questa classe, è
 * sempre delegata all'implementazione concreta corrente di {@link RideState}.
 */
public class Ride implements Subject {

    // Identificativo automatico casuale (chiave primaria logica del passaggio, generato dal costruttore).
    private String id;

    // Username del guidatore (ForeignKey virtuale alla classe Student).
    private String driverUsername;

    // Attributi descrittivi del percorso e tempi.
    private String departure;
    private String destination;
    private String date; // Data mantenuta testualmente per comodità prototipale

    // Orario di partenza deciso dal guidatore (formato testuale "HH:mm", stesso approccio di
    // "date"), o {@code null} se non specificato (retro-compatibilità con passaggi creati prima
    // dell'introduzione di questo campo).
    private String departureTime;

    // Statistiche posti a sedere autovettura.
    private int totalSeats;
    private int availableSeats;

    // Costo stimato base della spesa del viaggio intero (benzina + autostrada).
    private double basePrice;

    // Riferimento di tipo State-Pattern per cambiare il comportamento in base allo stato.
    private RideState state;

    // Lista degli username degli studenti che hanno prenotato (per la logica di dominio e DAO).
    private List<String> passengerUsernames = new ArrayList<>();

    // Lista di listener in ascolto di modifiche sul passaggio per l'Observer Pattern.
    private List<Observer> observers = new ArrayList<>();

    /**
     * Costruttore breve mantenuto per compatibilità con il codice preesistente (test e chiamate
     * che non specificano un orario di partenza). L'orario resta non impostato.
     */
    public Ride(String driverUsername, String departure, String destination, String date, int totalSeats, double basePrice) {
        this(driverUsername, departure, destination, date, totalSeats, basePrice, null);
    }

    /**
     * Costruisce un Passaggio nuovo generando l'UUID interno ed impostando
     * i dati raccolti.
     * Inizializza automaticamente lo State iniziale in Available o Full.
     */
    public Ride(String driverUsername, String departure, String destination, String date, int totalSeats, double basePrice, String departureTime) {
        // Ogni Ride ha un id univoco generato internamente: chi crea l'oggetto non deve fornirlo.
        this.id = UUID.randomUUID().toString();
        this.driverUsername = driverUsername;
        this.departure = departure;
        this.destination = destination;
        this.date = date;
        this.departureTime = departureTime;
        this.totalSeats = totalSeats;
        // Alla nascita nessun posto è ancora stato prenotato: i posti liberi coincidono col totale.
        this.availableSeats = totalSeats;
        this.basePrice = basePrice;

        // Assegnazione logica dello stato iniziale tramite il Design Pattern State: se non ci sono
        // posti fin da subito (caso limite, es. totalSeats=0) si parte direttamente da FullState.
        this.state = (totalSeats > 0) ? new AvailableState() : new FullState();
    }

    // Boilerplate getter ed event-setter per la logica di dominio e serializzazione DAO.
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDriverUsername() { return driverUsername; }
    public String getDeparture() { return departure; }
    public String getDestination() { return destination; }
    public String getDate() { return date; }
    public String getDepartureTime() { return departureTime; }
    public int getTotalSeats() { return totalSeats; }
    public double getBasePrice() { return basePrice; }
    public int getAvailableSeats() { return availableSeats; }
    // Usato dai DAO in fase di ricostruzione dell'oggetto da storage (il costruttore pubblico
    // parte sempre da availableSeats = totalSeats, qui invece si ripristina il valore reale salvato).
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }

    public List<String> getPassengerUsernames() { return passengerUsernames; }
    // Usato dai DAO per ripopolare la lista passeggeri letta da storage senza passare da bookSeat().
    public void setPassengerUsernames(List<String> usernames) { this.passengerUsernames = usernames; }
    public void addPassengerUsername(String username) {
        // Evita duplicati: uno stesso username non deve comparire due volte nella lista passeggeri.
        if (!this.passengerUsernames.contains(username)) {
            this.passengerUsernames.add(username);
        }
    }

    // Metodi ponte per far evolvere lo stato dinamico del viaggio (usati anche dai DAO in lettura).
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
     * @param username l'identificativo reale del passeggero da registrare a sistema.
     * @return true se c'era un posto vuoto e lo status lo ha permesso.
     */
    public boolean bookSeat(Observer passenger, String username) {
        // La decisione se un posto sia disponibile spetta interamente allo stato corrente:
        // Ride non sa (e non deve sapere) quale sia la logica di AvailableState/FullState.
        boolean success = state.bookSeat(this);
        if (success) {
            // Solo se la prenotazione è realmente andata a buon fine registriamo l'observer e
            // il passeggero: niente iscrizioni "fantasma" su prenotazioni fallite.
            attach(passenger);
            addPassengerUsername(username);
            // Invia evento ai Listener/Observer (Pattern Observer).
            notifyObservers("Un nuovo passeggero si è unito al passaggio da " + departure + " a " + destination);
        }
        return success;
    }

    /**
     * Tenta di annullare una prenotazione. Nasconde il Pattern State all'esterno (Incapsulamento).
     * Se l'operazione ha successo, rimuove lo username e notifica gli altri passeggeri.
     * @param username l'utente che cancella la prenotazione.
     * @return true se il posto è stato liberato con successo.
     */
    public boolean cancelSeat(String username) {
        // Guardia preventiva: non ha senso liberare un posto per qualcuno che non l'aveva prenotato,
        // né delegare inutilmente allo State un'operazione destinata a fallire comunque.
        if (!passengerUsernames.contains(username)) {
            return false;
        }

        // La logica di "si può tornare indietro da questo stato?" resta nello State (es. da uno
        // stato terminale come CANCELLED/COMPLETED non è più permesso liberare posti).
        boolean success = state.cancelSeat(this);
        if (success) {
            passengerUsernames.remove(username);
            // In una implementazione reale cercheremmo anche di fare detach(observer) se possibile.
            notifyObservers("L'utente " + username + " ha annullato la sua prenotazione per il viaggio verso " + destination);
        }
        return success;
    }

    /**
     * Il guidatore segna il passaggio come concluso (viaggio avvenuto). Delega la validità
     * della transizione allo State corrente: non è ammesso da uno stato già terminale.
     * @return true se la transizione è avvenuta.
     */
    public boolean complete() {
        // Nessuna notifica agli observer qui: il completamento non richiede un'azione da parte
        // dei passeggeri, a differenza dell'annullamento (vedi cancel() sotto).
        return state.complete(this);
    }

    /**
     * Il guidatore annulla il passaggio, anche se ci sono già passeggeri prenotati.
     * Se l'annullamento va a buon fine, notifica tutti i passeggeri iscritti tramite l'Observer.
     * @return true se la transizione è avvenuta.
     */
    public boolean cancel() {
        boolean success = state.cancel(this);
        // Notifica solo se c'era effettivamente qualcuno da avvisare: evita un broadcast inutile
        // su una lista vuota di passeggeri.
        if (success && !passengerUsernames.isEmpty()) {
            notifyObservers("Il guidatore ha annullato il passaggio da " + departure + " a " + destination);
        }
        return success;
    }

    /**
     * Pattern Observer: Aggiunge un abbonato/sottoscrittore.
     * @param observer l'entità che ha un metodo update da chiamare.
     */
    @Override
    public void attach(Observer observer) {
        // Evita di registrare due volte lo stesso observer (doppia notifica per lo stesso evento).
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /**
     * Pattern Observer: Toglie un abbonato dalla notifica broadcast.
     * @param observer l'entità abbonata da disiscrivere.
     */
    @Override
    public void detach(Observer observer) {
        observers.remove(observer);
    }

    /**
     * Pattern Observer: Triggera la funzione di ricezione notifica per ogni passaggero.
     * @param message il testo dell'aggiornamento.
     */
    @Override
    public void notifyObservers(String message) {
        // Il Subject (Ride) non conosce il tipo concreto degli Observer: chiama solo il contratto
        // dell'interfaccia, esattamente il punto del Pattern Observer.
        for (Observer obs : observers) {
            obs.update(message);
        }
    }
}
