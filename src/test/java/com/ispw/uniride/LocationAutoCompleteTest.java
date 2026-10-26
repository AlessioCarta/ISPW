package com.ispw.uniride;

import com.ispw.uniride.boundary.LocationAutoComplete;
import com.ispw.uniride.config.LocationCatalog;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite di Unit Testing (JUnit 5) per la logica di filtro dell'autocompletamento delle località
 * ({@code LocationAutoComplete.matching}), indipendente dalla parte grafica JavaFX.
 * @author Alessio Carta
 */
class LocationAutoCompleteTest {

    private static final List<String> OPTIONS = List.of("Roma", "Roma – Sapienza", "Frascati", "Artena", "Forlì", "Martina Franca");

    @Test
    void testNamesStartingWithTheTextComeFirst() {
        // "Artena" inizia con "art"; "Martina Franca" lo contiene solo all'interno di una parola.
        assertEquals(List.of("Artena", "Martina Franca"), LocationAutoComplete.matching(OPTIONS, "Art"));
    }

    @Test
    void testWordStartMatchesFollowFullNameMatches() {
        assertEquals(List.of("Roma – Sapienza"), LocationAutoComplete.matching(OPTIONS, "sap"));
        assertEquals(List.of("Roma", "Roma – Sapienza"), LocationAutoComplete.matching(OPTIONS, "ROMA"));
    }

    @Test
    void testAccentsAndCaseAreIgnored() {
        assertEquals(List.of("Forlì"), LocationAutoComplete.matching(OPTIONS, "forli"));
    }

    @Test
    void testEmptyTextShowsEveryOptionInOriginalOrder() {
        assertEquals(OPTIONS, LocationAutoComplete.matching(OPTIONS, "  "));
        assertTrue(LocationAutoComplete.matching(OPTIONS, "xyz").isEmpty());
    }

    @Test
    void testRealCatalogContainsTheMatchingCampus() {
        assertTrue(LocationAutoComplete.matching(LocationCatalog.getAllNames(), "tor verg").contains("Roma – Tor Vergata"));
    }
}
