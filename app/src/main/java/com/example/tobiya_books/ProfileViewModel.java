package com.example.tobiya_books;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class ProfileViewModel extends ViewModel {
    private MutableLiveData<DocumentSnapshot> profileData = new MutableLiveData<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration listenerRegistration;

    public LiveData<DocumentSnapshot> getProfileData() {
        return profileData;
    }

    public void fetchUserProfile(String userId) {
        if (db == null) {
            return;
        }

        DocumentReference docRef = db.collection("Reader").document(userId);
        listenerRegistration = docRef.addSnapshotListener((document, e) -> {
            if (e != null) {
                Log.e("ProfileViewModel", "Firestore error: ", e);
                return;
            }

            if (document != null && document.exists()) {
                Log.d("ProfileViewModel", "Document snapshot received: " + document.getData());
                profileData.setValue(document);
            } else {
                Log.d("ProfileViewModel", "Document does not exist");
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}
