package com.example.tobiya_books;

public class Reader {
    private String username;
    private String password;

    public Reader() {
        // Default constructor required for Firebase
    }

    public Reader(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}

