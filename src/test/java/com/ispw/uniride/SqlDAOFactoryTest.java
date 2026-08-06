package com.ispw.uniride;

import com.ispw.uniride.dao.sql.JdbcBookingDAO;
import com.ispw.uniride.dao.sql.JdbcRideDAO;
import com.ispw.uniride.dao.sql.JdbcStudentDAO;
import com.ispw.uniride.dao.sql.SqlDAOFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Suite di Unit Testing (JUnit 5) per {@code SqlDAOFactory}: verifica che la famiglia di DAO
 * restituita sia effettivamente quella collegata al database relazionale (H2 via JDBC).
 */
class SqlDAOFactoryTest {

    private final SqlDAOFactory factory = new SqlDAOFactory();

    @Test
    void testGetRideDAOReturnsJdbcImplementation() {
        assertInstanceOf(JdbcRideDAO.class, factory.getRideDAO());
    }

    @Test
    void testGetStudentDAOReturnsJdbcImplementation() {
        assertInstanceOf(JdbcStudentDAO.class, factory.getStudentDAO());
    }

    @Test
    void testGetBookingDAOReturnsJdbcImplementation() {
        assertInstanceOf(JdbcBookingDAO.class, factory.getBookingDAO());
    }
}
