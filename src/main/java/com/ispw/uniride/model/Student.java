package com.ispw.uniride.model;

public class Student {
    private String username;
    private String password; // Simple cleartext for simulation purposes
    private String fullName;

    public Student(String username, String password, String fullName) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
}
