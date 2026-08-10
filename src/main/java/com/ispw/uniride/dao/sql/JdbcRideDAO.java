package com.ispw.uniride.dao.sql;

import com.ispw.uniride.dao.RideDAO;
import com.ispw.uniride.model.Ride;
import com.ispw.uniride.model.state.AvailableState;
import com.ispw.uniride.model.state.CancelledState;
import com.ispw.uniride.model.state.CompletedState;
import com.ispw.uniride.model.state.FullState;
import com.ispw.uniride.model.state.RideState;
import com.ispw.uniride.utils.LoggerCustom;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
                + "total_seats, available_seats, base_price, status) KEY (id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = JdbcSupport.getConnection()) {
            // Disattiviamo l'autocommit: il salvataggio del Ride e la ricostruzione della lista
            // passeggeri devono avvenire come un'unica transazione atomica (vedi replacePassengers).
            connection.setAutoCommit(false);
            try (PreparedStatement stmt = connection.prepareStatement(upsertRide)) {
                // I "?" sono parametri legati per posizione: PreparedStatement previene ogni
                // SQL injection, i valori non vengono mai concatenati come stringa nella query.
                stmt.setString(1, ride.getId());
                stmt.setString(2, ride.getDriverUsername());
                stmt.setString(3, ride.getDeparture());
                stmt.setString(4, ride.getDestination());
                stmt.setString(5, ride.getDate());
                stmt.setInt(6, ride.getTotalSeats());
                stmt.setInt(7, ride.getAvailableSeats());
                stmt.setDouble(8, ride.getBasePrice());
                stmt.setString(9, ride.getStatus());
                stmt.executeUpdate();
            }
            replacePassengers(connection, ride);
            // Solo se ENTRAMBE le operazioni sono andate a buon fine si conferma la transazione:
            // in caso di eccezione, nessun commit avviene e il database resta nello stato precedente.
            connection.commit();
        } catch (SQLException e) {
            LoggerCustom.error("Errore nel salvataggio del passaggio su database", e);
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
        try (Connection connection = JdbcSupport.getConnection()) {
            connection.setAutoCommit(false);
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
            connection.commit();
        } catch (SQLException e) {
            LoggerCustom.error("Errore nell'eliminazione del passaggio dal database", e);
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
                    rides.add(mapRow(connection, rs));
                }
            }
        } catch (SQLException e) {
            LoggerCustom.error("Errore nella lettura dei passaggi dal database", e);
        }
        return rides;
    }

    private Ride mapRow(Connection connection, ResultSet rs) throws SQLException {
        // Come per la lettura da CSV: il costruttore pubblico genera un id nuovo e imposta
        // availableSeats=totalSeats, quindi sovrascriviamo subito dopo con i valori reali letti
        // dalla riga del database (setId, setAvailableSeats, setState).
        Ride ride = new Ride(
                rs.getString("driver_username"),
                rs.getString("departure"),
                rs.getString("destination"),
                rs.getString("ride_date"),
                rs.getInt("total_seats"),
                rs.getDouble("base_price")
        );
        ride.setId(rs.getString("id"));
        ride.setAvailableSeats(rs.getInt("available_seats"));
        ride.setState(parseState(rs.getString("status")));

        // Per ogni Ride letto, una query aggiuntiva recupera i suoi passeggeri dalla tabella di
        // collegamento: costa una query in più per riga, accettabile per i volumi di un prototipo.
        try (PreparedStatement stmt = connection.prepareStatement("SELECT username FROM ride_passengers WHERE ride_id = ?")) {
            stmt.setString(1, ride.getId());
            try (ResultSet passengers = stmt.executeQuery()) {
                while (passengers.next()) {
                    ride.addPassengerUsername(passengers.getString("username"));
                }
            }
        }
        return ride;
    }

    private RideState parseState(String status) {
        // Stessa logica di traduzione stringa -> oggetto State usata da FsRideDAO: il database,
        // come il CSV, può persistere solo la sigla testuale, mai un'istanza Java direttamente.
        switch (status) {
            case "AVAILABLE":
                return new AvailableState();
            case "COMPLETED":
                return new CompletedState();
            case "CANCELLED":
                return new CancelledState();
            default:
                return new FullState();
        }
    }
}
