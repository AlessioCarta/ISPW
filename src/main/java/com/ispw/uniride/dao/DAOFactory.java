package com.ispw.uniride.dao;

import com.ispw.uniride.config.Config;
import com.ispw.uniride.dao.fs.FsDAOFactory;
import com.ispw.uniride.dao.memory.MemoryDAOFactory;

/**
 * Implementazione del Design Pattern Abstract Factory (GoF).
 * Permette di generare classi "familiari" (DAO memory vs file system) centralizzando
 * unicamente in questo file la logica IF per il deployment a runtime o la configurazione statica.
 * Eredita il comportamento del Design Pattern Singleton per non sprecare risorse su una factory stateless.
 */
public abstract class DAOFactory {

    private static DAOFactory instance = null;

    protected DAOFactory() {}

    /**
     * Valuta dinamicamente i parametri di configurazione (`Config.PERSISTENCE_TYPE`)
     * e restituisce l'unica sotto-fabbrica (Factory concreta) abilitata.
     * Metodo esplicito Synchronized per proteggere la race condition iniziale.
     * @return Una factory in grado di stanziare i DAO richiesti.
     */
    public static synchronized DAOFactory getInstance() {
        if (instance == null) {
            // Logica discriminante dell'Abstract Factory:
            if (Config.PERSISTENCE_TYPE == Config.DBType.MEMORY) {
                instance = new MemoryDAOFactory();
            } else {
                instance = new FsDAOFactory();
            }
        }
        return instance;
    }

    /**
     * @return Il componente DAO di famiglia Ride.
     */
    public abstract RideDAO getRideDAO();

    /**
     * @return Il componente DAO di famiglia Studenti.
     */
    public abstract StudentDAO getStudentDAO();

    /**
     * @return Il componente DAO di famiglia Prenotazioni (stato di approvazione delle richieste).
     */
    public abstract BookingDAO getBookingDAO();
}
