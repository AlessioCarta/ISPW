package com.ispw.uniride.dao.sql;

import com.ispw.uniride.dao.StudentDAO;
import com.ispw.uniride.model.Student;
import com.ispw.uniride.utils.LoggerCustom;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Implementazione DAO su database relazionale (H2, via JDBC standard) per l'entità {@link Student}.
 * Segue lo stesso contratto delle controparti Memory/File System: nessuna logica di dominio o
 * validazione sintattica qui dentro, solo traduzione fra righe di tabella e oggetti Model.
 */
public class JdbcStudentDAO implements StudentDAO {

    @Override
    public void saveStudent(Student student) {
        // MERGE INTO ... KEY (username): upsert atomico, username come chiave di unicità (uno
        // studente non può registrarsi due volte con lo stesso username).
        String sql = "MERGE INTO students (username, password, full_name, home_location) KEY (username) VALUES (?, ?, ?, ?)";
        try (Connection connection = JdbcSupport.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, student.getUsername());
            stmt.setString(2, student.getPassword());
            stmt.setString(3, student.getFullName());
            // home_location può essere null (studente che non ha dichiarato la posizione):
            // setString(null) su H2 scrive correttamente un NULL SQL, non lancia eccezione.
            stmt.setString(4, student.getHomeLocation());
            stmt.executeUpdate();
        } catch (SQLException e) {
            LoggerCustom.error("Errore nel salvataggio dello studente su database", e);
        }
    }

    @Override
    public Student getStudentByUsername(String username) {
        String sql = "SELECT username, password, full_name, home_location FROM students WHERE username = ?";
        try (Connection connection = JdbcSupport.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                // username è chiave primaria: al più una riga di risultato. rs.next() sposta il
                // cursore sulla prima riga e ritorna true solo se esiste.
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            LoggerCustom.error("Errore nella lettura dello studente dal database", e);
        }
        // Nessuna riga trovata (o errore SQL): ritorna null, come da contratto di StudentDAO,
        // interpretato da LoginController come "utente non trovato".
        return null;
    }

    private Student mapRow(ResultSet rs) throws SQLException {
        return new Student(
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("full_name"),
                rs.getString("home_location")
        );
    }
}
