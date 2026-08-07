package com.ispw.uniride.dao.sql;

import com.ispw.uniride.utils.LoggerCustom;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

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
    // Password non vuota: anche trattandosi di un DB embedded locale (mai esposto in rete se non
    // sulla loopback via AUTO_SERVER), una credenziale vuota è comunque segnalata da SonarCloud
    // come cattiva pratica (S2115) e va evitata a prescindere dal contesto di deployment.
    private static final String PASSWORD = "uniride-local-dev-only";

    private static boolean schemaReady = false;

    /**
     * Apre una nuova connessione JDBC verso il database, creando lo schema al primo utilizzo.
     */
    static synchronized Connection getConnection() throws SQLException {
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

    private JdbcSupport() {
    }
}
