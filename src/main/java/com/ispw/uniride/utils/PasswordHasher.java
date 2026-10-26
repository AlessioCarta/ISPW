package com.ispw.uniride.utils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility per l'hashing delle password con PBKDF2 (incluso nel JDK, nessuna libreria esterna).
 * Formato memorizzato: {@code pbkdf2$<iterazioni>$<salt Base64>$<hash Base64>}. Il formato non
 * contiene virgole, quindi resta compatibile anche con la persistenza su file CSV.
 */
public final class PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA512";
    private static final String PREFIX = "pbkdf2";
    // Numero di iterazioni raccomandato da OWASP per PBKDF2-HMAC-SHA512.
    private static final int ITERATIONS = 210_000;
    private static final int SALT_BYTES = 16;
    private static final int KEY_BITS = 256;
    // Formato della versione prototipale precedente: accettato solo in verifica, così gli account
    // già salvati su file o database continuano a funzionare.
    private static final String LEGACY_PREFIX = "[HASHED]";

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Calcola l'hash di una password con un salt casuale nuovo ad ogni chiamata.
     */
    public static String hash(String rawPassword) {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] derived = derive(rawPassword, salt, ITERATIONS);
        Base64.Encoder encoder = Base64.getEncoder();
        return PREFIX + "$" + ITERATIONS + "$" + encoder.encodeToString(salt) + "$" + encoder.encodeToString(derived);
    }

    /**
     * Verifica una password rispetto al valore memorizzato. Il confronto avviene in tempo costante
     * ({@link MessageDigest#isEqual}), per non rivelare quanti byte iniziali coincidono.
     */
    public static boolean verify(String rawPassword, String stored) {
        if (rawPassword == null || stored == null) {
            return false;
        }
        if (stored.startsWith(LEGACY_PREFIX)) {
            return MessageDigest.isEqual((LEGACY_PREFIX + rawPassword).getBytes(StandardCharsets.UTF_8),
                    stored.getBytes(StandardCharsets.UTF_8));
        }
        String[] parts = stored.split("\\$");
        if (parts.length != 4 || !PREFIX.equals(parts[0])) {
            return false;
        }
        try {
            int iterations = Integer.parseInt(parts[1]);
            Base64.Decoder decoder = Base64.getDecoder();
            byte[] salt = decoder.decode(parts[2]);
            byte[] expected = decoder.decode(parts[3]);
            return MessageDigest.isEqual(derive(rawPassword, salt, iterations), expected);
        } catch (IllegalArgumentException _) {
            // Iterazioni non numeriche o Base64 non valido: valore memorizzato corrotto.
            return false;
        }
    }

    private static byte[] derive(String rawPassword, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(rawPassword.toCharArray(), salt, iterations, KEY_BITS);
        try {
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            // PBKDF2WithHmacSHA512 è garantito da ogni JDK moderno: se manca, è un errore di ambiente.
            throw new IllegalStateException("Algoritmo " + ALGORITHM + " non disponibile", e);
        } finally {
            spec.clearPassword();
        }
    }

    private PasswordHasher() {
    }
}
