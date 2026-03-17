package com.ispw.uniride.config;

/**
 * Classe contenitore per le configurazioni globali e i parametri di sistema di UniRide.
 * Al momento questa classe ha la priorità di determinare a "compile-time" (o esecuzione statica)
 * il tipo di persistenza da utilizzare nei DAOFactory (Pattern Abstract Factory).
 */
public class Config {

    /**
     * Enumerazione dei diversi meccanismi supportati per la memoria dati.
     */
    public enum DBType {
        /**
         * Archiviazione RAM temporanea (utilizzando strutture hash map locali).
         * Modalità ottimale per O(1) in prototipazione rapida, ma volatile in quanto i dati si azzerano
         * ad ogni riavvio del progetto.
         */
        MEMORY,

        /**
         * Memorizzazione testuale fisica permanente dei dati (tramite i file system e l'estensione CSV).
         * Modalità concepita con la sincronizzazione thread-safe tramite Lock e Caching per evitare
         * continui accessi bloccanti su disco.
         */
        FILESYSTEM
    }

    /**
     * SWITCH architetturale: la modifica di questo parametro farà istanziare al sistema l'intero
     * ecosistema DAO memory piuttosto che quello legato al disco rigido.
     */
    public static final DBType PERSISTENCE_TYPE = DBType.MEMORY;

    /**
     * Nasconde il costruttore di base. L'istanza è inutile trattandosi di classe con campi finali statici.
     */
    private Config() {
    }
}
