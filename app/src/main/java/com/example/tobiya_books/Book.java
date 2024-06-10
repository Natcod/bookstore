package com.example.tobiya_books;

import android.os.Parcel;
import android.os.Parcelable;
import java.io.Serializable;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class Book implements Parcelable,Serializable {
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
    private String publisherPath;  // Store DocumentReference as path
    private String documentReferencePath;  // Store DocumentReference as path
    private String fileURL;

    // Default constructor required for Firebase
    public Book() {}

    // Full constructor for all fields
    public Book(String title, String author, String description, Timestamp publicationDate,
                String coverImage, String language, double price, String accessType,
                Timestamp uploadDate, String genre, String approvalStatus, String publisherPath,
                String documentReferencePath, String fileURL) {
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
        this.publisherPath = publisherPath;
        this.documentReferencePath = documentReferencePath;
        this.fileURL = fileURL;
    }

    // Parcelable implementation
    protected Book(Parcel in) {
        title = in.readString();
        author = in.readString();
        description = in.readString();
        publicationDate = in.readParcelable(Timestamp.class.getClassLoader());
        coverImage = in.readString();
        language = in.readString();
        price = in.readDouble();
        accessType = in.readString();
        uploadDate = in.readParcelable(Timestamp.class.getClassLoader());
        genre = in.readString();
        approvalStatus = in.readString();
        publisherPath = in.readString();
        documentReferencePath = in.readString();
        fileURL = in.readString();
    }

    public static final Creator<Book> CREATOR = new Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(author);
        parcel.writeString(description);
        parcel.writeParcelable(publicationDate, i);
        parcel.writeString(coverImage);
        parcel.writeString(language);
        parcel.writeDouble(price);
        parcel.writeString(accessType);
        parcel.writeParcelable(uploadDate, i);
        parcel.writeString(genre);
        parcel.writeString(approvalStatus);
        parcel.writeString(publisherPath);
        parcel.writeString(documentReferencePath);
        parcel.writeString(fileURL);
    }

    // Convert paths to DocumentReference objects
    public DocumentReference getPublisher() {
        if (publisherPath != null) {
            return FirebaseFirestore.getInstance().document(publisherPath);
        }
        return null;
    }

    public void setPublisher(DocumentReference publisher) {
        if (publisher != null) {
            this.publisherPath = publisher.getPath();
        }
    }

    public DocumentReference getDocumentReference() {
        if (documentReferencePath != null) {
            return FirebaseFirestore.getInstance().document(documentReferencePath);
        }
        return null;
    }

    public void setDocumentReference(DocumentReference documentReference) {
        if (documentReference != null) {
            this.documentReferencePath = documentReference.getPath();
        }
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
    public String getPublisherPath() { return publisherPath; }
    public void setPublisherPath(String publisherPath) { this.publisherPath = publisherPath; }
    public String getDocumentReferencePath() { return documentReferencePath; }
    public void setDocumentReferencePath(String documentReferencePath) { this.documentReferencePath = documentReferencePath; }
    public String getFileURL() { return fileURL; }
    public void setFileURL(String fileURL) { this.fileURL = fileURL; }


}
