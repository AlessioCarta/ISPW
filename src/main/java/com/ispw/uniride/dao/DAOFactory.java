package com.ispw.uniride.dao;

import com.ispw.uniride.config.Config;
import com.ispw.uniride.dao.fs.FsDAOFactory;
import com.ispw.uniride.dao.memory.MemoryDAOFactory;

/**
 * Abstract Factory for producing DAO instances based on current config.
 */
public abstract class DAOFactory {

    private static DAOFactory instance = null;

    protected DAOFactory() {}

    public static synchronized DAOFactory getInstance() {
        if (instance == null) {
            if (Config.PERSISTENCE_TYPE == Config.DBType.MEMORY) {
                instance = new MemoryDAOFactory();
            } else {
                instance = new FsDAOFactory();
            }
        }
        return instance;
    }

    public abstract RideDAO getRideDAO();
    public abstract StudentDAO getStudentDAO();
}
