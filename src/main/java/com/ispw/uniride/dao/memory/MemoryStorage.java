package com.ispw.uniride.dao.memory;

import com.ispw.uniride.model.Ride;
import com.ispw.uniride.model.Student;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton In-Memory storage simulation.
 */
public class MemoryStorage {
    private static MemoryStorage instance = null;

    private Map<String, Student> studentsMap;
    private Map<String, Ride> ridesMap;

    private MemoryStorage() {
        studentsMap = new HashMap<>();
        ridesMap = new HashMap<>();
        // Preload some mock data (con posizione dichiarata di esempio, per mostrare da subito
        // i suggerimenti di vicinanza in Offri/Cerca Passaggio senza dover registrare un nuovo utente)
        studentsMap.put("mario.rossi", new Student("mario.rossi", "[HASHED]password", "Mario Rossi", "Palestrina"));
        studentsMap.put("luigi.verdi", new Student("luigi.verdi", "[HASHED]password", "Luigi Verdi", "Milano"));
    }

    public static synchronized MemoryStorage getInstance() {
        if (instance == null) {
            instance = new MemoryStorage();
        }
        return instance;
    }

    public Map<String, Student> getStudentsMap() { return studentsMap; }
    public Map<String, Ride> getRidesMap() { return ridesMap; }
    public List<Ride> getRidesList() { return new ArrayList<>(ridesMap.values()); }
}
