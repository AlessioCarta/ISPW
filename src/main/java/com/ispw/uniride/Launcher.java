package com.ispw.uniride;

/**
 * Launcher class to bypass JavaFX module-path requirements when starting
 * the application from an IDE or a fat JAR.
 */
public class Launcher {
    public static void main(String[] args) {
        MainGUI.main(args);
    }
}
