package com.ispw.uniride.controller;

import com.ispw.uniride.model.Student;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Singleton Pattern dedicato alla Sessione dell'Utente attuale in locale.
 * Sostituisce i meccanismi classici web "Servlet" o "JWT Tokens" immagazzinando l'attore
 * in memoria RAM centrale per tutte le Boundary/Views e Controllers.
 */
public class Session {

    // Riferimento interno all'unica istanza ammessa nel sistema
    private static Session instance = null;

    // Entità che ha eseguito il login con successo nella schermata. AtomicReference: il valore scritto
    // al login dal thread della GUI dev'essere subito visibile anche ad eventuali altri thread.
    // La Sessione resta unica per processo: UniRide è un'applicazione desktop mono-utente.
    private final AtomicReference<Student> loggedUser = new AtomicReference<>();

    /**
     * Costruttore bloccato privato per prevenire istanziazioni `new Session()` indesiderate.
     */
    private Session() {}

    /**
     * Ritorna l'oggetto Singleton universale, istanziato thread-safe (`synchronized`) in
     * maniera lazy solo quando il primo Controller lo invoca.
     */
    public static synchronized Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    // Memorizza lo studente una volta verificato il token d'accesso (es. dal `LoginController`)
    public void setLoggedUser(Student user) {
        this.loggedUser.set(user);
    }

    // Permette alle interfacce Dashboard o ai vari `SearchRideController` di
    // conoscere dinamicamente il soggetto delle operazioni (l'attore preponente).
    public Student getLoggedUser() {
        return loggedUser.get();
    }

    // Reset brutale dei dati in memoria qualora venga premuto il tasto disconnessione (Logout)
    public void logout() {
        this.loggedUser.set(null);
    }
}
