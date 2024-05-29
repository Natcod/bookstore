package com.example.tobiya_books;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
public class Book {
    private String title;
    private String author;
    private String description;
    private Timestamp publicationDate;
    private String coverImage;
    private String language;
    private double price;
    private String accessType;
    private Timestamp uploadDate;
    private String genre;
    private String approvalStatus;
    private DocumentReference publisher;

    // Default constructor required for Firebase
    public Book() {}

    // Full constructor for all fields
    public Book(String title, String author, String description, Timestamp publicationDate,
                String coverImage, String language, double price, String accessType,
                Timestamp uploadDate, String genre, String approvalStatus, DocumentReference publisher) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.publicationDate = publicationDate;
        this.coverImage = coverImage;
        this.language = language;
        this.price = price;
        this.accessType = accessType;
        this.uploadDate = uploadDate;
        this.genre = genre;
        this.approvalStatus = approvalStatus;
        this.publisher = publisher;
    }

    // Getters and setters for all fields
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Timestamp getPublicationDate() { return publicationDate; }
    public void setPublicationDate(Timestamp publicationDate) { this.publicationDate = publicationDate; }
    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getAccessType() { return accessType; }
    public void setAccessType(String accessType) { this.accessType = accessType; }
    public Timestamp getUploadDate() { return uploadDate; }
    public void setUploadDate(Timestamp uploadDate) { this.uploadDate = uploadDate; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }
    public DocumentReference getPublisher() { return publisher; }
    public void setPublisher(DocumentReference publisher) { this.publisher = publisher; }
}
