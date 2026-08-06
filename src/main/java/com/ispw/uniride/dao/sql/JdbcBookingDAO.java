package com.ispw.uniride.dao.sql;

import com.ispw.uniride.dao.BookingDAO;
import com.ispw.uniride.model.Booking;
import com.ispw.uniride.utils.LoggerCustom;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementazione DAO su database relazionale (H2, via JDBC standard) per l'entità {@link Booking}.
 */
public class JdbcBookingDAO implements BookingDAO {

    @Override
    public void saveBooking(Booking booking) {
        upsert(booking);
    }

    @Override
    public Booking getBookingById(String id) {
        List<Booking> results = query("SELECT * FROM bookings WHERE id = ?", stmt -> stmt.setString(1, id));
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<Booking> getBookingsByRide(String rideId) {
        return query("SELECT * FROM bookings WHERE ride_id = ?", stmt -> stmt.setString(1, rideId));
    }

    @Override
    public List<Booking> getBookingsByPassenger(String passengerUsername) {
        return query("SELECT * FROM bookings WHERE passenger_username = ?", stmt -> stmt.setString(1, passengerUsername));
    }

    @Override
    public void updateBooking(Booking booking) {
        // Su questo storage un aggiornamento è un upsert come il salvataggio iniziale.
        upsert(booking);
    }

    private void upsert(Booking booking) {
        String sql = "MERGE INTO bookings (id, ride_id, passenger_username, state) KEY (id) VALUES (?, ?, ?, ?)";
        try (Connection connection = JdbcSupport.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, booking.getId());
            stmt.setString(2, booking.getRideId());
            stmt.setString(3, booking.getPassengerUsername());
            stmt.setString(4, booking.getState());
            stmt.executeUpdate();
        } catch (SQLException e) {
            LoggerCustom.error("Errore nel salvataggio della prenotazione su database", e);
        }
    }

    @FunctionalInterface
    private interface StatementBinder {
        void bind(PreparedStatement stmt) throws SQLException;
    }

    private List<Booking> query(String sql, StatementBinder binder) {
        List<Booking> bookings = new ArrayList<>();
        try (Connection connection = JdbcSupport.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            binder.bind(stmt);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    bookings.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LoggerCustom.error("Errore nella lettura delle prenotazioni dal database", e);
        }
        return bookings;
    }

    private Booking mapRow(ResultSet rs) throws SQLException {
        Booking booking = new Booking(rs.getString("ride_id"), rs.getString("passenger_username"));
        booking.setId(rs.getString("id"));
        applyState(booking, rs.getString("state"));
        return booking;
    }

    /**
     * Riporta un Booking appena ricostruito dalla riga di tabella allo stato persistito, rigiocando
     * la transizione corrispondente (il costruttore parte sempre da REQUESTED).
     */
    private void applyState(Booking booking, String state) {
        if (Booking.CONFIRMED.equals(state)) {
            booking.confirm();
        } else if (Booking.REJECTED.equals(state)) {
            booking.reject();
        } else if (Booking.CANCELLED.equals(state)) {
            booking.cancel();
        }
        // REQUESTED: nessuna transizione necessaria, è lo stato iniziale del costruttore.
    }
}
