package com.ispw.uniride.utils;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Wrapper personalizzato per il logging nativo di Java (`java.util.logging.Logger`).
 * Utilizzato sistematicamente in tutto il progetto in sostituzione alle chiamate sincrone di
 * `System.out.println`, allo scopo di ottimizzare le risorse di I/O (Input/Output).
 */
public class LoggerCustom {

    // Istanza di logger statico legata a questa specifica classe
    private static final Logger logger = Logger.getLogger(LoggerCustom.class.getName());

    // Blocco di inizializzazione statica per configurare livelli e handler del logger.
    static {
        // Abilita la stampa di tutti i messaggi a livello nativo
        logger.setLevel(Level.ALL);

        // Assegna un gestore per reindirizzare le stampe sul terminale con formato standard
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        logger.addHandler(handler);

        // Evita che i messaggi vengano duplicati dalla gerarchia predefinita del root logger
        logger.setUseParentHandlers(false);
    }

    /**
     * Costruttore privato che impedisce l'istanziazione di questa classe.
     * I metodi di utilità come il Logger devono essere usati in contesto statico.
     */
    private LoggerCustom() {
    }

    /**
     * Registra un messaggio informativo di normale flusso d'esecuzione.
     * @param message il testo da stampare a livello INFO
     */
    public static void info(String message) {
        logger.info(message);
    }

    /**
     * Registra un avvertimento su eventi imprevisti, non fatali.
     * @param message il testo di allerta a livello WARNING
     */
    public static void warning(String message) {
        logger.warning(message);
    }

    /**
     * Registra un errore di sistema critico o eccezione, e ne riporta lo stack trace in output.
     * @param message un testo esplicativo sul contesto dell'errore (a livello SEVERE)
     * @param thrown l'eccezione / throwable che ha scatenato la chiamata
     */
    public static void error(String message, Throwable thrown) {
        logger.log(Level.SEVERE, message, thrown);
    }
}
