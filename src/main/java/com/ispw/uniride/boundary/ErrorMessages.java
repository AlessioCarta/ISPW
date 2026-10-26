package com.ispw.uniride.boundary;

import com.ispw.uniride.exceptions.PersistenceException;
import com.ispw.uniride.exceptions.RideActionException;
import com.ispw.uniride.exceptions.RideNotFoundException;
import com.ispw.uniride.exceptions.UserNotAuthorizedException;
import com.ispw.uniride.exceptions.UserNotFoundException;
import com.ispw.uniride.utils.LoggerCustom;
import javafx.scene.control.Alert;

/**
 * Traduzione centralizzata delle eccezioni in messaggi per l'utente, condivisa da tutte le
 * Boundary (grafiche e CLI): è il compito del Controller Grafico nel flusso delle eccezioni BCE.
 */
final class ErrorMessages {

    private static final String UNEXPECTED = "Si è verificato un errore imprevisto. Riprova.";

    /**
     * Le eccezioni di dominio e di persistenza hanno già un messaggio pensato per l'utente e lo
     * si mostra così com'è. Qualunque altra eccezione (es. NullPointerException) è un errore di
     * programmazione: il suo messaggio non è significativo per l'utente (spesso è {@code null}),
     * quindi viene registrato nel log con lo stack trace e all'utente si mostra un testo generico.
     */
    static String forUser(Exception e) {
        if (e instanceof RideActionException || e instanceof RideNotFoundException
                || e instanceof UserNotAuthorizedException || e instanceof UserNotFoundException
                || e instanceof PersistenceException) {
            return e.getMessage();
        }
        LoggerCustom.error("Errore imprevisto nella Boundary", e);
        return UNEXPECTED;
    }

    /**
     * Mostra un avviso quando non è possibile aprire una schermata (file FXML mancante o non
     * valido): senza, il clic sul pulsante non produrrebbe alcun effetto visibile.
     */
    static void showViewLoadError() {
        new Alert(Alert.AlertType.ERROR, "Impossibile aprire la schermata richiesta.").show();
    }

    private ErrorMessages() {
    }
}
