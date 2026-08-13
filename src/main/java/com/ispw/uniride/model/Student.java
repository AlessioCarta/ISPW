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

    // Comune/paese dichiarato dallo studente in fase di registrazione (facoltativo). Usato per
    // suggerire le località di partenza più vicine e l'ateneo più prossimo in Offri/Cerca Passaggio.
    private String homeLocation;

    // Numero di telefono dichiarato dallo studente (facoltativo). Visibile solo alla controparte
    // di una richiesta di passaggio già esistente (mai in un elenco pubblico di ricerca), così i
    // due studenti possono scriversi per accordarsi sul punto d'incontro esatto.
    private String phoneNumber;

    /**
     * Costruttore breve mantenuto per compatibilità con il codice preesistente (dati mock,
     * lettura di righe CSV storiche senza colonna posizione). Posizione e telefono restano non impostati.
     * @param username l'id dello studente, usato anche per i login.
     * @param password la credenziale testuale di questo attore.
     * @param fullName il nominativo da presentare nell'interfaccia agli altri compagni.
     */
    public Student(String username, String password, String fullName) {
        // Delega al costruttore completo con homeLocation e phoneNumber assenti (null): niente
        // duplicazione della logica di inizializzazione fra le varianti overloaded.
        this(username, password, fullName, null, null);
    }

    /**
     * Costruttore intermedio mantenuto per compatibilità con il codice preesistente (letture CSV
     * precedenti all'introduzione del numero di telefono). Il telefono resta non impostato.
     * @param username l'id dello studente, usato anche per i login.
     * @param password la credenziale testuale di questo attore.
     * @param fullName il nominativo da presentare nell'interfaccia agli altri compagni.
     * @param homeLocation il comune/paese di residenza dichiarato, o {@code null} se non fornito.
     */
    public Student(String username, String password, String fullName, String homeLocation) {
        this(username, password, fullName, homeLocation, null);
    }

    /**
     * Costruttore completo per istanziare un nuovo Studente a livello di logica di business.
     * @param username l'id dello studente, usato anche per i login.
     * @param password la credenziale testuale di questo attore.
     * @param fullName il nominativo da presentare nell'interfaccia agli altri compagni.
     * @param homeLocation il comune/paese di residenza dichiarato, o {@code null} se non fornito.
     * @param phoneNumber il numero di telefono dichiarato, o {@code null} se non fornito.
     */
    public Student(String username, String password, String fullName, String homeLocation, String phoneNumber) {
        // Assegnazione diretta dei campi: nessuna validazione qui, perché non è compito
        // dell'Entity ma del Controller Grafico/Applicativo (regola BCE: niente controlli
        // sintattici fuori dal proprio strato).
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.homeLocation = homeLocation;
        this.phoneNumber = phoneNumber;
    }

    // Metodi Getter canonici delle Entity
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
    public String getHomeLocation() { return homeLocation; }
    public void setHomeLocation(String homeLocation) { this.homeLocation = homeLocation; }
    public String getPhoneNumber() { return phoneNumber; }
}
