package com.ispw.uniride.controller;

import com.ispw.uniride.bean.UserBean;
import com.ispw.uniride.dao.DAOFactory;
import com.ispw.uniride.dao.StudentDAO;
import com.ispw.uniride.model.Student;

import java.util.regex.Pattern;

/**
 * Controller Applicativo del Pattern BCE.
 * Gestisce esplicitamente il Caso d'Uso: "Autenticazione / Login Sistema".
 * È responsabile di interagire con i DAO e valutare i dati in entrata dai Beans.
 */
public class LoginController {

    // Formato permissivo: un eventuale "+" iniziale (prefisso internazionale) seguito da 6-20
    // caratteri fra cifre e spazi. Non convalida che il numero sia realmente raggiungibile (fuori
    // portata di un prototipo), solo che "assomigli" a un numero di telefono plausibile.
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9 ]{6,20}$");

    /**
     * Tenta l'accesso al software valutando lo UserBean iniettato dalla View.
     * @param userBean Data Transfer Object contenente solo nome e pass grezzi.
     * @return flag vero o falso in caso di successo o credenziali non idonee.
     */
    public boolean login(UserBean userBean) {

        // Estrazione astratta del DAO, ignorando di fatto che questo sia
        // Memory o FS, l'obiettivo è incapsulare l'API dati (Pattern Factory/DAO).
        StudentDAO studentDAO = DAOFactory.getInstance().getStudentDAO();
        Student student = studentDAO.getStudentByUsername(userBean.getUsername());

        // Validazione della password rispetto all'oggetto reale estratto in fase DAO (Entity)
        // Utilizziamo l'hash fittizio per simulare la verifica in chiaro rispetto al db cifrato.
        String fakeHashToCheck = "[HASHED]" + userBean.getPassword();
        if (student != null && student.getPassword().equals(fakeHashToCheck)) {
            // Promuove il dominio a 'utente loggato nel sistema' grazie al Singleton globale "Session".
            Session.getInstance().setLoggedUser(student);
            return true;
        }

        return false;
    }

    /**
     * Registra un nuovo utente nel sistema simulando il salvataggio sicuro (Hash fittizio per prototipo).
     * Overload mantenuto per compatibilità (es. CLI) quando posizione e telefono non sono richiesti.
     * @throws com.ispw.uniride.exceptions.UserNotAuthorizedException se l'username è duplicato.
     */
    public void registerUser(String username, String rawPassword, String fullName) throws com.ispw.uniride.exceptions.UserNotAuthorizedException {
        registerUser(username, rawPassword, fullName, null, null);
    }

    /**
     * Registra un nuovo utente nel sistema simulando il salvataggio sicuro (Hash fittizio per prototipo),
     * memorizzando anche il comune/paese di residenza dichiarato per i suggerimenti di vicinanza.
     * Overload mantenuto per compatibilità quando il telefono non è richiesto.
     * @param homeLocation il comune dichiarato dallo studente, o {@code null}/vuoto se non fornito.
     * @throws com.ispw.uniride.exceptions.UserNotAuthorizedException se l'username è duplicato.
     */
    public void registerUser(String username, String rawPassword, String fullName, String homeLocation) throws com.ispw.uniride.exceptions.UserNotAuthorizedException {
        registerUser(username, rawPassword, fullName, homeLocation, null);
    }

    /**
     * Registra un nuovo utente nel sistema simulando il salvataggio sicuro (Hash fittizio per prototipo),
     * memorizzando anche il comune/paese di residenza e il numero di telefono dichiarati.
     * @param homeLocation il comune dichiarato dallo studente, o {@code null}/vuoto se non fornito.
     * @param phoneNumber il numero di telefono dichiarato, o {@code null}/vuoto se non fornito: resta
     * facoltativo come homeLocation, ma se presente deve avere un formato plausibile, perché è
     * proprio ciò che permette alla controparte di una richiesta di passaggio di contattarlo.
     * @throws com.ispw.uniride.exceptions.UserNotAuthorizedException se l'username è duplicato,
     * la password è troppo corta o il telefono ha un formato non valido.
     */
    public void registerUser(String username, String rawPassword, String fullName, String homeLocation, String phoneNumber) throws com.ispw.uniride.exceptions.UserNotAuthorizedException {
        StudentDAO studentDAO = DAOFactory.getInstance().getStudentDAO();
        // Un username duplicato è un errore di dominio, non un dettaglio di persistenza:
        // la verifica avviene qui nel Controller Applicativo, non dentro il DAO.
        if (studentDAO.getStudentByUsername(username) != null) {
            throw new com.ispw.uniride.exceptions.UserNotAuthorizedException("Username già in uso!");
        }

        // Vincolo minimo di robustezza della password, applicato prima di procedere all'hashing.
        if (rawPassword == null || rawPassword.length() < 6) {
            throw new com.ispw.uniride.exceptions.UserNotAuthorizedException("La password deve essere di almeno 6 caratteri.");
        }

        // Mock Hashing per requisiti di sicurezza simulati.
        String hashedPassword = "[HASHED]" + rawPassword;

        // Una stringa vuota o solo spazi viene trattata come "nessuna posizione dichiarata"
        // (null), coerentemente con come home_location viene salvata/letta nei DAO.
        String normalizedLocation = (homeLocation == null || homeLocation.trim().isEmpty()) ? null : homeLocation.trim();

        // Stessa normalizzazione per il telefono: facoltativo, ma se presente deve rispettare un
        // formato plausibile, altrimenti la funzionalità di contatto fra studenti non avrebbe senso.
        String normalizedPhone = (phoneNumber == null || phoneNumber.trim().isEmpty()) ? null : phoneNumber.trim();
        if (normalizedPhone != null && !PHONE_PATTERN.matcher(normalizedPhone).matches()) {
            throw new com.ispw.uniride.exceptions.UserNotAuthorizedException("Numero di telefono non valido: usa solo cifre, spazi ed eventualmente un + iniziale.");
        }

        Student newStudent = new Student(username, hashedPassword, fullName, normalizedLocation, normalizedPhone);
        studentDAO.saveStudent(newStudent);
    }

    /**
     * Interfaccia d'uscita (Logout) per le chiamate provenienti dalle Dashboard/Menu grafici.
     */
    public void logout() {
        Session.getInstance().logout();
    }
}
