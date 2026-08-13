# UniRide - Piattaforma di Carpooling Universitario

## 1. Descrizione del Progetto

**UniRide** è un'applicazione progettata specificatamente per gli studenti universitari per gestire il carpooling.
L'obiettivo è consentire agli studenti di offrire o cercare passaggi per andare e tornare dal campus, riducendo i costi di viaggio, l'inquinamento e promuovendo la socializzazione.

### Attori Principali
- **Guidatore (Studente):** un utente che possiede un'auto e offre passaggi inserendo dettagli come data, posti disponibili, punto di partenza/arrivo e costo base stimato.
- **Passeggero (Studente):** un utente che cerca un passaggio, visualizza i viaggi disponibili e prenota un posto.

Ogni studente può ricoprire entrambi i ruoli: nulla vieta di offrire una corsa e, in parallelo, cercarne un'altra come passeggero.

### Casi d'Uso Principali
1. **Registrazione / Login:** creazione account con username, password, comune/paese di residenza (facoltativo, usato per i suggerimenti di vicinanza) e numero di telefono (facoltativo, mostrato solo alla controparte di una richiesta di passaggio già esistente, mai in un elenco di ricerca pubblico).
2. **Offerta Passaggio:** inserimento di un nuovo viaggio disponibile (partenza, destinazione, data, orario di partenza HH:mm, posti, costo base).
3. **Ricerca Passaggio:** filtraggio delle corse disponibili in base a partenza e destinazione, con stima del costo per passeggero calcolata dinamicamente.
4. **Prenotazione Passaggio:** uno studente si iscrive a un passaggio offerto da un altro utente (non è consentito prenotare la propria corsa).
5. **Gestione Corse:** visione separata delle corse offerte (come guidatore) e di quelle prenotate (come passeggero), con possibilità di annullare una prenotazione o, se non ci sono ancora passeggeri, annullare direttamente una corsa offerta.

## 2. Architettura

Il progetto segue rigorosamente il pattern architetturale **BCE (Boundary-Control-Entity)** per separare chiaramente le responsabilità:

* **Boundary (`com.ispw.uniride.boundary`):**
    * Gestisce esclusivamente l'interazione con l'utente.
    * Per la GUI (JavaFX) espone le classi controller-FXML (`LoginBoundary`, `RegisterBoundary`, `StudentDashboardBoundary`, `OfferRideBoundary`, `SearchRideBoundary`, `ManageRidesBoundary`), passando e ricevendo solo oggetti *Bean*.
    * Esiste anche una simulazione CLI parallela (`LoginCLI`, `OfferRideCLI`, `SearchRideCLI`, `ManageRidesCLI`, `StudentCLI`), avviata da `CliMain`, per ambienti headless o testing rapido da terminale.
* **Control (`com.ispw.uniride.controller`):**
    * Incapsula tutta la logica di business e i Casi d'Uso (`LoginController`, `OfferRideController`, `SearchRideController`, `ManageRidesController`).
    * Coordina i flussi di dati senza dipendere minimamente dalla UI usata (la stessa logica serve sia GUI che CLI).
    * `Session` mantiene lo stato dell'utente autenticato per la durata dell'applicazione.
* **Entity (`com.ispw.uniride.model`):**
    * Rappresenta le classi di dominio: `Student` (attore) e `Ride` (passaggio offerto), con i sotto-package `model.state`, `model.strategy` e `model.observer` per la logica di dominio più complessa (vedi Design Pattern sotto).

Altri package di supporto:
* **`com.ispw.uniride.bean`:** DTO (`RideBean`, `UserBean`) usati per scambiare dati tra Boundary e Controller senza esporre le Entity.
* **`com.ispw.uniride.dao`:** interfacce e implementazioni per la persistenza (vedi sezione 3).
* **`com.ispw.uniride.exceptions`:** eccezioni applicative dedicate (`RideActionException`, `RideNotFoundException`, `UserNotAuthorizedException`, `UserNotFoundException`).
* **`com.ispw.uniride.config`:** parametri globali (`Config`) e il catalogo delle località selezionabili in UI (`LocationCatalog`, `LocationInfo`).
* **`com.ispw.uniride.utils`:** utility trasversali, come il logger applicativo (`LoggerCustom`).

### Design Pattern Applicati (GoF)
* **Abstract Factory:** `DAOFactory` disaccoppia la logica applicativa dalla tecnologia di persistenza scelta (`MemoryDAOFactory`, `FsDAOFactory`, `SqlDAOFactory`).
* **Singleton:** usato per `Session` (stato dell'utente loggato), `MemoryStorage` (storage in RAM) e la stessa `DAOFactory`.
* **State Pattern:** governa il ciclo di vita di un passaggio (`RideState` → `AvailableState`, `FullState`) senza condizionali sparsi nell'entità `Ride`.
* **Strategy Pattern:** calcola dinamicamente la quota di costo per passeggero (`CostStrategy` → `EqualSplitStrategy`, `DistanceCostStrategy`), selezionabile da `Config.COST_STRATEGY_TYPE`.
* **Observer Pattern:** notifica gli iscritti a un passaggio (`Subject`/`Observer`) quando un nuovo passeggero si unisce o una prenotazione viene annullata.

## 3. Persistenza dei Dati

L'applicazione supporta una persistenza **"tripla"**, selezionabile cambiando la costante `PERSISTENCE_TYPE` in `com.ispw.uniride.config.Config`:

1. **In-Memory Storage (`DBType.MEMORY`, default):**
   * Sfrutta strutture dati RAM locali (`HashMap`) ottime per sviluppo rapido e dimostrazioni.
   * Precarica due utenti demo (`mario.rossi` / `luigi.verdi`, password `password`) con una posizione di esempio già impostata, per mostrare da subito i suggerimenti di vicinanza.
2. **File System Storage (`DBType.FILESYSTEM`):**
   * Salva i dati su disco all'interno della root del progetto (`students.csv`, `rides.csv`, `bookings.csv`).
   * Utilizza **lock espliciti** per thread-safety in concorrenza e un **sistema di caching in memoria** sincronizzato con i timestamp dei file.
3. **Database Relazionale (`DBType.SQL`):**
   * Usa **H2** in modalità embedded (file locale `uniride_db.mv.db`, nessun server esterno da installare/configurare) tramite **JDBC standard**.
   * Schema relazionale reale, incluso una tabella di collegamento `ride_passengers` per la relazione molti-a-molti Ride↔passeggero (a differenza della variante CSV, che appiattisce la lista in colonna).
   * Le query di upsert usano `MERGE INTO ... KEY (...)`, sintassi H2 per l'insert-or-update.

In tutti e tre i casi la stessa interfaccia (`RideDAO`, `StudentDAO`, `BookingDAO`) è implementata in modo intercambiabile: cambiare persistenza dell'intera applicazione richiede di modificare **una sola costante**, senza toccare Controller o Model (Abstract Factory Pattern, vedi sopra).

## 4. Interfaccia Grafica (JavaFX)

* Le viste sono definite in FXML (`src/main/resources/com/ispw/uniride`) con un unico foglio di stile condiviso (`style.css`) che applica un design moderno coerente (palette indaco/violetto, card con ombre, componenti arrotondati) a tutte le schermate: Login, Registrazione, Dashboard, Offri Passaggio, Cerca Passaggio, Gestione Corse.
* Partenza e destinazione si selezionano da **menu a tendina editabili** (`ComboBox`) popolati dal `LocationCatalog`, che include non solo i capoluoghi ma anche i comuni/paesi limitrofi delle principali aree universitarie italiane (Lazio, Lombardia, Campania, Piemonte, Veneto, Emilia-Romagna, Toscana, Sicilia) — restando comunque liberi di digitare qualunque altra località.
* Se lo studente ha dichiarato il proprio comune in fase di registrazione, le località vengono ordinate dalla più vicina (calcolo delle distanze in linea d'aria con la formula di Haversine) e la destinazione predefinita suggerisce l'ateneo più vicino.
* La data del passaggio si seleziona con un `DatePicker` e i posti disponibili con uno `Spinner` numerico, riducendo l'inserimento manuale a favore di controlli guidati.

## 5. Installazione e Avvio

### Prerequisiti
* **Java:** 25 o superiore (vedi `maven.compiler.source`/`target` in `pom.xml`).
* **Maven:** strumento per la gestione delle dipendenze e build.

### Comandi Maven

1. **Compilazione:**
    ```bash
    mvn clean compile
    ```
2. **Avvio GUI (JavaFX)** — metodo consigliato, usa il plugin ufficiale `javafx-maven-plugin`:
    ```bash
    mvn javafx:run
    ```
    In alternativa, per scavalcare le limitazioni del module-path tramite `exec-maven-plugin`:
    ```bash
    mvn exec:java
    ```
3. **Avvio CLI:**
    Per test in ambienti server o terminal-only, occorre costruire il classpath e chiamare `CliMain`:
    ```bash
    mvn dependency:build-classpath -Dmdep.outputFile=classpath.txt
    java -cp target/classes:$(cat classpath.txt) com.ispw.uniride.CliMain
    ```
4. **Test (JUnit 5):**
    ```bash
    mvn test
    ```

*La reportistica log applicativa avviene esclusivamente tramite `java.util.logging.Logger` centralizzato nella classe `LoggerCustom` anziché stampe dirette su terminale.*
