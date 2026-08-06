package com.ispw.uniride;

import com.ispw.uniride.dao.sql.SqlDAOFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Suite di Unit Testing (JUnit 5) per {@code SqlDAOFactory}: essendo solo uno scheletro non
 * ancora collegato a un database reale, verifica che ogni metodo dichiari esplicitamente
 * l'assenza del collegamento invece di fallire silenziosamente o restituire {@code null}.
 */
class SqlDAOFactoryTest {

    private final SqlDAOFactory factory = new SqlDAOFactory();

    @Test
    void testGetRideDAOThrowsUnsupported() {
        assertThrows(UnsupportedOperationException.class, factory::getRideDAO);
    }

    @Test
    void testGetStudentDAOThrowsUnsupported() {
        assertThrows(UnsupportedOperationException.class, factory::getStudentDAO);
    }

    @Test
    void testGetBookingDAOThrowsUnsupported() {
        assertThrows(UnsupportedOperationException.class, factory::getBookingDAO);
    }
}
