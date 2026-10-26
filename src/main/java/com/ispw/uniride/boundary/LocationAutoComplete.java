package com.ispw.uniride.boundary;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Autocompletamento per le ComboBox editabili delle località: mentre l'utente scrive, l'elenco
 * a tendina si apre e mostra solo le voci che corrispondono al testo digitato. Riutilizzato da
 * tutte le Boundary grafiche che chiedono una località (registrazione, offerta, ricerca), così il
 * comportamento resta identico ovunque. È pura logica di presentazione: non tocca il Controller.
 */
public final class LocationAutoComplete {

    private static final Pattern DIACRITICS = Pattern.compile("\\p{M}+");
    // Separatori fra le parole di un nome: "Roma – Sapienza" ha come parole "roma" e "sapienza".
    private static final Pattern WORD_SEPARATOR = Pattern.compile("[^\\p{L}\\p{N}]+");

    private final ComboBox<String> comboBox;
    private final List<String> options;
    private final ObservableList<String> shown;
    // true mentre il filtro stesso chiude e riapre il menu: in quel caso l'elenco non va ripristinato.
    private boolean refreshing = false;

    private LocationAutoComplete(ComboBox<String> comboBox, List<String> options) {
        this.comboBox = comboBox;
        this.options = List.copyOf(options);
        this.shown = FXCollections.observableArrayList(this.options);
    }

    /**
     * Collega l'autocompletamento alla ComboBox, impostandone le voci.
     * @param options le località nell'ordine in cui vanno proposte (es. per vicinanza allo studente).
     */
    public static void attach(ComboBox<String> comboBox, List<String> options) {
        LocationAutoComplete autoComplete = new LocationAutoComplete(comboBox, options);
        comboBox.setItems(autoComplete.shown);
        comboBox.getEditor().addEventHandler(KeyEvent.KEY_RELEASED, autoComplete::onKeyReleased);
        // Chiuso il menu (dopo una scelta o un clic altrove) torna l'elenco completo, così riaprendolo
        // con la freccia si vedono di nuovo tutte le località.
        comboBox.setOnHidden(event -> {
            if (!autoComplete.refreshing) {
                autoComplete.replaceShown(autoComplete.options);
            }
        });
    }

    private void onKeyReleased(KeyEvent event) {
        KeyCode code = event.getCode();
        // Frecce, Invio, Esc, Tab e tasti modificatori servono a muoversi nell'elenco o a uscire dal
        // campo: non cambiano il testo, quindi non devono rifare il filtro.
        if (code.isNavigationKey() || code.isModifierKey() || code == KeyCode.ENTER
                || code == KeyCode.ESCAPE || code == KeyCode.TAB) {
            return;
        }
        List<String> matches = matching(options, comboBox.getEditor().getText());
        replaceShown(matches);
        refreshing = true;
        // Chiudere e riaprire il menu ne ricalcola l'altezza in base al nuovo numero di voci.
        comboBox.hide();
        if (!matches.isEmpty()) {
            comboBox.show();
        }
        refreshing = false;
    }

    /**
     * Sostituisce le voci mostrate senza perdere il testo che l'utente sta scrivendo: cambiare le
     * voci di una ComboBox editabile può far riscrivere all'editor la voce selezionata.
     */
    private void replaceShown(List<String> items) {
        TextField editor = comboBox.getEditor();
        String typed = editor.getText() == null ? "" : editor.getText();
        int caret = editor.getCaretPosition();
        shown.setAll(items);
        if (!typed.equals(editor.getText())) {
            editor.setText(typed);
        }
        editor.positionCaret(Math.min(caret, typed.length()));
    }

    /**
     * Le località che corrispondono al testo digitato, ignorando maiuscole e accenti: prima quelle
     * il cui nome inizia con il testo, poi quelle con una parola che inizia con il testo, infine
     * quelle che lo contengono altrove. All'interno di ogni gruppo resta l'ordine originale.
     * Con un testo vuoto restituisce tutte le località.
     */
    public static List<String> matching(List<String> options, String typed) {
        String query = normalize(typed == null ? "" : typed.trim());
        if (query.isEmpty()) {
            return new ArrayList<>(options);
        }
        List<String> startsWith = new ArrayList<>();
        List<String> wordStartsWith = new ArrayList<>();
        List<String> contains = new ArrayList<>();
        for (String option : options) {
            String name = normalize(option);
            if (name.startsWith(query)) {
                startsWith.add(option);
            } else if (hasWordStartingWith(name, query)) {
                wordStartsWith.add(option);
            } else if (name.contains(query)) {
                contains.add(option);
            }
        }
        startsWith.addAll(wordStartsWith);
        startsWith.addAll(contains);
        return startsWith;
    }

    private static boolean hasWordStartingWith(String name, String query) {
        for (String word : WORD_SEPARATOR.split(name)) {
            if (word.startsWith(query)) {
                return true;
            }
        }
        return false;
    }

    private static String normalize(String text) {
        String withoutAccents = DIACRITICS.matcher(Normalizer.normalize(text, Normalizer.Form.NFD)).replaceAll("");
        return withoutAccents.toLowerCase(Locale.ROOT);
    }
}
