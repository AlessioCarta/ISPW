package com.ispw.uniride;

import com.ispw.uniride.bean.RideBean;
import com.ispw.uniride.controller.OfferRideController;
import com.ispw.uniride.controller.Session;
import com.ispw.uniride.exceptions.RideActionException;
import com.ispw.uniride.exceptions.UserNotAuthorizedException;
import com.ispw.uniride.model.Student;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite di Unit Testing (JUnit 5) per {@code OfferRideController}: creazione di un passaggio
 * e le validazioni di dominio applicate prima della persistenza.
 * @author Alessio Carta
 */
class OfferRideControllerTest {

    private final OfferRideController controller = new OfferRideController();

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

        RideBean bean = new RideBean("Roma", "Milano", "10/12/2026", 3, 20.0);

        assertDoesNotThrow(() -> controller.offerRide(bean));
    }

    /**
     * Senza un utente loggato l'operazione deve essere rifiutata.
     */
    @Test
    void testOfferRideWithoutLoginFails() {
        Session.getInstance().logout();
        RideBean bean = new RideBean("Roma", "Milano", "10/12/2026", 3, 20.0);

        assertThrows(UserNotAuthorizedException.class, () -> controller.offerRide(bean));
    }

    /**
     * Partenza e destinazione coincidenti devono essere rifiutate.
     */
    @Test
    void testOfferRideSameDepartureAndDestinationFails() {
        loginAs(uniqueUsername("driver"));
        RideBean bean = new RideBean("Torino", "torino", "10/12/2026", 2, 15.0);

        RideActionException ex = assertThrows(RideActionException.class, () -> controller.offerRide(bean));
        assertEquals("Partenza e destinazione non possono coincidere.", ex.getMessage());
    }

    /**
     * Un numero di posti non positivo deve essere rifiutato.
     */
    @Test
    void testOfferRideZeroSeatsFails() {
        loginAs(uniqueUsername("driver"));
        RideBean bean = new RideBean("Napoli", "Salerno", "10/12/2026", 0, 10.0);

        RideActionException ex = assertThrows(RideActionException.class, () -> controller.offerRide(bean));
        assertEquals("Il numero di posti deve essere maggiore di zero.", ex.getMessage());
    }

    /**
     * Un costo base negativo deve essere rifiutato.
     */
    @Test
    void testOfferRideNegativePriceFails() {
        loginAs(uniqueUsername("driver"));
        RideBean bean = new RideBean("Bari", "Lecce", "10/12/2026", 2, -5.0);

        RideActionException ex = assertThrows(RideActionException.class, () -> controller.offerRide(bean));
        assertEquals("Il costo base non può essere negativo.", ex.getMessage());
    }
}
