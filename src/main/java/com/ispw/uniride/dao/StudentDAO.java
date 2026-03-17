package com.ispw.uniride.dao;

import com.ispw.uniride.model.Student;

public interface StudentDAO {
    void saveStudent(Student student);
    Student getStudentByUsername(String username);
}
