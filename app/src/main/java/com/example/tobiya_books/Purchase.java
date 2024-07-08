package com.example.tobiya_books;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

public class Purchase {
    private DocumentReference ebook;
    private int price;
    private Timestamp purchaseDate;
    private DocumentReference reader;
    private String transactionId;
    private boolean approvalStatus;

    public Purchase() {
        // Default constructor required for Firestore
    }

    public DocumentReference getEbook() {
        return ebook;
    }

    public void setEbook(DocumentReference ebook) {
        this.ebook = ebook;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public Timestamp getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(Timestamp purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public DocumentReference getReader() {
        return reader;
    }

    public void setReader(DocumentReference reader) {
        this.reader = reader;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public boolean isApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(boolean approvalStatus) {
        this.approvalStatus = approvalStatus;
    }
}
