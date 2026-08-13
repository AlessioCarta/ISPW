package com.ispw.uniride.dao.fs;

import com.ispw.uniride.dao.StudentDAO;
import com.ispw.uniride.model.Student;
import com.ispw.uniride.utils.LoggerCustom;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementazione concreta di {@link StudentDAO} per la famiglia File System.
 * Persiste su {@code students.csv}, con la stessa strategia di cache-invalidation-su-timestamp
 * usata da {@link FsRideDAO}. A differenza di quest'ultima, qui non esiste un percorso di
 * "update" (uno Student, una volta registrato, non cambia più via DAO): le scritture sono
 * quindi semplici append in coda al file, mai riscritture complete.
 */
public class FsStudentDAO implements StudentDAO {
    private static final String FILE_PATH = "students.csv";
    private static final Object LOCK = new Object();

    // Numero minimo di campi attesi su una riga CSV valida: username - password - fullName.
    // Il quarto campo (homeLocation) è facoltativo, per retro-compatibilità.
    private static final int MIN_CSV_FIELDS = 3;

    // Static Cache: condivisa fra tutte le istanze di FsStudentDAO.
    private static Map<String, Student> cache = null;
    private static long lastModified = 0;

    public FsStudentDAO() {
        ensureFileExists();
    }

    private void ensureFileExists() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            try {
                boolean created = file.createNewFile();
                if (!created) {
                    // Race condition innocua (creato nel frattempo da un altro processo/thread):
                    // usciamo senza popolare i dati mock, che in tal caso esistono già.
                    LoggerCustom.warning("students.csv risultava assente ma non è stato possibile crearlo (probabile race condition concorrente).");
                    return;
                }
                // Mock hashed initial data: stesso seed di MemoryStorage, così la demo si
                // comporta allo stesso modo indipendentemente dalla modalità di persistenza scelta.
                try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
                    pw.println("mario.rossi,[HASHED]password,Mario Rossi,Palestrina,+39 333 1234567");
                    pw.println("luigi.verdi,[HASHED]password,Luigi Verdi,Frascati,+39 347 7654321");
                }
            } catch (IOException e) {
                LoggerCustom.error("Failed to create students.csv", e);
            }
        }
    }

    private void refreshCache() {
        File file = new File(FILE_PATH);
        // Pattern check-lock-check, identico a FsRideDAO: evita di acquisire il lock quando
        // la cache è già aggiornata, e previene ricariche duplicate sotto concorrenza.
        if (cache == null || file.lastModified() > lastModified) {
            synchronized (LOCK) {
                if (cache == null || file.lastModified() > lastModified) {
                    loadCacheFromFile(file);
                }
            }
        }
    }

    /**
     * Legge integralmente students.csv e ricostruisce la cache in memoria, riga per riga.
     * Estratta da {@code refreshCache} per mantenere la complessità cognitiva del metodo
     * chiamante entro la soglia consentita.
     */
    private static void loadCacheFromFile(File file) {
        Map<String, Student> loaded = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                Student student = parseStudentFromCsvLine(line);
                if (student != null) {
                    loaded.put(student.getUsername(), student);
                }
            }
            cache = loaded;
            lastModified = file.lastModified();
        } catch (IOException e) {
            LoggerCustom.error("Error reading students.csv", e);
            cache = loaded;
        }
    }

    /**
     * Converte una riga CSV (username, password, fullName, [homeLocation], [phoneNumber]) in
     * uno {@link Student}.
     * @return lo Student ricostruito, oppure {@code null} se la riga è malformata.
     */
    private static Student parseStudentFromCsvLine(String line) {
        String[] parts = line.split(",");
        if (parts.length < MIN_CSV_FIELDS) {
            return null;
        }
        // Il 4° campo (posizione dichiarata) e il 5° (telefono) sono entrambi opzionali: righe
        // CSV storiche scritte prima dell'introduzione di questi campi restano leggibili senza errori.
        String homeLocation = parts.length >= 4 && !parts[3].trim().isEmpty() ? parts[3] : null;
        String phoneNumber = parts.length >= 5 && !parts[4].trim().isEmpty() ? parts[4] : null;
        return new Student(parts[0], parts[1], parts[2], homeLocation, phoneNumber);
    }

    @Override
    public void saveStudent(Student student) {
        synchronized (LOCK) {
            // true = modalità append: a differenza di FsRideDAO (che riscrive sempre l'intero
            // file), qui è sufficiente accodare la nuova riga, perché non esiste un caso d'uso
            // che modifichi uno Student già registrato — solo creazioni nuove in fase di registrazione.
            try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_PATH, true))) {
                String homeLocation = student.getHomeLocation() != null ? student.getHomeLocation() : "";
                String phoneNumber = student.getPhoneNumber() != null ? student.getPhoneNumber() : "";
                pw.println(student.getUsername() + "," + student.getPassword() + "," + student.getFullName() + "," + homeLocation + "," + phoneNumber);
                // Aggiorniamo la cache in RAM solo se già popolata: se nessuno l'ha ancora letta
                // (cache == null), la prossima refreshCache() la costruirà da zero includendo
                // anche questa nuova riga appena scritta.
                if (cache != null) {
                    cache.put(student.getUsername(), student);
                }
                // Come in FsRideDAO: allinea lastModified al valore post-scrittura, per evitare
                // che la nostra stessa scrittura venga scambiata per una modifica esterna.
                lastModified = new File(FILE_PATH).lastModified();
            } catch (IOException e) {
                LoggerCustom.error("Error saving student to CSV", e);
            }
        }
    }

    @Override
    public Student getStudentByUsername(String username) {
        refreshCache();
        return cache.get(username);
    }
}
