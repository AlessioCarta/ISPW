package com.ispw.uniride;

import com.ispw.uniride.dao.StudentDAO;
import com.ispw.uniride.dao.sql.JdbcStudentDAO;
import com.ispw.uniride.model.Student;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite di Unit Testing (JUnit 5) per {@code JdbcStudentDAO}.
 */
class JdbcStudentDAOTest {

    private final StudentDAO dao = new JdbcStudentDAO();

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

    @Test
    void testSaveStudentTwiceUpdatesInsteadOfDuplicating() {
        String username = unique("aggiornato");
        dao.saveStudent(new Student(username, "[HASHED]vecchia", "Nome Vecchio", null));
        dao.saveStudent(new Student(username, "[HASHED]nuova", "Nome Nuovo", "Roma"));

        Student found = dao.getStudentByUsername(username);
        assertEquals("Nome Nuovo", found.getFullName());
        assertEquals("Roma", found.getHomeLocation());
    }
}
