package com.ispw.uniride.model;

/**
 * Entità (Entity) principale del dominio applicativo.
 * Modella lo Studente universitario, che funge da attore base nel sistema.
 * Può agire sia da Guidatore (chi offre) sia da Passeggero (chi prenota).
 */
public class Student {
    // Identificativo univoco (es. email universitaria o matricola usata come handle)
    private String username;

    // Chiave di accesso, salvata in chiaro (cleartext) unicamente a scopo di simulazione prototipale
    private String password;

    // Nome e cognome per le funzioni di rubrica e visualizzazione profilo
    private String fullName;

    /**
     * Costruttore completo per istanziare un nuovo Studente a livello di logica di business.
     * @param username l'id dello studente, usato anche per i login.
     * @param password la credenziale testuale di questo attore.
     * @param fullName il nominativo da presentare nell'interfaccia agli altri compagni.
     */
    public Student(String username, String password, String fullName) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
    }

    // Metodi Getter canonici delle Entity
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
}
