package com.ispw.uniride;

import com.ispw.uniride.dao.StudentDAO;
import com.ispw.uniride.dao.fs.FsStudentDAO;
import com.ispw.uniride.model.Student;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite di Unit Testing (JUnit 5) per {@code FsStudentDAO}: persistenza su file CSV reale
 * (students.csv), inclusa la retro-compatibilità del campo opzionale homeLocation.
 */
class FsStudentDAOTest {

    private final StudentDAO dao = new FsStudentDAO();

    private String unique(String prefix) {
        return prefix + "." + System.nanoTime();
    }

    @Test
    void testSaveAndGetStudentByUsername() {
        String username = unique("mario");
        Student student = new Student(username, "[HASHED]password", "Mario Rossi", "Palestrina");

        dao.saveStudent(student);
        Student found = dao.getStudentByUsername(username);

        assertNotNull(found);
        assertEquals("Mario Rossi", found.getFullName());
        assertEquals("Palestrina", found.getHomeLocation());
    }

    /**
     * Uno studente salvato senza posizione dichiarata deve restare leggibile con homeLocation nulla,
     * senza generare righe CSV malformate.
     */
    @Test
    void testSaveStudentWithoutHomeLocation() {
        String username = unique("senzaposizione");
        Student student = new Student(username, "[HASHED]password", "Senza Posizione", null);

        dao.saveStudent(student);
        Student found = dao.getStudentByUsername(username);

        assertNotNull(found);
        assertNull(found.getHomeLocation());
    }

    @Test
    void testGetStudentByUsernameReturnsNullWhenNotFound() {
        assertNull(dao.getStudentByUsername(unique("inesistente")));
    }
}
