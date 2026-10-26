package com.ispw.uniride.bean;

/**
 * Classe Bean (DTO) con i soli dati del profilo dell'utente loggato che servono alla View:
 * nome da mostrare e posizione dichiarata. A differenza dell'entità {@code Student} non
 * contiene la password, quindi può attraversare il confine BCE verso la Boundary.
 */
public class ProfileBean {
    private final String username;
    private final String fullName;
    private final String homeLocation;

    public ProfileBean(String username, String fullName, String homeLocation) {
        this.username = username;
        this.fullName = fullName;
        this.homeLocation = homeLocation;
    }

    // Solo getter: il profilo è una fotografia in sola lettura dei dati di sessione.
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getHomeLocation() { return homeLocation; }
}
