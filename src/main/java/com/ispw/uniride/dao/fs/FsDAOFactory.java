package com.ispw.uniride.dao.fs;

import com.ispw.uniride.dao.BookingDAO;
import com.ispw.uniride.dao.DAOFactory;
import com.ispw.uniride.dao.RideDAO;
import com.ispw.uniride.dao.StudentDAO;

/**
 * Concrete Factory (Pattern Abstract Factory) per la famiglia File System (CSV).
 * Ogni chiamata crea una nuova istanza del DAO richiesto: essendo lo stato reale condiviso
 * a livello di campi {@code static} in ciascuna Fs*DAO (cache + lastModified), non serve
 * un Singleton esplicito qui — il comportamento di condivisione è già garantito dai campi statici.
 */
public class FsDAOFactory extends DAOFactory {
    @Override
    public RideDAO getRideDAO() {
        return new FsRideDAO();
    }

    @Override
    public StudentDAO getStudentDAO() {
        return new FsStudentDAO();
    }

    @Override
    public BookingDAO getBookingDAO() {
        return new FsBookingDAO();
    }
}
