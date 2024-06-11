package com.example.tobiya_books;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Profile extends Fragment {

    private EditText editTextName, editTextUsername, editTextEmail, editTextBio;
    private ImageView imageViewProfilePhoto;

    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;

    private static final String SHARED_PREFS_NAME = "user_prefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_LOGGED_IN = "LoggedIn";

    public Profile() {
        // Required empty public constructor
    }

    public static Profile newInstance() {
        return new Profile();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        sharedPreferences = getActivity().getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editTextName = view.findViewById(R.id.editTextname);
        editTextUsername = view.findViewById(R.id.editTextUsername);
        editTextEmail = view.findViewById(R.id.editTextEmail);
        editTextBio = view.findViewById(R.id.editTextemail);
        imageViewProfilePhoto = view.findViewById(R.id.imageViewProfilePhoto);

        String userId = sharedPreferences.getString(KEY_USER_ID, null);
        if (userId != null && !userId.isEmpty()) {
            fetchUserProfile(userId);
        } else {
            Toast.makeText(getActivity(), "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchUserProfile(String userId) {
        db.collection("Reader").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        String name = document.getString("name");
                        String username = document.getString("username");
                        String email = document.getString("email");
                        String bio = document.getString("bio");
                        String profilePhotoUrl = document.getString("profilePhotoUrl");

                        editTextName.setText(name);
                        editTextUsername.setText(username);
                        editTextEmail.setText(email);
                        editTextBio.setText(bio);

                        if (profilePhotoUrl != null && !profilePhotoUrl.isEmpty()) {
                            Glide.with(Profile.this)
                                    .load(profilePhotoUrl)
                                    .placeholder(R.drawable.baseline_account_circle_24)
                                    .error(R.drawable.baseline_account_circle_24)
                                    .into(imageViewProfilePhoto);
                        }
                    } else {
                        Toast.makeText(getActivity(), "User profile not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Failed to fetch user profile", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
