package com.ispw.uniride.controller;

import com.ispw.uniride.bean.UserBean;
import com.ispw.uniride.dao.DAOFactory;
import com.ispw.uniride.dao.StudentDAO;
import com.ispw.uniride.model.Student;

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
        Student student = studentDAO.getStudentByUsername(userBean.getUsername());

        // Validazione della password rispetto all'oggetto reale estratto in fase DAO (Entity)
        if (student != null && student.getPassword().equals(userBean.getPassword())) {
            // Promuove il dominio a 'utente loggato nel sistema' grazie al Singleton globale "Session".
            Session.getInstance().setLoggedUser(student);
            return true;
        }

        return false;
    }

    /**
     * Interfaccia d'uscita (Logout) per le chiamate provenienti dalle Dashboard/Menu grafici.
     */
    public void logout() {
        Session.getInstance().logout();
    }
}
