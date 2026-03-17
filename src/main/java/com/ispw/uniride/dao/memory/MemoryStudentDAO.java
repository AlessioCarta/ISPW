package com.ispw.uniride.dao.memory;

import com.ispw.uniride.dao.StudentDAO;
import com.ispw.uniride.model.Student;

public class MemoryStudentDAO implements StudentDAO {
    private MemoryStorage storage;

    public MemoryStudentDAO() {
        this.storage = MemoryStorage.getInstance();
    }

    @Override
    public void saveStudent(Student student) {
        storage.getStudentsMap().put(student.getUsername(), student);
    }

    @Override
    public Student getStudentByUsername(String username) {
        return storage.getStudentsMap().get(username);
    }
}
