package com.example.tobiya_books;

public class Book {
    private String title;
    private String author;
    private String description;
    private String publicationDate;
    private String coverImageName;
    private String language;
    private String price;
    private String accessType;

    public Book(String title, String author, String description, String publicationDate, String coverImageName, String language, String price, String accessType) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.publicationDate = publicationDate;
        this.coverImageName = coverImageName;
        this.language = language;
        this.price = price;
        this.accessType = accessType;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getDescription() {
        return description;
    }

    public String getPublicationDate() {
        return publicationDate;
    }

    public String getCoverImageName() {
        return coverImageName;
    }

    public String getLanguage() {
        return language;
    }

    public String getPrice() {
        return price;
    }

    public String getAccessType() {
        return accessType;
    }
}
