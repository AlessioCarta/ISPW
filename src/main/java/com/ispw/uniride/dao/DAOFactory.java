package com.ispw.uniride.dao;

import com.ispw.uniride.config.Config;
import com.ispw.uniride.dao.fs.FsDAOFactory;
import com.ispw.uniride.dao.memory.MemoryDAOFactory;
import com.ispw.uniride.dao.sql.SqlDAOFactory;

/**
 * Implementazione del Design Pattern Abstract Factory (GoF).
 * Permette di generare classi "familiari" (DAO memory, file system o database relazionale)
 * centralizzando unicamente in questo file la logica IF per il deployment a runtime o la
 * configurazione statica.
 * Eredita il comportamento del Design Pattern Singleton per non sprecare risorse su una factory stateless.
 */
public abstract class DAOFactory {

    // Campo statico che tiene l'unica istanza (Pattern Singleton) della factory concreta scelta.
    private static DAOFactory instance = null;

    // Costruttore protected (non private): impedisce new DAOFactory() dall'esterno del package,
    // ma resta invocabile dalle sottoclassi concrete tramite super() implicito.
    protected DAOFactory() {}

    /**
     * Valuta dinamicamente i parametri di configurazione (`Config.PERSISTENCE_TYPE`)
     * e restituisce l'unica sotto-fabbrica (Factory concreta) abilitata.
     * Metodo esplicito Synchronized per proteggere la race condition iniziale.
     * @return Una factory in grado di stanziare i DAO richiesti.
     */
    public static synchronized DAOFactory getInstance() {
        // Creata una sola volta (lazy init): le chiamate successive a getInstance() ritornano
        // sempre la stessa istanza già decisa, senza rivalutare la configurazione ogni volta.
        if (instance == null) {
            // Logica discriminante dell'Abstract Factory: è l'UNICO punto dell'applicazione che
            // sa quale famiglia concreta di DAO esiste — nessun'altra classe fa questo confronto.
            switch (Config.PERSISTENCE_TYPE) {
                case MEMORY:
                    instance = new MemoryDAOFactory();
                    break;
                case SQL:
                    instance = new SqlDAOFactory();
                    break;
                default:
                    // FILESYSTEM è il default esplicito: qualunque valore diverso da MEMORY/SQL
                    // ricade qui, coerente col fatto che è il terzo (e unico rimanente) DBType.
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
