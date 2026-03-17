package com.ispw.uniride;

/**
 * Classe Launcher usata per aggirare i requisiti del module-path di JavaFX.
 * Permette l'avvio corretto dell'interfaccia grafica (GUI) sia da IDE che da JAR compilati.
 */
public class Launcher {

    /**
     * Punto di ingresso effettivo dell'applicazione che richiama il main di JavaFX.
     * @param args argomenti passati da riga di comando
     */
    public static void main(String[] args) {
        // Chiama il metodo main della classe MainGUI per lanciare l'applicazione JavaFX
        MainGUI.main(args);
    }
}
