package com.example.tobiya_books;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.bottomappbar.BottomAppBar;

import java.util.HashMap;
import java.util.Map;

public class Profile extends Fragment {

    private EditText editTextFirstName, editTextLastName, editTextUsername, editTextEmail;
    private ImageView imageViewProfilePhoto;
    private TextView textViewInitial;
    private Button buttonSave, buttonSetProfilePhoto;

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton floatingActionButton;
    private BottomAppBar bottomAppBar;
    private Toolbar toolbar;

    private ProgressDialog progressDialog;

    private static final String USER_COLLECTION = "Reader";

    private Uri profileImageUri = null;

    public Profile() {
        // Required empty public constructor
    }
    public interface OnUserProfileUpdatedListener {
        void onUserProfileUpdated(String userId);
    }
    private OnUserProfileUpdatedListener callback;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnUserProfileUpdatedListener) {
            callback = (OnUserProfileUpdatedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnUserProfileUpdatedListener");
        }
    }
    public static Profile newInstance() {
        return new Profile();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference().child("profilePicture");
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // Hide the search menu item
        MenuItem searchItem = menu.findItem(R.id.action_search);
        if (searchItem != null) {
            searchItem.setVisible(false);
        }
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

        editTextFirstName = view.findViewById(R.id.editTextFirstName);
        editTextLastName = view.findViewById(R.id.editTextLastName);
        editTextUsername = view.findViewById(R.id.editTextUsername);
        editTextEmail = view.findViewById(R.id.editTextEmail);
        imageViewProfilePhoto = view.findViewById(R.id.imageViewProfilePhoto);
        textViewInitial = view.findViewById(R.id.textViewInitial);
        buttonSave = view.findViewById(R.id.buttonSave);
        buttonSetProfilePhoto = view.findViewById(R.id.buttonSetProfilePhoto);

        bottomNavigationView = getActivity().findViewById(R.id.bottom_navigation);
        floatingActionButton = getActivity().findViewById(R.id.fab);
        bottomAppBar = getActivity().findViewById(R.id.bottomAppBar);
        toolbar = getActivity().findViewById(R.id.toolbar);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String currentUserId = sharedPreferences.getString("UserID", null);
        if (currentUserId != null && !currentUserId.isEmpty()) {
            fetchUserProfile(currentUserId);
        } else {
            Toast.makeText(getActivity(), "User not logged in", Toast.LENGTH_SHORT).show();
        }

        buttonSetProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String firstName = editTextFirstName.getText().toString().trim();
                String lastName = editTextLastName.getText().toString().trim();
                String username = editTextUsername.getText().toString().trim();
                String email = editTextEmail.getText().toString().trim();

                if (currentUserId != null) {
                    if (profileImageUri != null) {
                        uploadImage(currentUserId, firstName, lastName, username, email);
                    } else {
                        updateUserProfile(currentUserId, firstName, lastName, username, email, null);
                    }
                }
            }
        });

        View.OnFocusChangeListener focusChangeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    hideNavigationViews();
                } else {
                    if (!editTextFirstName.hasFocus() && !editTextLastName.hasFocus() &&
                            !editTextUsername.hasFocus() && !editTextEmail.hasFocus()) {
                        showNavigationViews();
                    }
                }
            }
        };

        editTextFirstName.setOnFocusChangeListener(focusChangeListener);
        editTextLastName.setOnFocusChangeListener(focusChangeListener);
        editTextUsername.setOnFocusChangeListener(focusChangeListener);
        editTextEmail.setOnFocusChangeListener(focusChangeListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        checkAndSetNavigationVisibility();
    }

    private void checkAndSetNavigationVisibility() {
        bottomNavigationView = getActivity().findViewById(R.id.bottom_navigation);
        floatingActionButton = getActivity().findViewById(R.id.fab);
        bottomAppBar = getActivity().findViewById(R.id.bottomAppBar);
        toolbar = getActivity().findViewById(R.id.toolbar);

        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.VISIBLE);
        }

        if (floatingActionButton != null) {
            floatingActionButton.setVisibility(View.VISIBLE);
        }

        if (bottomAppBar != null) {
            bottomAppBar.setVisibility(View.VISIBLE);
        }

        if (toolbar != null) {
            toolbar.setVisibility(View.VISIBLE);
        }

        // Reset focus if no EditText has focus
        if (!editTextFirstName.hasFocus() && !editTextLastName.hasFocus() &&
                !editTextUsername.hasFocus() && !editTextEmail.hasFocus()) {
            clearEditTextFocus();
        }
    }

    private void clearEditTextFocus() {
        editTextFirstName.clearFocus();
        editTextLastName.clearFocus();
        editTextUsername.clearFocus();
        editTextEmail.clearFocus();
        hideKeyboard();
    }

    private void hideNavigationViews() {
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.GONE);
        }
        if (floatingActionButton != null) {
            floatingActionButton.setVisibility(View.GONE);
        }
        if (bottomAppBar != null) {
            bottomAppBar.setVisibility(View.GONE);
        }
    }

    private void showNavigationViews() {
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.VISIBLE);
        }
        if (floatingActionButton != null) {
            floatingActionButton.setVisibility(View.VISIBLE);
        }
        if (bottomAppBar != null) {
            bottomAppBar.setVisibility(View.VISIBLE);
        }
        if (toolbar != null) {
            toolbar.setVisibility(View.VISIBLE);
        }
    }

    private void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void fetchUserProfile(String userId) {
        progressDialog.show();
        db.collection(USER_COLLECTION).document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        String firstName = document.getString("firstName");
                        String lastName = document.getString("lastName");
                        String username = document.getString("username");
                        String email = document.getString("email");
                        String profilePhotoUrl = document.getString("profilePhotoUrl");

                        editTextFirstName.setText(firstName);
                        editTextLastName.setText(lastName);
                        editTextUsername.setText(username);
                        editTextEmail.setText(email);

                        if (profilePhotoUrl != null && !profilePhotoUrl.isEmpty()) {
                            Glide.with(Profile.this)
                                    .load(profilePhotoUrl)
                                    .placeholder(R.drawable.baseline_person_24)
                                    .error(R.drawable.baseline_person_24)
                                    .circleCrop()
                                    .into(imageViewProfilePhoto);
                            textViewInitial.setVisibility(View.GONE);
                        } else {
                            showInitials(firstName, lastName);
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

    private void showInitials(String firstName, String lastName) {
        if (firstName != null && !firstName.isEmpty() && lastName != null && !lastName.isEmpty()) {
            String initials = String.valueOf(firstName.charAt(0)) + String.valueOf(lastName.charAt(0));
            textViewInitial.setText(initials);
            textViewInitial.setVisibility(View.VISIBLE);
            imageViewProfilePhoto.setImageResource(R.drawable.circular_background);
        } else {
            textViewInitial.setVisibility(View.GONE);
        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Photo"), 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            profileImageUri = data.getData();
            try {
                Glide.with(requireActivity())
                        .load(profileImageUri)
                        .placeholder(R.drawable.baseline_person_24)
                        .error(R.drawable.baseline_person_24)
                        .circleCrop()
                        .into(imageViewProfilePhoto);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImage(final String userId, final String firstName, final String lastName, final String username, final String email) {
        progressDialog.show();
        buttonSave.setEnabled(false);
        final StorageReference profileImageRef = storageRef.child(userId + ".jpg");
        UploadTask uploadTask = profileImageRef.putFile(profileImageUri);

        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    profileImageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                Uri downloadUri = task.getResult();
                                saveProfilePhotoUrlToPreferences(downloadUri.toString());  // Save URL to SharedPreferences
                                updateUserProfile(userId, firstName, lastName, username, email, downloadUri.toString());
                            } else {
                                progressDialog.dismiss();
                                buttonSave.setEnabled(true);
                                Toast.makeText(getActivity(), "Failed to get download URL", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    progressDialog.dismiss();
                    buttonSave.setEnabled(true);
                    Toast.makeText(getActivity(), "Failed to upload profile photo", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateUserProfile(String userId, String firstName, String lastName, String username, String email, String profilePhotoUrl) {
        progressDialog.show();
        buttonSave.setEnabled(false);
        Map<String, Object> user = new HashMap<>();
        user.put("firstName", firstName);
        user.put("lastName", lastName);
        user.put("username", username);
        user.put("email", email);
        if (profilePhotoUrl != null) {
            user.put("profilePhotoUrl", profilePhotoUrl);
        }

        db.collection(USER_COLLECTION).document(userId).set(user, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.dismiss();
                buttonSave.setEnabled(true);
                if (task.isSuccessful()) {
                    Toast.makeText(getActivity(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Failed to update profile", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveProfilePhotoUrlToPreferences(String profilePhotoUrl) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("ProfilePhotoUrl", profilePhotoUrl);
        editor.apply();
    }
}
