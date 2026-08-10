package com.ispw.uniride.dao.memory;

import com.ispw.uniride.dao.RideDAO;
import com.ispw.uniride.model.Ride;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementazione concreta di {@link RideDAO} per la famiglia In-Memory (Pattern Abstract
 * Factory: prodotta da {@link MemoryDAOFactory}). Non ha stato proprio: legge e scrive sempre
 * sulla HashMap condivisa esposta da {@link MemoryStorage} (Singleton), così tutte le istanze
 * di questo DAO nell'applicazione vedono sempre gli stessi dati.
 */
public class MemoryRideDAO implements RideDAO {
    // Riferimento al Singleton condiviso: non una copia dei dati, ma l'accesso alla stessa
    // mappa usata da ogni altra istanza di MemoryRideDAO nell'applicazione.
    private MemoryStorage storage;

    public MemoryRideDAO() {
        this.storage = MemoryStorage.getInstance();
    }

    @Override
    public void saveRide(Ride ride) {
        // put() con la stessa id sovrascrive: comportamento equivalente a un "upsert" naturale
        // delle HashMap, non serve distinguere insert da update come nel caso SQL.
        storage.getRidesMap().put(ride.getId(), ride);
    }

    @Override
    public List<Ride> getAllRides() {
        return storage.getRidesList();
    }

    @Override
    public List<Ride> getAvailableRides(String departure, String destination) {
        // Scansione lineare di tutti i Ride: accettabile per una simulazione in RAM con
        // volumi di dati ridotti, non pensata per scalare a grandi quantità di record.
        List<Ride> availableRides = new ArrayList<>();
        for (Ride ride : storage.getRidesList()) {
            // Filtro combinato: lo stato deve essere AVAILABLE (posti liberi) E la tratta deve
            // combaciare esattamente (ignorando maiuscole/minuscole) con quella cercata.
            if ("AVAILABLE".equals(ride.getStatus()) &&
                ride.getDeparture().equalsIgnoreCase(departure) &&
                ride.getDestination().equalsIgnoreCase(destination)) {
                availableRides.add(ride);
            }
        }
        return availableRides;
    }

    @Override
    public Ride getRideById(String id) {
        // get() su HashMap: ritorna null se l'id non esiste, comportamento coerente col
        // contratto dichiarato in RideDAO.
        return storage.getRidesMap().get(id);
    }

    @Override
    public void updateRide(Ride ride) {
        // Stessa identica operazione di saveRide(): in una HashMap "salvare" e "aggiornare"
        // sono la stessa put(), a differenza della sintassi SQL dove servono istruzioni diverse.
        storage.getRidesMap().put(ride.getId(), ride);
    }

    @Override
    public List<Ride> getRidesByDriver(String username) {
        // Trova tutti i passaggi in cui l'utente è il guidatore (per la vista "Le mie corse offerte").
        List<Ride> result = new ArrayList<>();
        for (Ride ride : storage.getRidesList()) {
            if (ride.getDriverUsername().equals(username)) {
                result.add(ride);
            }
        }
        return result;
    }

    @Override
    public List<Ride> getRidesByPassenger(String username) {
        // Trova tutti i passaggi in cui l'utente compare nella lista passeggeri (per la vista
        // "Le mie prenotazioni").
        List<Ride> result = new ArrayList<>();
        for (Ride ride : storage.getRidesList()) {
            if (ride.getPassengerUsernames().contains(username)) {
                result.add(ride);
            }
        }
        return result;
    }

    @Override
    public void deleteRide(String id) {
        // Rimozione fisica dalla mappa: usata solo quando un Ride viene eliminato del tutto
        // (percorso oggi non più il principale, sostituito da cancel()/complete() sullo State,
        // ma mantenuto nell'interfaccia per compatibilità).
        storage.getRidesMap().remove(id);
    }
}
