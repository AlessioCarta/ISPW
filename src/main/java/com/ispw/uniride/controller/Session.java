package com.ispw.uniride.controller;

import com.ispw.uniride.model.Student;

public class Session {
    private static Session instance = null;
    private Student loggedUser;

    private Session() {}

    public static synchronized Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    public void setLoggedUser(Student user) {
        this.loggedUser = user;
    }

    public Student getLoggedUser() {
        return loggedUser;
    }

    public void logout() {
        this.loggedUser = null;
    }
}
