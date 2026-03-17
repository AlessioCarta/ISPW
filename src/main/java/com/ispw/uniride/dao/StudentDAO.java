package com.ispw.uniride.dao;

import com.ispw.uniride.model.Student;

/**
 * Livello astratto di interfaccia al DBMS per gli Studenti universitari, utilizzato nel Pattern DAO.
 * Consente alle classi esterne di richiedere/inserire attori in maniera "technology-agnostic".
 */
public interface StudentDAO {

    /**
     * Inserisce in maniera permanente l'entità Student (e.g., durante la registrazione vera e propria,
     * non ancora formalmente esposta nella UI).
     * @param student l'attore appena creato.
     */
    void saveStudent(Student student);

    /**
     * Simula una query di SELECT * WHERE username = ?, fungendo da chiave primaria.
     * Metodo indispensabile per la validazione della logica in LoginController.
     * @param username stringa da cercare negli storage.
     * @return Studente trovato in archivio, null in caso d'assenza.
     */
    Student getStudentByUsername(String username);
}
