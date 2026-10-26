package com.ispw.uniride.boundary;

import java.util.Scanner;

/**
 * Lettura dell'input da console condivisa da tutte le Boundary testuali.
 * Un solo {@link Scanner} per l'intero processo: ogni Scanner bufferizza in anticipo parte di
 * System.in, quindi crearne uno per schermata fa perdere le righe già lette da quello
 * precedente (evidente quando l'input arriva da un file o da una pipe).
 */
final class InputCLI {

    private static final Scanner SCANNER = new Scanner(System.in);

    /**
     * Mostra il prompt e restituisce la riga digitata dall'utente (stringa vuota a fine input).
     */
    static String readLine(String prompt) {
        System.out.print(prompt);
        return SCANNER.hasNextLine() ? SCANNER.nextLine() : "";
    }

    /**
     * Legge una scelta numerica da menu.
     * @return il numero digitato, oppure -1 se la riga non è un numero intero.
     */
    static int readChoice(String prompt) {
        try {
            return Integer.parseInt(readLine(prompt).trim());
        } catch (NumberFormatException _) {
            return -1;
        }
    }

    private InputCLI() {
    }
}
