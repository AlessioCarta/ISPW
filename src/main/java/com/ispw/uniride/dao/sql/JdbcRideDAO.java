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
        String upsertRide = "MERGE INTO rides (id, driver_username, departure, destination, ride_date, "
                + "total_seats, available_seats, base_price, status) KEY (id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = JdbcSupport.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement stmt = connection.prepareStatement(upsertRide)) {
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
        try (PreparedStatement delete = connection.prepareStatement("DELETE FROM ride_passengers WHERE ride_id = ?")) {
            delete.setString(1, ride.getId());
            delete.executeUpdate();
        }
        String insert = "INSERT INTO ride_passengers (ride_id, username) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(insert)) {
            for (String username : ride.getPassengerUsernames()) {
                stmt.setString(1, ride.getId());
                stmt.setString(2, username);
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    @Override
    public List<Ride> getAllRides() {
        return queryRides("SELECT * FROM rides", stmt -> { });
    }

    @Override
    public List<Ride> getAvailableRides(String departure, String destination) {
        String sql = "SELECT * FROM rides WHERE status = 'AVAILABLE' AND UPPER(departure) = UPPER(?) AND UPPER(destination) = UPPER(?)";
        return queryRides(sql, stmt -> {
            stmt.setString(1, departure);
            stmt.setString(2, destination);
        });
    }

    @Override
    public Ride getRideById(String id) {
        List<Ride> results = queryRides("SELECT * FROM rides WHERE id = ?", stmt -> stmt.setString(1, id));
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
        String sql = "SELECT r.* FROM rides r JOIN ride_passengers p ON r.id = p.ride_id WHERE p.username = ?";
        return queryRides(sql, stmt -> stmt.setString(1, username));
    }

    @Override
    public void deleteRide(String id) {
        try (Connection connection = JdbcSupport.getConnection()) {
            connection.setAutoCommit(false);
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
        try (Connection connection = JdbcSupport.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
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
