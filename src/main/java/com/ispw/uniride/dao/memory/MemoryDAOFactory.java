package com.ispw.uniride.dao.memory;

import com.ispw.uniride.dao.BookingDAO;
import com.ispw.uniride.dao.DAOFactory;
import com.ispw.uniride.dao.RideDAO;
import com.ispw.uniride.dao.StudentDAO;

/**
 * Concrete Factory (Pattern Abstract Factory) per la famiglia In-Memory. Ogni metodo crea
 * un'istanza nuova del DAO corrispondente: essendo i Memory*DAO privi di stato proprio
 * (delegano tutto al Singleton {@link MemoryStorage}), non c'è bisogno di riusarne una sola.
 */
public class MemoryDAOFactory extends DAOFactory {
    @Override
    public RideDAO getRideDAO() {
        return new MemoryRideDAO();
    }

    @Override
    public StudentDAO getStudentDAO() {
        return new MemoryStudentDAO();
    }

    @Override
    public BookingDAO getBookingDAO() {
        return new MemoryBookingDAO();
    }
}
