package com.ispw.uniride;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main application class for the JavaFX GUI.
 */
public class MainGUI extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("UniRide - Piattaforma di Carpooling Universitario");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
