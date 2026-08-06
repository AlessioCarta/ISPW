package com.ispw.uniride.dao.sql;

import com.ispw.uniride.dao.BookingDAO;
import com.ispw.uniride.dao.DAOFactory;
import com.ispw.uniride.dao.RideDAO;
import com.ispw.uniride.dao.StudentDAO;

/**
 * Concrete Factory della famiglia di persistenza su database relazionale: usa H2 in modalità
 * embedded (file locale, nessun server esterno da installare/configurare) tramite JDBC standard.
 * Selezionabile impostando {@code Config.PERSISTENCE_TYPE = DBType.SQL}.
 */
public class SqlDAOFactory extends DAOFactory {
    @Override
    public RideDAO getRideDAO() {
        return new JdbcRideDAO();
    }

    @Override
    public StudentDAO getStudentDAO() {
        return new JdbcStudentDAO();
    }

    @Override
    public BookingDAO getBookingDAO() {
        return new JdbcBookingDAO();
    }
}
