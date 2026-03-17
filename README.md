# UniRide - Piattaforma di Carpooling Universitario

## 1. Descrizione del Progetto

**UniRide** è un'applicazione progettata specificatamente per gli studenti universitari per gestire il carpooling.
L'obiettivo è consentire agli studenti di offrire o cercare passaggi per andare e tornare dal campus, riducendo i costi di viaggio, l'inquinamento e promuovendo la socializzazione.

### Attori Principali:
- **Guidatore (Studente):** Un utente che possiede un'auto e offre passaggi inserendo dettagli come data, orario, posti disponibili, punto di partenza/arrivo.
- **Passeggero (Studente):** Un utente che cerca un passaggio, visualizza i viaggi disponibili, prenota un posto e contatta il guidatore.

### Casi d'Uso Principali:
1.  **Offerta Passaggio:** Inserimento di un nuovo viaggio disponibile.
2.  **Ricerca Passaggio:** Filtraggio delle corse in base a campus, orario e giorno.
3.  **Prenotazione Passaggio:** Interazione per riservare un posto, gestendo le conferme o i rifiuti.

## 2. Architettura

Il progetto segue rigorosamente il pattern architetturale **BCE (Boundary-Control-Entity)** per separare chiaramente le responsabilità:

*   **Boundary (`com.ispw.uniride.boundary`):**
    *   Gestisce esclusivamente l'interazione con l'utente.
    *   Nel caso della GUI (JavaFX), funge da "Controller" per le View (file FXML), passando e ricevendo solo oggetti *Bean*.
    *   Esiste anche una simulazione CLI (`CliMain.java`) per ambienti headless.
*   **Control (`com.ispw.uniride.controller`):**
    *   Incapsula tutta la logica di business e i Casi d'Uso (es. `RideController`, `BookingController`).
    *   Coordina i flussi di dati senza dipendere minimamente dalla UI usata.
*   **Entity (`com.ispw.uniride.model`):**
    *   Rappresenta le classi di dominio (es. `Student`, `Ride`, `Booking`).

### Design Pattern Applicati (GoF):
*   **Abstract Factory / Factory Method:** Implemenato nel `DAOFactory` per disaccoppiare la logica dalla tecnologia di persistenza.
*   **Singleton:** Usato per gestire la configurazione globale, lo stato della Sessione, o gli storage In-Memory.
*   **State Pattern:** Per gestire il ciclo di vita di un viaggio/prenotazione (es. *Richiesto*, *Confermato*, *Completato*).
*   **Strategy Pattern:** Per la suddivisione flessibile dei costi in base alla distanza o ai passeggeri.
*   **Observer Pattern:** Per inviare notifiche (aggiornamenti di viaggio) ai passeggeri in ascolto.

## 3. Persistenza dei Dati

L'applicazione supporta una persistenza **"duale"** che può essere modificata cambiando la costante `PERSISTENCE_TYPE` (in `com.ispw.uniride.config.Config.java`):

1.  **In-Memory Storage (`DBType.MEMORY`):**
    *   Sfrutta strutture dati RAM locali (es. `HashMap` per accessi $O(1)$) ottime per lo sviluppo rapido e dimostrazioni.
2.  **File System Storage (`DBType.FILESYSTEM`):**
    *   Salva i dati su disco all'interno della root del progetto (formato `.csv`).
    *   Utilizza **lock espliciti** per thread-safety in concorrenza e un **sistema di caching in memoria** sincronizzato con i timestamp dei file.

## 4. Installazione e Avvio

### Prerequisiti
*   **Java:** 17 o superiore.
*   **Maven:** Strumento per la gestione delle dipendenze e build.

### Comandi Maven

1.  **Compilazione:**
    ```bash
    mvn clean compile
    ```
2.  **Avvio GUI (JavaFX):**
    Avvia l'applicativo scavalcando limitazioni modulari, partendo dalla classe `Launcher`:
    ```bash
    mvn exec:java
    ```
3.  **Avvio CLI:**
    Per test in ambienti server o terminal-only, occorre costruire il classpath e chiamare `CliMain`:
    ```bash
    mvn dependency:build-classpath
    java -cp target/classes:$(cat classpath.txt) com.ispw.uniride.CliMain
    ```
4.  **Test (JUnit 5):**
    ```bash
    mvn test
    ```

*La reportistica log applicativa avviene esclusivamente tramite `java.util.logging.Logger` centralizzato nella classe `LoggerCustom` anziché stampe su terminale.*