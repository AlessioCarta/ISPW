package com.ispw.uniride.bean;

/**
 * Classe Bean (DTO - Data Transfer Object) per racchiudere e proteggere
 * i dati dell'utente dal livello di View/Boundary a quello di Logica applicativa (Controller).
 * In questo modo il Controller e la Boundary non si inviano dati base separatamente (es. String username, String pass),
 * ma comunicano formalmente con il DTO incapsulato.
 */
public class UserBean {
    private String username;
    private String password;

    /**
     * Costruttore base per racchiudere l'autenticazione inviata dall'interfaccia utente.
     * @param username identificativo inserito.
     * @param password chiave di sicurezza inserita.
     */
    public UserBean(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Metodi Getter e Setter
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
