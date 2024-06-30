package com.example.tobiya_books;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class LoginTabFragment extends Fragment {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;

    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login_tab, container, false);

        db = FirebaseFirestore.getInstance();
        sharedPreferences = getActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);

        if (isLoggedIn()) {
            // If user is already logged in, open MainActivity directly
            openMainActivity(sharedPreferences.getString("UserID", ""));
        }

        usernameEditText = view.findViewById(R.id.username_edit_text);
        passwordEditText = view.findViewById(R.id.password_edit_text);
        loginButton = view.findViewById(R.id.login_button);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = usernameEditText.getText().toString().trim();
                final String password = passwordEditText.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(getContext(), "Please enter both username and password", Toast.LENGTH_SHORT).show();
                    return;
                }

                db.collection("Reader").whereEqualTo("username", username).get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    QuerySnapshot querySnapshot = task.getResult();
                                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                        DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                                        Boolean banned = document.getBoolean("banned");
                                        if (banned != null && banned) {
                                            Toast.makeText(getContext(), "Your account is not approved. Please wait until it is approved.", Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        String actualPassword = document.getString("password");
                                        if (password.equals(actualPassword)) {
                                            String userId = document.getId(); // Get the document ID as user ID
                                            saveLoginStatus(userId); // Save login status
                                            openMainActivity(userId);
                                        } else {
                                            Toast.makeText(getContext(), "Incorrect password", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(getContext(), "No user found with the provided username", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        return view;
    }

    private void openMainActivity(String userId) {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    private void saveLoginStatus(String userId) {
        // Save login status
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("UserID", userId);
        editor.putBoolean("LoggedIn", true);
        editor.apply();
    }

    private boolean isLoggedIn() {
        // Check if user is already logged in
        return sharedPreferences.getBoolean("LoggedIn", false);
    }
}
