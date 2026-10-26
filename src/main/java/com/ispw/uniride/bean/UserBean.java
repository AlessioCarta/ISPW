package com.ispw.uniride.bean;

import com.ispw.uniride.exceptions.InvalidInputException;

/**
 * Classe Bean (DTO - Data Transfer Object) per racchiudere e proteggere
 * i dati dell'utente dal livello di View/Boundary a quello di Logica applicativa (Controller).
 * In questo modo il Controller e la Boundary non si inviano dati base separatamente (es. String username, String pass),
 * ma comunicano formalmente con il DTO incapsulato.
 * Come ogni Bean di input ha solo il costruttore vuoto: i dati entrano dai setter, che ne
 * controllano la forma (campi obbligatori), mai le regole di business.
 */
public class UserBean {
    private String username;
    private String password;

    /**
     * @param username identificativo inserito: gli spazi iniziali e finali vengono rimossi, così
     * uno spazio digitato per errore non impedisce l'accesso.
     * @throws InvalidInputException se lo username è vuoto.
     */
    public void setUsername(String username) throws InvalidInputException {
        this.username = BeanFields.required(username, "Inserisci lo username.");
    }

    /**
     * @param password chiave di sicurezza inserita, conservata così com'è (anche gli spazi contano).
     * @throws InvalidInputException se la password è vuota.
     */
    public void setPassword(String password) throws InvalidInputException {
        if (password == null || password.isEmpty()) {
            throw new InvalidInputException("Inserisci la password.");
        }
        this.password = password;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
}
