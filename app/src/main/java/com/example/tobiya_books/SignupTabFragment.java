package com.example.tobiya_books;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SignupTabFragment extends Fragment {

    // UI Elements
    Button signupButton;
    EditText usernameEditText, emailEditText, passwordEditText, confirmEditText, firstNameEditText, lastNameEditText;

    // Firestore instance
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference readerCollection = db.collection("Reader");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_signuptab, container, false);

        // Initialize UI elements
        usernameEditText = view.findViewById(R.id.signup_username);
        emailEditText = view.findViewById(R.id.Login_email);
        passwordEditText = view.findViewById(R.id.signup_password);
        confirmEditText = view.findViewById(R.id.signup_confirm);
        firstNameEditText = view.findViewById(R.id.signup_firstname);
        lastNameEditText = view.findViewById(R.id.signup_lastname);
        signupButton = view.findViewById(R.id.signup_button);

        // Set onClick listener for the signup button
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkUsernameAndRegister();
            }
        });

        return view;
    }

    // Method to validate input and check username/email availability
    private void checkUsernameAndRegister() {
        String username = usernameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirm = confirmEditText.getText().toString().trim();
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();

        // Perform input validation
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirm)) {
            Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 8) {
            Toast.makeText(getContext(), "Password must be at least 8 characters long", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!email.contains("@")) {
            Toast.makeText(getContext(), "Invalid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        if (containsNumber(firstName) || containsNumber(lastName)) {
            Toast.makeText(getContext(), "First Name and Last Name should not contain numbers", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the username already exists
        readerCollection.whereEqualTo("username", username).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            Toast.makeText(getContext(), "Username is already taken", Toast.LENGTH_SHORT).show();
                        } else {
                            // Check if the email already exists
                            readerCollection.whereEqualTo("email", email).get()
                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            if (!queryDocumentSnapshots.isEmpty()) {
                                                Toast.makeText(getContext(), "Email is already registered", Toast.LENGTH_SHORT).show();
                                            } else {
                                                // Register the user
                                                registerUser(username, email, password, firstName, lastName);
                                            }
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(getContext(), "Error checking email: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Error checking username: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Helper method to check if a string contains a number
    private boolean containsNumber(String str) {
        for (char c : str.toCharArray()) {
            if (Character.isDigit(c)) {
                return true;
            }
        }
        return false;
    }

    // Method to register the user in Firestore
    private void registerUser(String username, String email, String password, String firstName, String lastName) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm:ss a z", Locale.getDefault());
        String registrationDate = sdf.format(new Date());

        Map<String, Object> reader = new HashMap<>();
        reader.put("username", username);
        reader.put("email", email);
        reader.put("password", password);
        reader.put("firstName", firstName);
        reader.put("lastName", lastName);
        reader.put("registrationDate", registrationDate);

        readerCollection.add(reader)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(getContext(), "User registered successfully", Toast.LENGTH_SHORT).show();
                        clearInputFields();
                        redirectToLogin();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Error registering user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Method to clear input fields after successful registration
    private void clearInputFields() {
        usernameEditText.setText("");
        emailEditText.setText("");
        passwordEditText.setText("");
        confirmEditText.setText("");
        firstNameEditText.setText("");
        lastNameEditText.setText("");
    }

    // Method to redirect to the login screen
    private void redirectToLogin() {
        Intent intent = new Intent(getActivity(), MainActivity2.class);
        startActivity(intent);
        getActivity().finish(); // Call finish() if you want to close the current activity
    }
}
