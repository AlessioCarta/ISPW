package com.ispw.uniride.bean;

import com.ispw.uniride.exceptions.InvalidInputException;

/**
 * Controlli sintattici comuni ai Bean di input (Pure Fabrication GRASP): evita di ripetere in
 * ogni setter la stessa gestione di campi vuoti e caratteri non ammessi.
 */
final class BeanFields {

    /**
     * @return il valore privo di spazi iniziali e finali.
     * @throws InvalidInputException con il messaggio indicato se il valore è assente o vuoto.
     */
    static String required(String value, String message) throws InvalidInputException {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidInputException(message);
        }
        return value.trim();
    }

    /**
     * @return il valore privo di spazi iniziali e finali, oppure {@code null} se non è stato fornito.
     */
    static String optional(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    /**
     * La virgola è il separatore dei file CSV della persistenza su file system: in un campo di
     * testo ne sposterebbe le colonne, quindi viene rifiutata già all'ingresso del dato.
     * @throws InvalidInputException se il valore contiene una virgola.
     */
    static void rejectComma(String value, String message) throws InvalidInputException {
        if (value != null && value.contains(",")) {
            throw new InvalidInputException(message);
        }
    }

    private BeanFields() {
    }
}
