package com.ispw.uniride.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Catalogo statico delle località selezionabili nelle ComboBox di "Offri Passaggio" e
 * "Cerca Passaggio": non solo i grandi capoluoghi, ma anche i comuni/paesi limitrofi delle
 * principali aree universitarie italiane (Lazio, Lombardia, Campania, Piemonte, Veneto,
 * Emilia-Romagna, Toscana, Sicilia), così uno studente residente in un paese come Palestrina
 * può trovare la propria località senza dover digitare a mano un nome esatto.
 * <p>
 * Le coordinate sono approssimate (centro abitato) e servono unicamente a calcolare distanze
 * relative per ordinare i suggerimenti: non hanno pretesa di precisione cartografica.
 */
public final class LocationCatalog {

    private static final List<LocationInfo> LOCATIONS = Collections.unmodifiableList(Arrays.asList(
            // ---- Lazio (area Roma, incluso il Prenestino/Castelli Romani) ----
            new LocationInfo("Roma", 41.902, 12.496, true),
            new LocationInfo("Palestrina", 41.837, 12.906, false),
            new LocationInfo("Zagarolo", 41.837, 12.836, false),
            new LocationInfo("Cave", 41.809, 12.916, false),
            new LocationInfo("San Vito Romano", 41.845, 12.978, false),
            new LocationInfo("Poli", 41.887, 12.887, false),
            new LocationInfo("Castel San Pietro Romano", 41.851, 12.933, false),
            new LocationInfo("Gallicano nel Lazio", 41.870, 12.913, false),
            new LocationInfo("Tivoli", 41.963, 12.798, false),
            new LocationInfo("Frascati", 41.808, 12.681, false),
            new LocationInfo("Albano Laziale", 41.726, 12.652, false),
            new LocationInfo("Marino", 41.767, 12.660, false),
            new LocationInfo("Velletri", 41.686, 12.776, false),
            new LocationInfo("Genzano di Roma", 41.706, 12.685, false),
            new LocationInfo("Colonna", 41.808, 12.762, false),
            new LocationInfo("San Cesareo", 41.796, 12.833, false),
            new LocationInfo("Valmontone", 41.789, 12.930, false),
            new LocationInfo("Anzio", 41.448, 12.629, false),
            new LocationInfo("Nettuno", 41.459, 12.666, false),
            new LocationInfo("Pomezia", 41.671, 12.501, false),
            new LocationInfo("Ostia", 41.732, 12.286, false),
            new LocationInfo("Guidonia Montecelio", 41.996, 12.727, false),
            new LocationInfo("Fiumicino", 41.767, 12.234, false),
            new LocationInfo("Ciampino", 41.805, 12.596, false),
            new LocationInfo("Grottaferrata", 41.786, 12.673, false),
            new LocationInfo("Monterotondo", 42.050, 12.618, false),
            new LocationInfo("Civitavecchia", 42.093, 11.795, false),
            new LocationInfo("Viterbo", 42.417, 12.107, false),
            new LocationInfo("Latina", 41.467, 12.904, false),
            new LocationInfo("Frosinone", 41.639, 13.342, false),
            new LocationInfo("Rieti", 42.404, 12.860, false),

            // ---- Lombardia (area Milano) ----
            new LocationInfo("Milano", 45.464, 9.190, true),
            new LocationInfo("Monza", 45.583, 9.274, false),
            new LocationInfo("Bergamo", 45.695, 9.670, false),
            new LocationInfo("Brescia", 45.541, 10.211, false),
            new LocationInfo("Como", 45.808, 9.085, false),
            new LocationInfo("Varese", 45.821, 8.826, false),
            new LocationInfo("Pavia", 45.185, 9.156, true),
            new LocationInfo("Lodi", 45.314, 9.504, false),
            new LocationInfo("Cremona", 45.133, 10.022, false),
            new LocationInfo("Mantova", 45.157, 10.797, false),
            new LocationInfo("Sesto San Giovanni", 45.535, 9.235, false),
            new LocationInfo("Cinisello Balsamo", 45.552, 9.216, false),
            new LocationInfo("Legnano", 45.596, 8.916, false),
            new LocationInfo("Rho", 45.531, 9.038, false),
            new LocationInfo("Cologno Monzese", 45.548, 9.276, false),
            new LocationInfo("Vimercate", 45.615, 9.367, false),
            new LocationInfo("Busto Arsizio", 45.611, 8.851, false),
            new LocationInfo("Gallarate", 45.660, 8.793, false),
            new LocationInfo("Lecco", 45.855, 9.393, false),
            new LocationInfo("Seregno", 45.652, 9.202, false),

            // ---- Campania (area Napoli) ----
            new LocationInfo("Napoli", 40.852, 14.268, true),
            new LocationInfo("Caserta", 41.073, 14.332, false),
            new LocationInfo("Salerno", 40.678, 14.759, true),
            new LocationInfo("Portici", 40.818, 14.339, false),
            new LocationInfo("Pozzuoli", 40.824, 14.121, false),
            new LocationInfo("Casoria", 40.905, 14.293, false),
            new LocationInfo("Torre del Greco", 40.786, 14.371, false),
            new LocationInfo("Ercolano", 40.807, 14.354, false),
            new LocationInfo("Giugliano in Campania", 40.928, 14.196, false),
            new LocationInfo("Aversa", 40.973, 14.207, false),
            new LocationInfo("Nola", 40.926, 14.529, false),
            new LocationInfo("Castellammare di Stabia", 40.699, 14.484, false),
            new LocationInfo("Torre Annunziata", 40.752, 14.446, false),
            new LocationInfo("Benevento", 41.130, 14.782, false),
            new LocationInfo("Avellino", 40.914, 14.792, false),

            // ---- Piemonte (area Torino) ----
            new LocationInfo("Torino", 45.070, 7.687, true),
            new LocationInfo("Moncalieri", 45.001, 7.685, false),
            new LocationInfo("Rivoli", 45.070, 7.520, false),
            new LocationInfo("Collegno", 45.079, 7.573, false),
            new LocationInfo("Nichelino", 44.994, 7.652, false),
            new LocationInfo("Grugliasco", 45.069, 7.585, false),
            new LocationInfo("Chieri", 45.021, 7.822, false),
            new LocationInfo("Settimo Torinese", 45.135, 7.769, false),
            new LocationInfo("Alessandria", 44.913, 8.615, false),
            new LocationInfo("Novara", 45.446, 8.622, false),
            new LocationInfo("Cuneo", 44.386, 7.545, false),
            new LocationInfo("Asti", 44.900, 8.207, false),
            new LocationInfo("Vercelli", 45.323, 8.422, false),
            new LocationInfo("Biella", 45.566, 8.058, false),

            // ---- Veneto ----
            new LocationInfo("Venezia", 45.440, 12.315, true),
            new LocationInfo("Mestre", 45.490, 12.242, false),
            new LocationInfo("Padova", 45.407, 11.877, true),
            new LocationInfo("Verona", 45.438, 10.992, true),
            new LocationInfo("Vicenza", 45.547, 11.546, false),
            new LocationInfo("Treviso", 45.667, 12.244, false),
            new LocationInfo("Rovigo", 45.070, 11.790, false),
            new LocationInfo("Belluno", 46.140, 12.217, false),
            new LocationInfo("Mira", 45.428, 12.135, false),
            new LocationInfo("Chioggia", 45.219, 12.279, false),
            new LocationInfo("Bassano del Grappa", 45.766, 11.735, false),
            new LocationInfo("Montebelluna", 45.775, 12.045, false),

            // ---- Emilia-Romagna ----
            new LocationInfo("Bologna", 44.494, 11.343, true),
            new LocationInfo("Modena", 44.647, 10.925, true),
            new LocationInfo("Parma", 44.801, 10.328, true),
            new LocationInfo("Reggio Emilia", 44.698, 10.631, false),
            new LocationInfo("Ferrara", 44.836, 11.619, true),
            new LocationInfo("Ravenna", 44.417, 12.200, false),
            new LocationInfo("Rimini", 44.059, 12.568, false),
            new LocationInfo("Forlì", 44.223, 12.041, false),
            new LocationInfo("Cesena", 44.139, 12.243, false),
            new LocationInfo("Piacenza", 45.052, 9.693, false),
            new LocationInfo("Imola", 44.353, 11.714, false),
            new LocationInfo("Carpi", 44.784, 10.885, false),
            new LocationInfo("Faenza", 44.286, 11.882, false),

            // ---- Toscana ----
            new LocationInfo("Firenze", 43.769, 11.256, true),
            new LocationInfo("Prato", 43.881, 11.097, false),
            new LocationInfo("Pisa", 43.716, 10.400, true),
            new LocationInfo("Livorno", 43.551, 10.313, false),
            new LocationInfo("Siena", 43.319, 11.331, true),
            new LocationInfo("Arezzo", 43.463, 11.879, false),
            new LocationInfo("Grosseto", 42.760, 11.114, false),
            new LocationInfo("Lucca", 43.844, 10.501, false),
            new LocationInfo("Pistoia", 43.933, 10.918, false),
            new LocationInfo("Massa", 44.036, 10.140, false),
            new LocationInfo("Carrara", 44.079, 10.098, false),
            new LocationInfo("Empoli", 43.717, 10.945, false),
            new LocationInfo("Scandicci", 43.759, 11.192, false),

            // ---- Sicilia ----
            new LocationInfo("Palermo", 38.116, 13.362, true),
            new LocationInfo("Catania", 37.508, 15.083, true),
            new LocationInfo("Messina", 38.193, 15.554, true),
            new LocationInfo("Siracusa", 37.075, 15.287, false),
            new LocationInfo("Ragusa", 36.926, 14.727, false),
            new LocationInfo("Trapani", 38.018, 12.514, false),
            new LocationInfo("Agrigento", 37.310, 13.586, false),
            new LocationInfo("Caltanissetta", 37.490, 14.062, false),
            new LocationInfo("Enna", 37.566, 14.280, false),
            new LocationInfo("Marsala", 37.798, 12.435, false),
            new LocationInfo("Gela", 37.068, 14.247, false),
            new LocationInfo("Acireale", 37.611, 15.166, false)
    ));

    // Raggio medio terrestre in km, usato dalla formula di Haversine per stimare le distanze.
    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * @return la lista immutabile di tutte le località note, in ordine alfabetico d'inserimento.
     */
    public static List<LocationInfo> getAll() {
        return LOCATIONS;
    }

    /**
     * @return i soli nomi delle località, utili per popolare direttamente una ComboBox.
     */
    public static List<String> getAllNames() {
        List<String> names = new ArrayList<>();
        for (LocationInfo loc : LOCATIONS) {
            names.add(loc.getName());
        }
        return names;
    }

    /**
     * Cerca una località conosciuta per nome (case-insensitive), es. quella dichiarata dallo studente.
     * @return la voce corrispondente, oppure {@code null} se il nome non è nel catalogo.
     */
    public static LocationInfo findByName(String name) {
        if (name == null) {
            return null;
        }
        for (LocationInfo loc : LOCATIONS) {
            if (loc.getName().equalsIgnoreCase(name.trim())) {
                return loc;
            }
        }
        return null;
    }

    /**
     * Ordina tutte le località per distanza crescente da una posizione di riferimento, così i paesi
     * più vicini allo studente compaiono per primi nella ComboBox (che resta comunque liberamente
     * modificabile per cercare qualunque altra destinazione).
     * @param origin la località di riferimento (tipicamente la posizione dichiarata dallo studente).
     * @return i nomi di tutte le località note, dal più vicino al più lontano rispetto a {@code origin}.
     */
    public static List<String> getNamesSortedByDistanceFrom(LocationInfo origin) {
        List<LocationInfo> sorted = new ArrayList<>(LOCATIONS);
        sorted.sort(Comparator.comparingDouble(loc -> distanceKm(origin, loc)));
        List<String> names = new ArrayList<>();
        for (LocationInfo loc : sorted) {
            names.add(loc.getName());
        }
        return names;
    }

    /**
     * Individua la città universitaria nota più vicina alla posizione di riferimento, da proporre
     * come destinazione predefinita in "Offri/Cerca Passaggio".
     * @param origin la località di riferimento.
     * @return il nome della città universitaria più vicina, o {@code null} se il catalogo non ne contiene.
     */
    public static String getNearestUniversityCity(LocationInfo origin) {
        LocationInfo nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (LocationInfo loc : LOCATIONS) {
            if (!loc.isUniversityCity()) {
                continue;
            }
            double d = distanceKm(origin, loc);
            if (d < nearestDistance) {
                nearestDistance = d;
                nearest = loc;
            }
        }
        return nearest != null ? nearest.getName() : null;
    }

    /**
     * Formula di Haversine: stima la distanza in linea d'aria (km) tra due coordinate geografiche.
     */
    private static double distanceKm(LocationInfo a, LocationInfo b) {
        double lat1 = Math.toRadians(a.getLatitude());
        double lat2 = Math.toRadians(b.getLatitude());
        double deltaLat = Math.toRadians(b.getLatitude() - a.getLatitude());
        double deltaLon = Math.toRadians(b.getLongitude() - a.getLongitude());

        double sinLat = Math.sin(deltaLat / 2);
        double sinLon = Math.sin(deltaLon / 2);
        double h = sinLat * sinLat + Math.cos(lat1) * Math.cos(lat2) * sinLon * sinLon;
        double c = 2 * Math.atan2(Math.sqrt(h), Math.sqrt(1 - h));
        return EARTH_RADIUS_KM * c;
    }

    /**
     * Nasconde il costruttore: classe di utilità con soli membri statici.
     */
    private LocationCatalog() {
    }
}
