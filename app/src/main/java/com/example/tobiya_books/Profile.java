package com.example.tobiya_books;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Profile extends Fragment {

    private EditText editTextFirstName, editTextLastName, editTextUsername, editTextEmail;
    private TextView textViewInitial;
    private Button buttonSave;

    private FirebaseFirestore db;

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

        editTextFirstName = view.findViewById(R.id.editTextname);
        editTextLastName = view.findViewById(R.id.editTextLastName);
        editTextUsername = view.findViewById(R.id.editTextUsername);
        editTextEmail = view.findViewById(R.id.editTextEmail);
        textViewInitial = view.findViewById(R.id.textViewInitial);
        buttonSave = view.findViewById(R.id.buttonSave);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String currentUserId = sharedPreferences.getString("UserID", null);
        if (currentUserId != null && !currentUserId.isEmpty()) {
            fetchUserProfile(currentUserId);
        } else {
            Toast.makeText(getActivity(), "User not logged in", Toast.LENGTH_SHORT).show();
        }

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String firstName = editTextFirstName.getText().toString();
                String lastName = editTextLastName.getText().toString();
                String username = editTextUsername.getText().toString();
                String email = editTextEmail.getText().toString();

                if (currentUserId != null) {
                    updateUserProfile(currentUserId, firstName, lastName, username, email);
                }
            }
        });
    }

    private void fetchUserProfile(String userId) {
        db.collection("Reader").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        String firstName = document.getString("firstName");
                        String lastName = document.getString("lastName");
                        String username = document.getString("username");
                        String email = document.getString("email");

                        editTextFirstName.setText(firstName);
                        editTextLastName.setText(lastName);
                        editTextUsername.setText(username);
                        editTextEmail.setText(email);

                        // Set initial of the first name
                        if (firstName != null && !firstName.isEmpty()) {
                            textViewInitial.setText(String.valueOf(firstName.charAt(0)).toUpperCase());
                        }

                        // Store the details in SharedPreferences
                        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("Username", username);
                        editor.putString("FirstName", firstName);
                        editor.putString("LastName", lastName);
                        editor.putString("Email", email);
                        editor.apply();
                    } else {
                        Toast.makeText(getActivity(), "User profile not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Failed to fetch user profile", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateUserProfile(String userId, String firstName, String lastName, String username, String email) {
        Map<String, Object> user = new HashMap<>();
        user.put("firstName", firstName);
        user.put("lastName", lastName);
        user.put("username", username);
        user.put("email", email);

        db.collection("Reader").document(userId).update(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getActivity(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Failed to update profile", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

