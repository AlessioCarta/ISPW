package com.ispw.uniride;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Classe principale per l'interfaccia grafica JavaFX (GUI).
 * Gestisce l'avvio della finestra e il caricamento della prima scena.
 */
public class MainGUI extends Application {

    /**
     * Metodo di partenza del ciclo di vita JavaFX.
     * Viene chiamato automaticamente dopo launch().
     * @param primaryStage lo Stage principale (finestra) fornito dal sistema.
     * @throws Exception se il file FXML non viene trovato o è malformato.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Carica la gerarchia dei nodi dal file FXML del Login
        Parent root = FXMLLoader.load(getClass().getResource("/com/ispw/uniride/Login.fxml"));

        // Imposta il titolo della finestra principale dell'applicazione
        primaryStage.setTitle("UniRide - Piattaforma di Carpooling Universitario");

        // Crea una nuova Scena con dimensioni predefinite 400x300 pixel contenente il layout caricato
        primaryStage.setScene(new Scene(root, 400, 300));

        // Mostra la finestra a schermo
        primaryStage.show();
    }

    /**
     * Metodo main interno a JavaFX per avviare il framework.
     * Deve essere richiamato dalla classe esterna Launcher per evitare errori di modulo.
     * @param args argomenti riga di comando passati all'applicazione
     */
    public static void main(String[] args) {
        // Richiama la procedura interna di JavaFX che istanzia MainGUI e invoca start()
        launch(args);
    }
}
