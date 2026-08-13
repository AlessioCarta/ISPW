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
     * Una registrazione con un numero di telefono in formato plausibile deve avere successo e
     * il numero deve essere recuperabile dalla sessione dopo il login.
     */
    @Test
    void testRegisterWithValidPhoneNumberSucceeds() throws UserNotAuthorizedException {
        String username = uniqueUsername("contelefono.test");
        controller.registerUser(username, "password123", "Con Telefono", "Roma", "+39 333 1234567");

        controller.login(new UserBean(username, "password123"));

        assertEquals("+39 333 1234567", Session.getInstance().getLoggedUser().getPhoneNumber());
        Session.getInstance().logout();
    }

    /**
     * Un numero di telefono con caratteri non ammessi (lettere) deve essere rifiutato in fase
     * di registrazione, così la funzionalità di contatto fra studenti resta affidabile.
     */
    @Test
    void testRegisterWithInvalidPhoneNumberFormatFails() {
        String username = uniqueUsername("telefonoinvalido.test");
        UserNotAuthorizedException ex = assertThrows(UserNotAuthorizedException.class,
                () -> controller.registerUser(username, "password123", "Telefono Invalido", "Roma", "chiamami!"));
        assertEquals("Numero di telefono non valido: usa solo cifre, spazi ed eventualmente un + iniziale.", ex.getMessage());
    }

    /**
     * Il telefono è facoltativo come la posizione: lasciato vuoto, la registrazione deve comunque riuscire.
     */
    @Test
    void testRegisterWithoutPhoneNumberSucceeds() {
        String username = uniqueUsername("senzatelefono.test");
        assertDoesNotThrow(() -> controller.registerUser(username, "password123", "Senza Telefono", "Roma", null));
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
