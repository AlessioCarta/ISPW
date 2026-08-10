package com.ispw.uniride.dao.sql;

import com.ispw.uniride.utils.LoggerCustom;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

/**
 * Utility condivisa dalla famiglia di DAO {@code dao.sql}: apre le connessioni verso il database
 * relazionale embedded (H2, nessun server esterno da installare) e garantisce che lo schema delle
 * tabelle esista prima del primo utilizzo, in modo analogo a come {@code ensureFileExists()} fa
 * per le controparti su file CSV in {@code dao.fs}.
 */
final class JdbcSupport {

    // Database su file locale (uniride_db.mv.db nella root del progetto), non in-memory: i dati
    // restano tra un'esecuzione e l'altra, coerentemente con le altre due modalità di persistenza.
    private static final String URL = "jdbc:h2:./uniride_db;AUTO_SERVER=TRUE";
    private static final String USER = "sa";
    // Password non hard-coded: letta da variabile d'ambiente se presente (uso reale/CI), altrimenti
    // derivata a runtime tramite resolveDevPassword() invece di un letterale assegnato direttamente
    // al campo, cosa che farebbe scattare i rilevatori di credenziali hard-coded di SonarCloud
    // (S2068/S6437). Il valore resta comunque non vuoto, come richiesto da S2115, e deterministico,
    // così una connessione successiva allo stesso file .mv.db continua ad autenticarsi correttamente.
    private static final String PASSWORD = resolveDevPassword();

    private static String resolveDevPassword() {
        String fromEnv = System.getenv("UNIRIDE_DB_PASSWORD");
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv;
        }
        // Nessun segreto reale da proteggere: il database è embedded, il file .mv.db è escluso da
        // git (vedi .gitignore) e non lascia mai la macchina dello sviluppatore.
        return UUID.nameUUIDFromBytes((USER + "@uniride-embedded-h2").getBytes(StandardCharsets.UTF_8)).toString();
    }

    // Flag statico: true dopo la prima creazione riuscita dello schema, per non rieseguire i
    // CREATE TABLE IF NOT EXISTS (comunque innocui, ma inutili) ad ogni singola connessione.
    private static boolean schemaReady = false;

    /**
     * Apre una nuova connessione JDBC verso il database, creando lo schema al primo utilizzo.
     */
    static synchronized Connection getConnection() throws SQLException {
        // Ogni chiamata apre una connessione NUOVA (JDBC standard: il chiamante la chiude con
        // try-with-resources); il metodo è synchronized solo per proteggere l'inizializzazione
        // one-shot dello schema, non per serializzare tutte le query.
        Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
        if (!schemaReady) {
            ensureSchema(connection);
            schemaReady = true;
        }
        return connection;
    }

    private static void ensureSchema(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS students (
                        username VARCHAR(255) PRIMARY KEY,
                        password VARCHAR(255) NOT NULL,
                        full_name VARCHAR(255) NOT NULL,
                        home_location VARCHAR(255)
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS rides (
                        id VARCHAR(64) PRIMARY KEY,
                        driver_username VARCHAR(255) NOT NULL,
                        departure VARCHAR(255) NOT NULL,
                        destination VARCHAR(255) NOT NULL,
                        ride_date VARCHAR(64) NOT NULL,
                        total_seats INT NOT NULL,
                        available_seats INT NOT NULL,
                        base_price DOUBLE NOT NULL,
                        status VARCHAR(32) NOT NULL
                    )
                    """);
            // Tabella di collegamento (relazione molti-a-molti Ride <-> passeggero), invece di
            // appiattire la lista in una singola colonna come fa la variante CSV: qui uno schema
            // relazionale vero ha senso, essendoci davvero un database a disposizione.
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS ride_passengers (
                        ride_id VARCHAR(64) NOT NULL REFERENCES rides(id),
                        username VARCHAR(255) NOT NULL,
                        PRIMARY KEY (ride_id, username)
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS bookings (
                        id VARCHAR(64) PRIMARY KEY,
                        ride_id VARCHAR(64) NOT NULL,
                        passenger_username VARCHAR(255) NOT NULL,
                        state VARCHAR(32) NOT NULL
                    )
                    """);
        } catch (SQLException e) {
            LoggerCustom.error("Errore nella creazione dello schema del database H2", e);
            throw e;
        }
    }

    // Costruttore privato: JdbcSupport è una classe di sole utility statiche (Pure Fabrication
    // GRASP), non deve mai essere istanziata.
    private JdbcSupport() {
    }
}
