package com.ispw.uniride;

import com.ispw.uniride.config.LocationCatalog;
import com.ispw.uniride.config.LocationInfo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite di Unit Testing (JUnit 5) per {@code LocationCatalog}: ricerca per nome, ordinamento
 * per vicinanza e individuazione della città universitaria più vicina.
 * @author Alessio Carta
 */
class LocationCatalogTest {

    @Test
    void testGetAllNamesIsNotEmpty() {
        List<String> names = LocationCatalog.getAllNames();
        assertFalse(names.isEmpty());
        assertTrue(names.contains("Roma"));
    }

    @Test
    void testFindByNameIsCaseInsensitive() {
        LocationInfo found = LocationCatalog.findByName("  roma  ");
        assertNotNull(found);
        assertEquals("Roma", found.name());
    }

    @Test
    void testFindByNameReturnsNullWhenUnknown() {
        assertNull(LocationCatalog.findByName("Località Inesistente XYZ"));
        assertNull(LocationCatalog.findByName(null));
    }

    /**
     * Da Palestrina, i paesi limitrofi del Prenestino devono comparire prima di città lontane
     * come Palermo nell'ordinamento per vicinanza.
     */
    @Test
    void testNamesSortedByDistanceFromPutPalestrinaNearbyFirst() {
        LocationInfo palestrina = LocationCatalog.findByName("Palestrina");
        assertNotNull(palestrina);

        List<String> sorted = LocationCatalog.getNamesSortedByDistanceFrom(palestrina);

        int palestrinaIndex = sorted.indexOf("Palestrina");
        int zagaroloIndex = sorted.indexOf("Zagarolo");
        int palermoIndex = sorted.indexOf("Palermo");

        assertEquals(0, palestrinaIndex, "La località di riferimento deve comparire per prima (distanza zero)");
        assertTrue(zagaroloIndex < palermoIndex, "Un paese limitrofo deve precedere una città lontana");
    }

    /**
     * Da Palestrina l'ateneo più vicino deve essere Tor Vergata (sobborghi sud-est di Roma),
     * più vicino della Sapienza (centro città) o di qualunque altro ateneo più lontano.
     */
    @Test
    void testNearestUniversityCityFromPalestrinaIsTorVergata() {
        LocationInfo palestrina = LocationCatalog.findByName("Palestrina");

        String nearestUniversity = LocationCatalog.getNearestUniversityCity(palestrina);

        assertEquals("Roma – Tor Vergata", nearestUniversity);
    }
}
