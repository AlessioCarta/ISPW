package com.ispw.uniride.dao.sql;

import com.ispw.uniride.dao.RideDAO;
import com.ispw.uniride.exceptions.PersistenceException;
import com.ispw.uniride.model.Ride;
import com.ispw.uniride.model.state.RideState;
import com.ispw.uniride.utils.LoggerCustom;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementazione DAO su database relazionale (H2, via JDBC standard) per l'entità {@link Ride}.
 * A differenza della variante CSV, i passeggeri non vengono appiattiti in una colonna: vivono
 * nella tabella di collegamento {@code ride_passengers}, una relazione molti-a-molti vera.
 */
public class JdbcRideDAO implements RideDAO {

    @Override
    public void saveRide(Ride ride) {
        // MERGE INTO ... KEY (id): sintassi di upsert propria di H2. Se una riga con quell'id
        // esiste già viene aggiornata, altrimenti viene inserita — un'unica istruzione al posto
        // di un SELECT preventivo per decidere fra INSERT e UPDATE.
        String upsertRide = "MERGE INTO rides (id, driver_username, departure, destination, ride_date, "
                + "departure_time, total_seats, available_seats, base_price, status) KEY (id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        // Il salvataggio del Ride e la ricostruzione della lista passeggeri devono avvenire come
        // un'unica transazione atomica (vedi replacePassengers e inTransaction).
        try {
            inTransaction(connection -> {
                executeRideUpsert(connection, upsertRide, ride);
                replacePassengers(connection, ride);
            });
        } catch (SQLException e) {
            LoggerCustom.error("Errore nel salvataggio del passaggio su database", e);
            throw new PersistenceException("Impossibile salvare il passaggio: database non disponibile.", e);
        }
    }

    private void executeRideUpsert(Connection connection, String upsertRide, Ride ride) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(upsertRide)) {
            // I "?" sono parametri legati per posizione: PreparedStatement previene ogni
            // SQL injection, i valori non vengono mai concatenati come stringa nella query.
            stmt.setString(1, ride.getId());
            stmt.setString(2, ride.getDriverUsername());
            stmt.setString(3, ride.getDeparture());
            stmt.setString(4, ride.getDestination());
            stmt.setString(5, ride.getDate());
            stmt.setString(6, ride.getDepartureTime());
            stmt.setInt(7, ride.getTotalSeats());
            stmt.setInt(8, ride.getAvailableSeats());
            stmt.setDouble(9, ride.getBasePrice());
            stmt.setString(10, ride.getStatus());
            stmt.executeUpdate();
        }
    }

    /**
     * Ricrea l'elenco dei passeggeri di un passaggio: più semplice e meno soggetto a errori che
     * calcolare un diff, dato il volume di dati previsto da un prototipo come questo.
     */
    private void replacePassengers(Connection connection, Ride ride) throws SQLException {
        // Prima si cancella tutto il collegamento esistente per questo Ride...
        try (PreparedStatement delete = connection.prepareStatement("DELETE FROM ride_passengers WHERE ride_id = ?")) {
            delete.setString(1, ride.getId());
            delete.executeUpdate();
        }
        // ...poi si reinseriscono, in un'unica batch, tutti i passeggeri attualmente presenti
        // nell'oggetto Ride in memoria: il risultato finale è sempre coerente con lo stato Java,
        // senza dover calcolare quali righe aggiungere/rimuovere rispetto a prima.
        String insert = "INSERT INTO ride_passengers (ride_id, username) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(insert)) {
            for (String username : ride.getPassengerUsernames()) {
                stmt.setString(1, ride.getId());
                stmt.setString(2, username);
                // addBatch() accoda l'istruzione invece di eseguirla subito: eseguendole tutte
                // insieme con executeBatch() si riduce il numero di round-trip verso il database.
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    @Override
    public List<Ride> getAllRides() {
        // Nessun parametro da legare: il binder è un lambda "vuoto" che non fa nulla.
        return queryRides("SELECT * FROM rides", stmt -> { });
    }

    @Override
    public List<Ride> getAvailableRides(String departure, String destination) {
        // UPPER(...) = UPPER(?) replica il confronto case-insensitive che nelle altre famiglie
        // di DAO si ottiene con equalsIgnoreCase() lato Java: qui il confronto avviene lato database.
        String sql = "SELECT * FROM rides WHERE status = 'AVAILABLE' AND UPPER(departure) = UPPER(?) AND UPPER(destination) = UPPER(?)";
        return queryRides(sql, stmt -> {
            stmt.setString(1, departure);
            stmt.setString(2, destination);
        });
    }

    @Override
    public Ride getRideById(String id) {
        List<Ride> results = queryRides("SELECT * FROM rides WHERE id = ?", stmt -> stmt.setString(1, id));
        // L'id è chiave primaria: la query può ritornare al più una riga. Se la lista è vuota,
        // nessun Ride ha quell'id, e ritorniamo null coerentemente col contratto di RideDAO.
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public void updateRide(Ride ride) {
        // Su questo storage un aggiornamento è un upsert come il salvataggio iniziale.
        saveRide(ride);
    }

    @Override
    public List<Ride> getRidesByDriver(String username) {
        return queryRides("SELECT * FROM rides WHERE driver_username = ?", stmt -> stmt.setString(1, username));
    }

    @Override
    public List<Ride> getRidesByPassenger(String username) {
        // JOIN con la tabella di collegamento: trova tutti i Ride per cui esiste una riga in
        // ride_passengers con quello username, sfruttando davvero la relazione molti-a-molti.
        String sql = "SELECT r.* FROM rides r JOIN ride_passengers p ON r.id = p.ride_id WHERE p.username = ?";
        return queryRides(sql, stmt -> stmt.setString(1, username));
    }

    @Override
    public void deleteRide(String id) {
        try {
            inTransaction(connection -> deleteRideRows(connection, id));
        } catch (SQLException e) {
            LoggerCustom.error("Errore nell'eliminazione del passaggio dal database", e);
            throw new PersistenceException("Impossibile eliminare il passaggio: database non disponibile.", e);
        }
    }

    private void deleteRideRows(Connection connection, String id) throws SQLException {
        // Si cancellano prima le righe collegate in ride_passengers (che referenziano
        // rides.id via foreign key) e solo dopo la riga in rides stessa: l'ordine inverso
        // fallirebbe per violazione del vincolo di integrità referenziale.
        try (PreparedStatement deletePassengers = connection.prepareStatement("DELETE FROM ride_passengers WHERE ride_id = ?")) {
            deletePassengers.setString(1, id);
            deletePassengers.executeUpdate();
        }
        try (PreparedStatement deleteRide = connection.prepareStatement("DELETE FROM rides WHERE id = ?")) {
            deleteRide.setString(1, id);
            deleteRide.executeUpdate();
        }
    }

    /**
     * Operazione da eseguire su una connessione già aperta, all'interno di una transazione.
     */
    @FunctionalInterface
    private interface TransactionWork {
        void run(Connection connection) throws SQLException;
    }

    /**
     * Esegue le operazioni indicate in un'unica transazione: o vengono confermate tutte, o nessuna.
     */
    private void inTransaction(TransactionWork work) throws SQLException {
        try (Connection connection = JdbcSupport.getConnection()) {
            connection.setAutoCommit(false);
            commitOrRollback(connection, work);
        }
    }

    private void commitOrRollback(Connection connection, TransactionWork work) throws SQLException {
        try {
            work.run(connection);
            // Solo se TUTTE le operazioni sono andate a buon fine si conferma la transazione.
            connection.commit();
        } catch (SQLException e) {
            // Rollback esplicito: lo standard JDBC non garantisce cosa accada alle modifiche
            // non confermate alla chiusura della connessione, quindi non ci si affida a close().
            connection.rollback();
            throw e;
        }
    }

    /**
     * Interfaccia funzionale minima per parametrizzare una PreparedStatement senza duplicare
     * l'apertura/chiusura di connessione e ResultSet in ogni metodo di interrogazione.
     */
    @FunctionalInterface
    private interface StatementBinder {
        void bind(PreparedStatement stmt) throws SQLException;
    }

    private List<Ride> queryRides(String sql, StatementBinder binder) {
        List<Ride> rides = new ArrayList<>();
        // try-with-resources su connessione, statement e (più sotto) result set: tutte le
        // risorse JDBC vengono chiuse automaticamente anche in caso di eccezione.
        try (Connection connection = JdbcSupport.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            // Il binder inietta i parametri specifici di ogni query (o non fa nulla, se la
            // query non ne ha): questo metodo resta identico per tutte le query di lettura.
            binder.bind(stmt);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    rides.add(mapRow(rs));
                }
            }
            loadPassengers(connection, rides);
        } catch (SQLException e) {
            LoggerCustom.error("Errore nella lettura dei passaggi dal database", e);
            throw new PersistenceException("Impossibile leggere i passaggi: database non disponibile.", e);
        }
        return rides;
    }

    private Ride mapRow(ResultSet rs) throws SQLException {
        // Come per la lettura da CSV: il costruttore pubblico genera un id nuovo e imposta
        // availableSeats=totalSeats, quindi sovrascriviamo subito dopo con i valori reali letti
        // dalla riga del database (setId, setAvailableSeats, setState).
        Ride ride = new Ride(
                rs.getString("driver_username"),
                rs.getString("departure"),
                rs.getString("destination"),
                rs.getString("ride_date"),
                rs.getInt("total_seats"),
                rs.getDouble("base_price"),
                rs.getString("departure_time")
        );
        ride.setId(rs.getString("id"));
        ride.setAvailableSeats(rs.getInt("available_seats"));
        ride.setState(RideState.fromStatus(rs.getString("status")));
        return ride;
    }

    /**
     * Recupera i passeggeri di tutti i Ride letti con un'unica query sulla tabella di collegamento,
     * invece di una query aggiuntiva per ogni Ride (problema "N+1").
     */
    private void loadPassengers(Connection connection, List<Ride> rides) throws SQLException {
        if (rides.isEmpty()) {
            return;
        }
        Map<String, Ride> byId = new HashMap<>();
        for (Ride ride : rides) {
            byId.put(ride.getId(), ride);
        }
        // Tutti gli id passano in un solo parametro di tipo array (= ANY(?)): la query resta un
        // testo fisso, senza segnaposto generati a runtime, e i valori non vengono mai concatenati.
        String sql = "SELECT ride_id, username FROM ride_passengers WHERE ride_id = ANY(?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setArray(1, connection.createArrayOf("VARCHAR", byId.keySet().toArray()));
            try (ResultSet passengers = stmt.executeQuery()) {
                while (passengers.next()) {
                    byId.get(passengers.getString("ride_id")).addPassengerUsername(passengers.getString("username"));
                }
            }
        }
    }
}
