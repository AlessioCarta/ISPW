package com.ispw.uniride;

import com.ispw.uniride.utils.PasswordHasher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite di Unit Testing (JUnit 5) per {@code PasswordHasher}: hash PBKDF2 con salt e verifica,
 * compreso il formato prototipale precedente ancora presente in file/database esistenti.
 * @author Alessio Carta
 */
class PasswordHasherTest {

    @Test
    void testHashVerifiesOnlyTheOriginalPassword() {
        String stored = PasswordHasher.hash("password123");

        assertFalse(stored.contains("password123"));
        assertFalse(stored.contains(","));
        assertTrue(PasswordHasher.verify("password123", stored));
        assertFalse(PasswordHasher.verify("password124", stored));
    }

    @Test
    void testSamePasswordProducesDifferentHashes() {
        // Salt casuale: due utenti con la stessa password non hanno lo stesso valore memorizzato.
        assertNotEquals(PasswordHasher.hash("password123"), PasswordHasher.hash("password123"));
    }

    @Test
    void testLegacyAndCorruptValues() {
        assertTrue(PasswordHasher.verify("password", "[HASHED]password"));
        assertFalse(PasswordHasher.verify("altra", "[HASHED]password"));
        assertFalse(PasswordHasher.verify("password", "pbkdf2$abc$###$###"));
        assertFalse(PasswordHasher.verify("password", null));
    }

    @Test
    void testDemoUserCanLogInWithHashedSeed() throws Exception {
        assertTrue(new com.ispw.uniride.controller.LoginController()
                .login(TestBeans.user("mario.rossi", "password")));
        new com.ispw.uniride.controller.LoginController().logout();
    }
}
