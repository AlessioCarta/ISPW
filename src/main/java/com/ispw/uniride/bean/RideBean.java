package com.ispw.uniride.bean;

import com.ispw.uniride.exceptions.InvalidInputException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.regex.Pattern;

/**
 * Data Transfer Object (DTO) destinato a trasportare logicamente informazioni
 * riguardanti un Passaggio (Ride) tra i confini architetturali (ad es. da Controller Applicativo alla Boundary).
 * Utilizzato per schermare il modulo del Dominio interno dell'Applicativo ed esporre alla UI grafica soltanto
 * i dati sicuri.
 */
public class RideBean {
    private String id;
    private String departure;
    private String destination;
    private String date;
    // Orario di partenza deciso dal guidatore (formato "HH:mm"), non sensibile: visibile a
    // chiunque veda il passaggio (elenco ricerca compreso), a differenza del numero di telefono.
    private String departureTime;
    private int totalSeats;
    private double basePrice;

    // Campi calcolati a runtime e letti esclusivamente in output (visualizzazione dashboard)
    private int availableSeats;
    private String status;
    private String driver;
    private double computedPrice;

    // Stesso formato prodotto dal DatePicker della GUI e richiesto dalla CLI ("gg/mm/aaaa").
    // STRICT (con "uuuu" per l'anno) rifiuta date inesistenti come 31/02 invece di "aggiustarle".
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    // Formato orario a 24 ore: lo zero iniziale sull'ora è facoltativo ("9:30" e "09:30" sono
    // entrambi validi) e il valore viene poi normalizzato a due cifre da setDepartureTime.
    private static final Pattern TIME_PATTERN = Pattern.compile("^([01]?\\d|2[0-3]):[0-5]\\d$");

    /**
     * Costruttore vuoto per il caso d'uso "Offri Passaggio": la Boundary compila il Bean tramite
     * i setter, che controllano la forma di ciascun campo (obbligatorietà, formato, parsabilità)
     * prima che il dato raggiunga il Controller Applicativo, responsabile delle sole regole di business.
     */
    public RideBean() {
        // Nessuna inizializzazione: i campi vengono valorizzati e validati dai setter.
    }

    /**
     * Costruttore DTO completo per il caso d'uso "Ricerca Passaggio" o "Elenco dashboard".
     * In questo caso la logica applicativa ha già interrogato il Database e lo incapsula in
     * un oggetto fittizio contenente tutti gli stati generati (come id o disponibili).
     * @param id identificativo univoco assegnato (es. hash in csv o in RAM) alla Ride.
     * @param driver nome autista assegnato alla sessione corrente della creazione.
     * @param departure l'origine prefissata dal conducente.
     * @param destination meta stabilita del carpooling.
     * @param date la schedulazione testuale o oggettuale indicante il giorno d'avvio.
     * @param departureTime l'orario di partenza deciso dal guidatore (formato "HH:mm"), o {@code null}.
     * @param totalSeats i posti a sedere massimi inseriti alla creazione.
     * @param availableSeats il conteggio dei passeggeri in tempo reale rispetto agli slot vuoti.
     * @param basePrice spesa base totale che sarà successivamente elaborata dallo Strategy pattern.
     * @param status stringa testuale equivalente allo stato della macchina (AVAILABLE o FULL).
     */
    // I 10 parametri sono tutti campi primitivi/String indipendenti di sola visualizzazione (una
    // riga della ListView): introdurre un oggetto Builder o un value-object annidato avrebbe
    // aggiunto complessità senza reali benefici per un semplice DTO come questo.
    @SuppressWarnings("java:S107")
    public RideBean(String id, String driver, String departure, String destination, String date, String departureTime, int totalSeats, int availableSeats, double basePrice, String status) {
        this.id = id;
        this.driver = driver;
        this.departure = departure;
        this.destination = destination;
        this.date = date;
        this.departureTime = departureTime;
        this.totalSeats = totalSeats;
        this.availableSeats = availableSeats;
        this.basePrice = basePrice;
        this.status = status;
    }

    // Boilerplate getter ed event-setter incapsulati per mascherare i campi originali
    public void setDeparture(String departure) throws InvalidInputException {
        String value = BeanFields.required(departure, "Partenza e destinazione sono obbligatorie.");
        BeanFields.rejectComma(value, "Partenza e destinazione non possono contenere virgole.");
        this.departure = value;
    }

    public void setDestination(String destination) throws InvalidInputException {
        String value = BeanFields.required(destination, "Partenza e destinazione sono obbligatorie.");
        BeanFields.rejectComma(value, "Partenza e destinazione non possono contenere virgole.");
        this.destination = value;
    }

    /**
     * @param date data nel formato gg/mm/aaaa; il controllo che non sia già passata è invece una
     * regola di business e spetta al Controller Applicativo.
     */
    public void setDate(String date) throws InvalidInputException {
        String value = BeanFields.required(date, "La data del passaggio è obbligatoria.");
        try {
            this.date = LocalDate.parse(value, DATE_FORMAT).format(DATE_FORMAT);
        } catch (DateTimeParseException _) {
            throw new InvalidInputException("Data non valida: usa il formato gg/mm/aaaa (es. 15/03/2027).");
        }
    }

    public void setDepartureTime(String departureTime) throws InvalidInputException {
        String value = departureTime == null ? "" : departureTime.trim();
        if (!TIME_PATTERN.matcher(value).matches()) {
            throw new InvalidInputException("Inserisci un orario di partenza valido (formato HH:mm, es. 08:30).");
        }
        // Forma canonica "HH:mm", così liste e ricerche mostrano sempre lo stesso formato.
        String[] parts = value.split(":");
        this.departureTime = String.format("%02d:%s", Integer.parseInt(parts[0]), parts[1]);
    }

    public void setTotalSeats(String totalSeats) throws InvalidInputException {
        try {
            this.totalSeats = Integer.parseInt(BeanFields.required(totalSeats, "Indica il numero di posti."));
        } catch (NumberFormatException _) {
            throw new InvalidInputException("Il numero di posti deve essere un numero intero.");
        }
    }

    /**
     * @param basePrice accetta sia il punto sia la virgola come separatore decimale ("12.50" o "12,50").
     */
    public void setBasePrice(String basePrice) throws InvalidInputException {
        try {
            this.basePrice = Double.parseDouble(BeanFields.required(basePrice, "Indica il costo base.").replace(',', '.'));
        } catch (NumberFormatException _) {
            throw new InvalidInputException("Il costo base deve essere un numero (es. 12.50).");
        }
    }

    public String getId() { return id; }
    public String getDeparture() { return departure; }
    public String getDestination() { return destination; }
    public String getDate() { return date; }
    public String getDepartureTime() { return departureTime; }
    public int getTotalSeats() { return totalSeats; }
    public double getBasePrice() { return basePrice; }
    public int getAvailableSeats() { return availableSeats; }
    public String getStatus() { return status; }
    public String getDriver() { return driver; }
    public double getComputedPrice() { return computedPrice; }

    /**
     * @param computedPrice campo non dipendente dal database, viene popolato a runtime in fase
     * di visualizzazione in base al pattern Strategy.
     */
    public void setComputedPrice(double computedPrice) { this.computedPrice = computedPrice; }
}
