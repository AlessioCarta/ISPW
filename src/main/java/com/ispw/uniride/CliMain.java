package com.ispw.uniride;

import com.ispw.uniride.boundary.LoginCLI;
import com.ispw.uniride.utils.LoggerCustom;

/**
 * Punto di ingresso (Entry Point) dell'applicazione per l'interfaccia a riga di comando (CLI).
 * È stato concepito per ambienti "headless" (senza display grafico), per testing
 * e bypassa completamente l'inizializzazione di JavaFX e delle librerie FXML.
 */
public class CliMain {

    /**
     * Avvia l'applicazione in modalità testuale.
     * @param args argomenti passati da riga di comando all'avvio del programma (non utilizzati:
     * la firma {@code String[] args} è comunque richiesta dalla JVM per riconoscere l'entry point).
     */
    @SuppressWarnings("java:S1172")
    public static void main(String[] args) {
        // Usa il logger custom per evitare colli di bottiglia da standard I/O in sistemi di produzione
        LoggerCustom.info("Starting UniRide CLI Mode...");

        // Istanzia la boundary del login della CLI per avviare il ciclo di vita utente testuale
        new LoginCLI().start();
    }
}
