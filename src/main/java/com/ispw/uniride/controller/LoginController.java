package com.ispw.uniride.controller;

import com.ispw.uniride.bean.ProfileBean;
import com.ispw.uniride.bean.RegistrationBean;
import com.ispw.uniride.bean.UserBean;
import com.ispw.uniride.dao.DAOFactory;
import com.ispw.uniride.dao.StudentDAO;
import com.ispw.uniride.exceptions.UserNotAuthorizedException;
import com.ispw.uniride.model.Student;
import com.ispw.uniride.utils.PasswordHasher;

/**
 * Controller Applicativo del Pattern BCE.
 * Gestisce esplicitamente il Caso d'Uso: "Autenticazione / Login Sistema".
 * È responsabile di interagire con i DAO e valutare i dati in entrata dai Beans.
 */
public class LoginController {

    /**
     * Tenta l'accesso al software valutando lo UserBean iniettato dalla View.
     * @param userBean Data Transfer Object contenente solo nome e pass grezzi.
     * @return flag vero o falso in caso di successo o credenziali non idonee.
     */
    public boolean login(UserBean userBean) {

        // Estrazione astratta del DAO, ignorando di fatto che questo sia
        // Memory o FS, l'obiettivo è incapsulare l'API dati (Pattern Factory/DAO).
        StudentDAO studentDAO = DAOFactory.getInstance().getStudentDAO();
        // Lo username arriva già normalizzato dal Bean (spazi iniziali e finali rimossi).
        Student student = studentDAO.getStudentByUsername(userBean.getUsername());

        // Validazione della password rispetto all'hash PBKDF2 memorizzato nell'entità letta dal DAO.
        if (student != null && PasswordHasher.verify(userBean.getPassword(), student.getPassword())) {
            // Promuove il dominio a 'utente loggato nel sistema' grazie al Singleton globale "Session".
            Session.getInstance().setLoggedUser(student);
            return true;
        }

        return false;
    }

    /**
     * Registra un nuovo utente nel sistema salvando la password come hash PBKDF2.
     * Il formato di ogni campo (username, lunghezza della password, telefono, assenza di virgole)
     * è già stato verificato dal Bean: qui resta la sola regola di business dello username univoco.
     * @param registrationBean i dati del modulo di registrazione, già validati sintatticamente.
     * @throws UserNotAuthorizedException se lo username è già in uso.
     */
    public void registerUser(RegistrationBean registrationBean) throws UserNotAuthorizedException {
        StudentDAO studentDAO = DAOFactory.getInstance().getStudentDAO();
        // Un username duplicato è un errore di dominio, non un dettaglio di persistenza:
        // la verifica avviene qui nel Controller Applicativo, non dentro il DAO.
        if (studentDAO.getStudentByUsername(registrationBean.getUsername()) != null) {
            throw new UserNotAuthorizedException("Username già in uso!");
        }

        // Hash PBKDF2 con salt casuale: la password in chiaro non viene mai persistita.
        String hashedPassword = PasswordHasher.hash(registrationBean.getPassword());
        Student newStudent = new Student(registrationBean.getUsername(), hashedPassword, registrationBean.getFullName(),
                registrationBean.getHomeLocation(), registrationBean.getPhoneNumber());
        studentDAO.saveStudent(newStudent);
    }

    /**
     * Espone alla Boundary il profilo dell'utente loggato come Bean, così che la View non debba
     * mai accedere direttamente alla Sessione né all'entità {@code Student} (che contiene la password).
     * @return il profilo dell'utente loggato, oppure {@code null} se nessuno ha effettuato l'accesso.
     */
    public ProfileBean getLoggedUserProfile() {
        Student student = Session.getInstance().getLoggedUser();
        if (student == null) {
            return null;
        }
        return new ProfileBean(student.getUsername(), student.getFullName(), student.getHomeLocation());
    }

    /**
     * Interfaccia d'uscita (Logout) per le chiamate provenienti dalle Dashboard/Menu grafici.
     */
    public void logout() {
        Session.getInstance().logout();
    }
}
