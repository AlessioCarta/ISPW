package com.ispw.uniride.dao.memory;

import com.ispw.uniride.dao.StudentDAO;
import com.ispw.uniride.model.Student;

/**
 * Implementazione concreta di {@link StudentDAO} per la famiglia In-Memory.
 * Delega tutto alla HashMap condivisa in {@link MemoryStorage}, indicizzata per username
 * (che funge da chiave primaria logica dello Studente).
 */
public class MemoryStudentDAO implements StudentDAO {
    // Riferimento al Singleton condiviso con le altre famiglie di DAO In-Memory.
    private MemoryStorage storage;

    public MemoryStudentDAO() {
        this.storage = MemoryStorage.getInstance();
    }

    @Override
    public void saveStudent(Student student) {
        // put() con chiave = username: se lo username esiste già, lo sovrascrive (upsert).
        storage.getStudentsMap().put(student.getUsername(), student);
    }

    @Override
    public Student getStudentByUsername(String username) {
        // Ritorna null se nessuno studente è registrato con quello username: LoginController
        // interpreta il null come "utente non trovato".
        return storage.getStudentsMap().get(username);
    }
}
