package com.ispw.uniride.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Catalogo statico delle principali città universitarie italiane.
 * Alimenta i menu a tendina (ComboBox) di "Offri Passaggio" e "Cerca Passaggio" in modo che
 * lo studente possa selezionare rapidamente una tratta senza dover digitare a mano il nome
 * della città. Le ComboBox restano comunque editabili: se una città non è in elenco può
 * comunque essere scritta liberamente.
 */
public final class CityCatalog {

    /**
     * Elenco alfabetico delle città proposte di default nelle selezioni.
     */
    private static final List<String> CITIES = Collections.unmodifiableList(Arrays.asList(
            "Bari", "Bologna", "Cagliari", "Catania", "Ferrara", "Firenze", "Genova",
            "Messina", "Milano", "Modena", "Napoli", "Padova", "Palermo", "Parma",
            "Pavia", "Perugia", "Pisa", "Roma", "Salerno", "Siena", "Torino",
            "Trento", "Trieste", "Udine", "Venezia", "Verona"
    ));

    /**
     * @return la lista immutabile delle città proposte nelle ComboBox dell'interfaccia.
     */
    public static List<String> getCities() {
        return CITIES;
    }

    /**
     * Nasconde il costruttore: classe di utilità con soli membri statici.
     */
    private CityCatalog() {
    }
}
