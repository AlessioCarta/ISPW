package com.ispw.uniride.dao.fs;

import com.ispw.uniride.dao.BookingDAO;
import com.ispw.uniride.dao.DAOFactory;
import com.ispw.uniride.dao.RideDAO;
import com.ispw.uniride.dao.StudentDAO;

/**
 * Produces File System DAO implementations.
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
