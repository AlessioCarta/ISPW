package com.ispw.uniride;

import com.ispw.uniride.dao.StudentDAO;
import com.ispw.uniride.dao.memory.MemoryStudentDAO;
import com.ispw.uniride.model.Student;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite di Unit Testing (JUnit 5) per {@code MemoryStudentDAO}.
 * @author Alessio Carta
 */
class MemoryStudentDAOTest {

    private final StudentDAO dao = new MemoryStudentDAO();

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
    void testGetStudentByUsernameReturnsNullWhenNotFound() {
        assertNull(dao.getStudentByUsername(unique("inesistente")));
    }
}
