package com.example.tobiya_books;

public class Book {
    private String title;
    private String author;
    private String description;
    private String publicationDate;
    private String coverImageName;

    public Book(String title, String author, String description, String publicationDate, String coverImageName) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.publicationDate = publicationDate;
        this.coverImageName = coverImageName;
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
}
