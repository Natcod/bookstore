package com.example.tobiya_books;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.PropertyName;
import java.util.Date;
import java.util.List;

public class Group {
    private String id;
    @PropertyName("bookClubName")
    private String name;
    private List<String> members;
    private Date creationDate;
    private DocumentReference creator; // Use DocumentReference instead of String

    public Group() {
        // Required empty constructor for Firestore serialization
    }

    public Group(String name, List<String> members) {
        this.name = name;
        this.members = members;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @PropertyName("bookClubName")
    public String getName() {
        return name;
    }

    @PropertyName("bookClubName")
    public void setName(String name) {
        this.name = name;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public DocumentReference getCreator() {
        return creator;
    }

    public void setCreator(DocumentReference creator) {
        this.creator = creator;
    }
}
