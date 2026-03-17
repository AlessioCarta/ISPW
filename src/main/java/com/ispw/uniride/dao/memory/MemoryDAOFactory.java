package com.ispw.uniride.dao.memory;

import com.ispw.uniride.dao.DAOFactory;
import com.ispw.uniride.dao.RideDAO;
import com.ispw.uniride.dao.StudentDAO;

public class MemoryDAOFactory extends DAOFactory {
    @Override
    public RideDAO getRideDAO() {
        return new MemoryRideDAO();
    }

    @Override
    public StudentDAO getStudentDAO() {
        return new MemoryStudentDAO();
    }
}
