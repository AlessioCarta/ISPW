package com.ispw.uniride.dao.fs;

import com.ispw.uniride.dao.StudentDAO;
import com.ispw.uniride.model.Student;
import com.ispw.uniride.utils.LoggerCustom;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class FsStudentDAO implements StudentDAO {
    private static final String FILE_PATH = "students.csv";
    private static final Object LOCK = new Object();

    // Numero minimo di campi attesi su una riga CSV valida (username, password, fullName);
    // il 4° campo (homeLocation) è facoltativo per retro-compatibilità.
    private static final int MIN_CSV_FIELDS = 3;

    // Static Cache
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
                    LoggerCustom.warning("students.csv risultava assente ma non è stato possibile crearlo (probabile race condition concorrente).");
                    return;
                }
                // Mock hashed initial data
                try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
                    pw.println("mario.rossi,[HASHED]password,Mario Rossi");
                    pw.println("luigi.verdi,[HASHED]password,Luigi Verdi");
                }
            } catch (IOException e) {
                LoggerCustom.error("Failed to create students.csv", e);
            }
        }
    }

    private void refreshCache() {
        File file = new File(FILE_PATH);
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
    private void loadCacheFromFile(File file) {
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
     * Converte una riga CSV (username, password, fullName, [homeLocation]) in uno {@link Student}.
     * @return lo Student ricostruito, oppure {@code null} se la riga è malformata.
     */
    private Student parseStudentFromCsvLine(String line) {
        String[] parts = line.split(",");
        if (parts.length < MIN_CSV_FIELDS) {
            return null;
        }
        // Il 4° campo (posizione dichiarata) è opzionale: righe CSV storiche scritte prima
        // di questa funzionalità restano leggibili senza errori.
        String homeLocation = parts.length >= 4 && !parts[3].trim().isEmpty() ? parts[3] : null;
        return new Student(parts[0], parts[1], parts[2], homeLocation);
    }

    @Override
    public void saveStudent(Student student) {
        synchronized (LOCK) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_PATH, true))) {
                String homeLocation = student.getHomeLocation() != null ? student.getHomeLocation() : "";
                pw.println(student.getUsername() + "," + student.getPassword() + "," + student.getFullName() + "," + homeLocation);
                if (cache != null) {
                    cache.put(student.getUsername(), student);
                }
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
