package com.example.tobiya_books;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Message {
    private String message;
    private DocumentReference sender;
    private String senderUsername; // Add a field to store the sender's username
    @ServerTimestamp
    private Date sentDateTime;

    // Constructors, getters, and setters
    // Default constructor required for Firestore
    public Message() {}

    public Message(String message, DocumentReference sender, String senderUsername, Date sentDateTime) {
        this.message = message;
        this.sender = sender;
        this.senderUsername = senderUsername; // Set the sender's username
        this.sentDateTime = sentDateTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public DocumentReference getSender() {
        return sender;
    }

    public void setSender(DocumentReference sender) {
        this.sender = sender;
    }

    public String getSenderUsername() {
        return senderUsername; // Retrieve the sender's username
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public Date getSentDateTime() {
        return sentDateTime;
    }

    public void setSentDateTime(Date sentDateTime) {
        this.sentDateTime = sentDateTime;
    }
}
