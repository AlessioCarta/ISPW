package com.ispw.uniride.dao.memory;

import com.ispw.uniride.model.Booking;
import com.ispw.uniride.model.Ride;
import com.ispw.uniride.model.Student;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton In-Memory storage simulation. Contiene le 3 HashMap condivise da tutta la
 * famiglia {@code dao.memory}: ogni Memory*DAO legge/scrive qui invece di avere uno stato
 * proprio, così che i dati restino coerenti fra le diverse interfacce DAO nella stessa sessione
 * applicativa (es. un Ride salvato tramite RideDAO è visibile anche da BookingDAO).
 */
public class MemoryStorage {
    // Unica istanza (Pattern Singleton): tutte le famiglie Memory*DAO condividono questi dati.
    private static MemoryStorage instance = null;

    // Le tre "tabelle" in RAM, indicizzate per chiave primaria logica (username o id) per un
    // accesso a tempo costante invece di scorrere liste.
    private Map<String, Student> studentsMap;
    private Map<String, Ride> ridesMap;
    private Map<String, Booking> bookingsMap;

    private MemoryStorage() {
        // Inizializzazione delle mappe vuote alla prima (e unica) creazione dell'istanza.
        studentsMap = new HashMap<>();
        ridesMap = new HashMap<>();
        bookingsMap = new HashMap<>();
        // Preload some mock data (con posizione dichiarata di esempio, per mostrare da subito
        // i suggerimenti di vicinanza in Offri/Cerca Passaggio senza dover registrare un nuovo utente).
        // Il prefisso "[HASHED]" è solo un segnaposto testuale (non un vero hash crittografico),
        // sufficiente per il livello prototipale di autenticazione richiesto dal corso.
        // Entrambi gli utenti demo sono nella stessa area (Lazio, Prenestino/Castelli Romani):
        // così anche i suggerimenti di vicinanza del secondo utente (passeggero) restano coerenti
        // con la tratta Palestrina -> Roma offerta dal primo, utile per demo/registrazioni video.
        studentsMap.put("mario.rossi", new Student("mario.rossi", "[HASHED]password", "Mario Rossi", "Palestrina"));
        studentsMap.put("luigi.verdi", new Student("luigi.verdi", "[HASHED]password", "Luigi Verdi", "Frascati"));
    }

    public static synchronized MemoryStorage getInstance() {
        // Lazy initialization: la prima chiamata crea l'istanza (con i due utenti demo),
        // le successive ritornano sempre la stessa, mantenendo lo stato accumulato nel tempo.
        if (instance == null) {
            instance = new MemoryStorage();
        }
        return instance;
    }

    // Accesso diretto alle mappe per i Memory*DAO (stesso package): usato per put/get/remove
    // sulle singole entità tramite la loro chiave.
    public Map<String, Student> getStudentsMap() { return studentsMap; }
    public Map<String, Ride> getRidesMap() { return ridesMap; }
    // Copia difensiva in una nuova ArrayList: chi riceve la lista non può alterare la mappa
    // originale aggiungendo/rimuovendo elementi dalla lista restituita.
    public List<Ride> getRidesList() { return new ArrayList<>(ridesMap.values()); }
    public Map<String, Booking> getBookingsMap() { return bookingsMap; }
    public List<Booking> getBookingsList() { return new ArrayList<>(bookingsMap.values()); }
}
