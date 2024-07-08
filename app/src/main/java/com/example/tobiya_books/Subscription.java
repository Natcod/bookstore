package com.example.tobiya_books;

import com.google.firebase.Timestamp;

public class Subscription {
    public enum SubscriptionType {
        DAILY,
        WEEKLY,
        MONTHLY,
        YEARLY
    }

    private SubscriptionType subscriptionType;
    private Timestamp startDate;
    private Timestamp endDate;
    private String reader;
    private String bookReference;

    // Constructors
    public Subscription() {}

    public Subscription(SubscriptionType subscriptionType, Timestamp startDate, Timestamp endDate, String reader, String bookReference) {
        this.subscriptionType = subscriptionType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reader = reader;
        this.bookReference = bookReference;
    }

    // Getters and Setters
    public SubscriptionType getSubscriptionType() {
        return subscriptionType;
    }

    public void setSubscriptionType(SubscriptionType subscriptionType) {
        this.subscriptionType = subscriptionType;
    }

    public Timestamp getStartDate() {
        return startDate;
    }

    public void setStartDate(Timestamp startDate) {
        this.startDate = startDate;
    }

    public Timestamp getEndDate() {
        return endDate;
    }

    public void setEndDate(Timestamp endDate) {
        this.endDate = endDate;
    }

    public String getReader() {
        return reader;
    }

    public void setReader(String reader) {
        this.reader = reader;
    }

    public String getBookReference() {
        return bookReference;
    }

    public void setBookReference(String bookReference) {
        this.bookReference = bookReference;
    }
}
