package com.ispw.uniride;

import com.ispw.uniride.bean.RideBean;
import com.ispw.uniride.controller.OfferRideController;
import com.ispw.uniride.controller.Session;
import com.ispw.uniride.dao.DAOFactory;
import com.ispw.uniride.dao.RideDAO;
import com.ispw.uniride.exceptions.RideActionException;
import com.ispw.uniride.exceptions.UserNotAuthorizedException;
import com.ispw.uniride.model.Ride;
import com.ispw.uniride.model.Student;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite di Unit Testing (JUnit 5) per {@code OfferRideController}: creazione di un passaggio
 * e le validazioni di dominio applicate prima della persistenza.
 * @author Alessio Carta
 */
class OfferRideControllerTest {

    private final OfferRideController controller = new OfferRideController();

    // Data sempre futura rispetto al giorno di esecuzione: una data fissa farebbe fallire i test
    // non appena superata, ora che OfferRideController rifiuta i passaggi nel passato.
    private static final String FUTURE_DATE = LocalDate.now().plusMonths(1).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

    private String uniqueUsername(String prefix) {
        return prefix + "." + System.nanoTime();
    }

    private void loginAs(String username) {
        Session.getInstance().setLoggedUser(new Student(username, "[HASHED]password", "Utente Test"));
    }

    @AfterEach
    void tearDown() {
        Session.getInstance().logout();
    }

    /**
     * Un'offerta con dati validi deve avere successo per un utente autenticato.
     */
    @Test
    void testOfferRideSuccess() {
        loginAs(uniqueUsername("driver"));

        RideBean bean = new RideBean("Roma", "Milano", FUTURE_DATE, "08:30", 3, 20.0);

        assertDoesNotThrow(() -> controller.offerRide(bean));
    }

    /**
     * Senza un utente loggato l'operazione deve essere rifiutata.
     */
    @Test
    void testOfferRideWithoutLoginFails() {
        Session.getInstance().logout();
        RideBean bean = new RideBean("Roma", "Milano", FUTURE_DATE, "08:30", 3, 20.0);

        assertThrows(UserNotAuthorizedException.class, () -> controller.offerRide(bean));
    }

    /**
     * Partenza e destinazione coincidenti devono essere rifiutate.
     */
    @Test
    void testOfferRideSameDepartureAndDestinationFails() {
        loginAs(uniqueUsername("driver"));
        RideBean bean = new RideBean("Torino", "torino", FUTURE_DATE, "08:30", 2, 15.0);

        RideActionException ex = assertThrows(RideActionException.class, () -> controller.offerRide(bean));
        assertEquals("Partenza e destinazione non possono coincidere.", ex.getMessage());
    }

    /**
     * Un numero di posti non positivo deve essere rifiutato.
     */
    @Test
    void testOfferRideZeroSeatsFails() {
        loginAs(uniqueUsername("driver"));
        RideBean bean = new RideBean("Napoli", "Salerno", FUTURE_DATE, "08:30", 0, 10.0);

        RideActionException ex = assertThrows(RideActionException.class, () -> controller.offerRide(bean));
        assertEquals("Il numero di posti deve essere maggiore di zero.", ex.getMessage());
    }

    /**
     * Un costo base negativo deve essere rifiutato.
     */
    @Test
    void testOfferRideNegativePriceFails() {
        loginAs(uniqueUsername("driver"));
        RideBean bean = new RideBean("Bari", "Lecce", FUTURE_DATE, "08:30", 2, -5.0);

        RideActionException ex = assertThrows(RideActionException.class, () -> controller.offerRide(bean));
        assertEquals("Il costo base non può essere negativo.", ex.getMessage());
    }

    /**
     * Un orario di partenza mancante o con formato non valido deve essere rifiutato.
     */
    @Test
    void testOfferRideInvalidDepartureTimeFails() {
        loginAs(uniqueUsername("driver"));
        RideBean bean = new RideBean("Genova", "Savona", FUTURE_DATE, "ore otto", 2, 10.0);

        RideActionException ex = assertThrows(RideActionException.class, () -> controller.offerRide(bean));
        assertEquals("Inserisci un orario di partenza valido (formato HH:mm, es. 08:30).", ex.getMessage());
    }

    /**
     * Un'ora digitata senza lo zero iniziale (es. "9:30") deve essere accettata, non solo quella
     * a due cifre: è la digitazione più naturale per chi non guarda l'esempio nel prompt.
     * Il valore persistito deve però essere sempre normalizzato a due cifre ("09:30"), così che
     * le liste mostrino un formato coerente indipendentemente da come l'ha digitato il guidatore.
     */
    @Test
    void testOfferRideSingleDigitHourIsAcceptedAndNormalized() {
        String driver = uniqueUsername("driver");
        loginAs(driver);
        RideBean bean = new RideBean("Frosinone", "Cassino", FUTURE_DATE, "9:30", 2, 10.0);

        assertDoesNotThrow(() -> controller.offerRide(bean));

        RideDAO rideDAO = DAOFactory.getInstance().getRideDAO();
        List<Ride> offered = rideDAO.getRidesByDriver(driver);
        assertEquals(1, offered.size());
        assertEquals("09:30", offered.get(0).getDepartureTime());
    }

    /**
     * Una data mancante (DatePicker vuoto), inesistente o già passata deve essere rifiutata.
     */
    @Test
    void testOfferRideInvalidDateFails() {
        loginAs(uniqueUsername("driver"));
        String yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        assertThrows(RideActionException.class, () -> controller.offerRide(new RideBean("Roma", "Milano", "", "08:30", 2, 10.0)));
        assertThrows(RideActionException.class, () -> controller.offerRide(new RideBean("Roma", "Milano", "31/02/2030", "08:30", 2, 10.0)));
        RideActionException ex = assertThrows(RideActionException.class,
                () -> controller.offerRide(new RideBean("Roma", "Milano", yesterday, "08:30", 2, 10.0)));
        assertEquals("Non si può offrire un passaggio in una data già passata.", ex.getMessage());
    }

    /**
     * Una virgola in partenza/destinazione spezzerebbe le colonne di rides.csv.
     */
    @Test
    void testOfferRideCommaInCityFails() {
        loginAs(uniqueUsername("driver"));
        RideBean bean = new RideBean("Roma, Italia", "Milano", FUTURE_DATE, "08:30", 2, 10.0);

        assertThrows(RideActionException.class, () -> controller.offerRide(bean));
    }
}
