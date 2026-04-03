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
                file.createNewFile();
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
                    cache = new HashMap<>();
                    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            String[] parts = line.split(",");
                            if (parts.length >= 3) {
                                cache.put(parts[0], new Student(parts[0], parts[1], parts[2]));
                            }
                        }
                        lastModified = file.lastModified();
                    } catch (IOException e) {
                        LoggerCustom.error("Error reading students.csv", e);
                    }
                }
            }
        }
    }

    @Override
    public void saveStudent(Student student) {
        synchronized (LOCK) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_PATH, true))) {
                pw.println(student.getUsername() + "," + student.getPassword() + "," + student.getFullName());
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
