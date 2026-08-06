package com.ispw.uniride.dao.sql;

import com.ispw.uniride.dao.BookingDAO;
import com.ispw.uniride.dao.DAOFactory;
import com.ispw.uniride.dao.RideDAO;
import com.ispw.uniride.dao.StudentDAO;

/**
 * Concrete Factory che verrebbe configurata se il sistema scalasse per impiegare MySQL/PostgreSQL.
 * Attualmente definita a scopo prototipale per rispettare l'Abstract Factory Pattern.
 */
public class SqlDAOFactory extends DAOFactory {
    @Override
    public RideDAO getRideDAO() {
        throw new UnsupportedOperationException("JDBC MySQL Database not linked yet.");
    }

    @Override
    public StudentDAO getStudentDAO() {
        throw new UnsupportedOperationException("JDBC MySQL Database not linked yet.");
    }

    @Override
    public BookingDAO getBookingDAO() {
        throw new UnsupportedOperationException("JDBC MySQL Database not linked yet.");
    }
}
