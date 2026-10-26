package com.ispw.uniride.bean;

import com.ispw.uniride.exceptions.InvalidInputException;

import java.util.regex.Pattern;

/**
 * Bean di input del caso d'uso "Registrazione": raccoglie i dati del modulo e ne controlla la
 * forma. Le regole di business (es. username già in uso) restano nel Controller Applicativo.
 */
public class RegistrationBean {

    // Username di 3-50 caratteri fra lettere, cifre, punto, trattino e underscore: esclude spazi
    // (che renderebbero "mario.rossi " un account diverso da "mario.rossi") e virgole.
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9._-]{3,50}$");
    // Formato permissivo: un eventuale "+" iniziale (prefisso internazionale) seguito da 6-20
    // caratteri fra cifre e spazi. Non convalida che il numero sia realmente raggiungibile.
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9 ]{6,20}$");
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_NAME_LENGTH = 100;

    private String username;
    private String password;
    private String fullName;
    private String homeLocation;
    private String phoneNumber;

    public void setUsername(String username) throws InvalidInputException {
        String value = BeanFields.required(username, "Inserisci lo username.");
        if (!USERNAME_PATTERN.matcher(value).matches()) {
            throw new InvalidInputException("Username non valido: 3-50 caratteri fra lettere, cifre, punto, trattino e underscore.");
        }
        this.username = value;
    }

    public void setPassword(String password) throws InvalidInputException {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new InvalidInputException("La password deve essere di almeno " + MIN_PASSWORD_LENGTH + " caratteri.");
        }
        this.password = password;
    }

    public void setFullName(String fullName) throws InvalidInputException {
        String value = BeanFields.required(fullName, "Il nome completo è obbligatorio (massimo " + MAX_NAME_LENGTH + " caratteri).");
        if (value.length() > MAX_NAME_LENGTH) {
            throw new InvalidInputException("Il nome completo è obbligatorio (massimo " + MAX_NAME_LENGTH + " caratteri).");
        }
        BeanFields.rejectComma(value, "Nome e posizione non possono contenere virgole.");
        this.fullName = value;
    }

    /**
     * @param homeLocation facoltativa: vuota o di soli spazi equivale a "nessuna posizione dichiarata".
     */
    public void setHomeLocation(String homeLocation) throws InvalidInputException {
        String value = BeanFields.optional(homeLocation);
        BeanFields.rejectComma(value, "Nome e posizione non possono contenere virgole.");
        this.homeLocation = value;
    }

    /**
     * @param phoneNumber facoltativo, ma se presente deve avere un formato plausibile, perché è
     * ciò che permette alla controparte di una richiesta di passaggio di contattare lo studente.
     */
    public void setPhoneNumber(String phoneNumber) throws InvalidInputException {
        String value = BeanFields.optional(phoneNumber);
        if (value != null && !PHONE_PATTERN.matcher(value).matches()) {
            throw new InvalidInputException("Numero di telefono non valido: usa solo cifre, spazi ed eventualmente un + iniziale.");
        }
        this.phoneNumber = value;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
    public String getHomeLocation() { return homeLocation; }
    public String getPhoneNumber() { return phoneNumber; }
}
