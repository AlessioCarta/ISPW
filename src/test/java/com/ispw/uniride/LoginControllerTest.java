package com.ispw.uniride;

import com.ispw.uniride.bean.ProfileBean;
import com.ispw.uniride.bean.RegistrationBean;
import com.ispw.uniride.bean.UserBean;
import com.ispw.uniride.controller.LoginController;
import com.ispw.uniride.controller.Session;
import com.ispw.uniride.exceptions.InvalidInputException;
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
    void testRegisterAndLoginSuccess() throws UserNotAuthorizedException, InvalidInputException {
        String username = uniqueUsername("mario.test");
        controller.registerUser(TestBeans.registration(username, "password123", "Mario Test", "Roma", null));

        boolean success = controller.login(TestBeans.user(username, "password123"));

        assertTrue(success, "Il login con le credenziali appena registrate deve riuscire");
        assertNotNull(Session.getInstance().getLoggedUser());
        assertEquals(username, Session.getInstance().getLoggedUser().getUsername());

        Session.getInstance().logout();
    }

    /**
     * Un secondo tentativo di registrazione con lo stesso username deve essere rifiutato.
     */
    @Test
    void testRegisterDuplicateUsernameFails() throws UserNotAuthorizedException, InvalidInputException {
        String username = uniqueUsername("duplicate.test");
        controller.registerUser(TestBeans.registration(username, "password123", "Primo Utente", null, null));

        RegistrationBean second = TestBeans.registration(username, "altrapassword", "Secondo Utente", null, null);
        UserNotAuthorizedException ex = assertThrows(UserNotAuthorizedException.class, () -> controller.registerUser(second));
        assertEquals("Username già in uso!", ex.getMessage());
    }

    /**
     * Una password più corta di 6 caratteri deve essere rifiutata in fase di registrazione.
     */
    @Test
    void testRegisterShortPasswordFails() {
        // Controllo sintattico: lo esegue il Bean di registrazione, non il Controller.
        RegistrationBean bean = new RegistrationBean();
        InvalidInputException ex = assertThrows(InvalidInputException.class, () -> bean.setPassword("abc"));
        assertEquals("La password deve essere di almeno 6 caratteri.", ex.getMessage());
    }

    /**
     * Il login con la password sbagliata deve fallire senza promuovere alcun utente in Sessione.
     */
    @Test
    void testLoginWrongPasswordFails() throws UserNotAuthorizedException, InvalidInputException {
        String username = uniqueUsername("wrongpw.test");
        controller.registerUser(TestBeans.registration(username, "password123", "Utente Test", null, null));

        boolean success = controller.login(TestBeans.user(username, "passwordSbagliata"));

        assertFalse(success);
    }

    /**
     * Il login con uno username mai registrato deve fallire.
     */
    @Test
    void testLoginUnknownUserFails() throws InvalidInputException {
        boolean success = controller.login(TestBeans.user(uniqueUsername("nobody"), "qualsiasi"));

        assertFalse(success);
    }

    /**
     * Una registrazione con un numero di telefono in formato plausibile deve avere successo e
     * il numero deve essere recuperabile dalla sessione dopo il login.
     */
    @Test
    void testRegisterWithValidPhoneNumberSucceeds() throws UserNotAuthorizedException, InvalidInputException {
        String username = uniqueUsername("contelefono.test");
        controller.registerUser(TestBeans.registration(username, "password123", "Con Telefono", "Roma", "+39 333 1234567"));

        controller.login(TestBeans.user(username, "password123"));

        assertEquals("+39 333 1234567", Session.getInstance().getLoggedUser().getPhoneNumber());
        Session.getInstance().logout();
    }

    /**
     * Un numero di telefono con caratteri non ammessi (lettere) deve essere rifiutato in fase
     * di registrazione, così la funzionalità di contatto fra studenti resta affidabile.
     */
    @Test
    void testRegisterWithInvalidPhoneNumberFormatFails() {
        RegistrationBean bean = new RegistrationBean();
        InvalidInputException ex = assertThrows(InvalidInputException.class, () -> bean.setPhoneNumber("chiamami!"));
        assertEquals("Numero di telefono non valido: usa solo cifre, spazi ed eventualmente un + iniziale.", ex.getMessage());
    }

    /**
     * Il telefono è facoltativo come la posizione: lasciato vuoto, la registrazione deve comunque riuscire.
     */
    @Test
    void testRegisterWithoutPhoneNumberSucceeds() throws InvalidInputException {
        RegistrationBean bean = TestBeans.registration(uniqueUsername("senzatelefono.test"), "password123", "Senza Telefono", "Roma", null);
        assertDoesNotThrow(() -> controller.registerUser(bean));
    }

    /**
     * Il logout deve azzerare l'utente attualmente loggato in Sessione.
     */
    @Test
    void testLogoutClearsSession() throws UserNotAuthorizedException, InvalidInputException {
        String username = uniqueUsername("logout.test");
        controller.registerUser(TestBeans.registration(username, "password123", "Utente Logout", null, null));
        controller.login(TestBeans.user(username, "password123"));
        assertNotNull(Session.getInstance().getLoggedUser());

        controller.logout();

        assertNull(Session.getInstance().getLoggedUser());
    }

    /**
     * Username con spazi interni, troppo corti o con virgole, e nome completo vuoto o con
     * virgole, devono essere rifiutati prima di arrivare al DAO.
     */
    @Test
    void testRegisterInvalidUsernameOrNameFails() {
        RegistrationBean bean = new RegistrationBean();
        assertThrows(InvalidInputException.class, () -> bean.setUsername("ab"));
        assertThrows(InvalidInputException.class, () -> bean.setUsername("mario rossi"));
        assertThrows(InvalidInputException.class, () -> bean.setFullName("  "));
        assertThrows(InvalidInputException.class, () -> bean.setFullName("Rossi, Mario"));
    }

    /**
     * Gli spazi attorno allo username vengono rimossi sia in registrazione sia al login, così
     * "nome " e "nome" non possono diventare due account distinti.
     */
    @Test
    void testUsernameIsTrimmedOnRegisterAndLogin() throws UserNotAuthorizedException, InvalidInputException {
        String username = uniqueUsername("trim.test");
        controller.registerUser(TestBeans.registration("  " + username + " ", "password123", "Utente Trim", null, null));

        RegistrationBean duplicate = TestBeans.registration(username, "password123", "Duplicato", null, null);
        assertThrows(UserNotAuthorizedException.class, () -> controller.registerUser(duplicate));
        assertTrue(controller.login(TestBeans.user(username + " ", "password123")));
    }

    /**
     * Il profilo esposto alla Boundary riflette l'utente loggato (senza password) ed è null
     * dopo il logout.
     */
    @Test
    void testLoggedUserProfileExposedAsBean() throws UserNotAuthorizedException, InvalidInputException {
        String username = uniqueUsername("profilo.test");
        controller.registerUser(TestBeans.registration(username, "password123", "Utente Profilo", "Roma", null));
        assertTrue(controller.login(TestBeans.user(username, "password123")));

        ProfileBean profile = controller.getLoggedUserProfile();
        assertEquals("Utente Profilo", profile.getFullName());
        assertEquals("Roma", profile.getHomeLocation());

        controller.logout();
        assertNull(controller.getLoggedUserProfile());
    }
}
