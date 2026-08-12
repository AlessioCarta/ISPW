package com.ispw.uniride;

import com.ispw.uniride.bean.UserBean;
import com.ispw.uniride.controller.LoginController;
import com.ispw.uniride.controller.Session;
import com.ispw.uniride.exceptions.UserNotAuthorizedException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite di Unit Testing (JUnit 5) per {@code LoginController}: registrazione, login e logout.
 * Session e DAOFactory sono Singleton condivisi per tutta la JVM di test, quindi ogni test usa
 * uno username univoco per restare indipendente dagli altri indipendentemente dall'ordine di esecuzione.
 * @author Alessio Carta
 */
class LoginControllerTest {

    private final LoginController controller = new LoginController();

    private String uniqueUsername(String prefix) {
        return prefix + "." + System.nanoTime();
    }

    /**
     * Una registrazione con dati validi deve avere successo e permettere il login immediato
     * con le stesse credenziali.
     */
    @Test
    void testRegisterAndLoginSuccess() throws UserNotAuthorizedException {
        String username = uniqueUsername("mario.test");
        controller.registerUser(username, "password123", "Mario Test", "Roma");

        boolean success = controller.login(new UserBean(username, "password123"));

        assertTrue(success, "Il login con le credenziali appena registrate deve riuscire");
        assertNotNull(Session.getInstance().getLoggedUser());
        assertEquals(username, Session.getInstance().getLoggedUser().getUsername());

        Session.getInstance().logout();
    }

    /**
     * Un secondo tentativo di registrazione con lo stesso username deve essere rifiutato.
     */
    @Test
    void testRegisterDuplicateUsernameFails() throws UserNotAuthorizedException {
        String username = uniqueUsername("duplicate.test");
        controller.registerUser(username, "password123", "Primo Utente", null);

        UserNotAuthorizedException ex = assertThrows(UserNotAuthorizedException.class,
                () -> controller.registerUser(username, "altrapassword", "Secondo Utente", null));
        assertEquals("Username già in uso!", ex.getMessage());
    }

    /**
     * Una password più corta di 6 caratteri deve essere rifiutata in fase di registrazione.
     */
    @Test
    void testRegisterShortPasswordFails() {
        String username = uniqueUsername("shortpw.test");
        UserNotAuthorizedException ex = assertThrows(UserNotAuthorizedException.class,
                () -> controller.registerUser(username, "abc", "Utente Corto", null));
        assertEquals("La password deve essere di almeno 6 caratteri.", ex.getMessage());
    }

    /**
     * Il login con la password sbagliata deve fallire senza promuovere alcun utente in Sessione.
     */
    @Test
    void testLoginWrongPasswordFails() throws UserNotAuthorizedException {
        String username = uniqueUsername("wrongpw.test");
        controller.registerUser(username, "password123", "Utente Test", null);

        boolean success = controller.login(new UserBean(username, "passwordSbagliata"));

        assertFalse(success);
    }

    /**
     * Il login con uno username mai registrato deve fallire.
     */
    @Test
    void testLoginUnknownUserFails() {
        boolean success = controller.login(new UserBean(uniqueUsername("nobody"), "qualsiasi"));

        assertFalse(success);
    }

    /**
     * Il logout deve azzerare l'utente attualmente loggato in Sessione.
     */
    @Test
    void testLogoutClearsSession() throws UserNotAuthorizedException {
        String username = uniqueUsername("logout.test");
        controller.registerUser(username, "password123", "Utente Logout", null);
        controller.login(new UserBean(username, "password123"));
        assertNotNull(Session.getInstance().getLoggedUser());

        controller.logout();

        assertNull(Session.getInstance().getLoggedUser());
    }
}
