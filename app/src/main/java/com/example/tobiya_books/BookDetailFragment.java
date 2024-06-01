package com.example.tobiya_books;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.Timestamp;
import java.util.Date;

import timber.log.Timber;

public class BookDetailFragment extends Fragment {

    private static final String TAG = "BookDetailFragment";
    private ImageView coverImageView;
    private TextView titleTextView;
    private TextView authorTextView;
    private TextView descriptionTextView;
    private TextView publicationDateTextView;
    private Button buyNowButton;
    private TextView languageTextView;
    private TextView priceTextView;
    private TextView accessTypeTextView;
    private Button backButton;
    private String documentId;

    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_book_detail, container, false);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize views
        coverImageView = view.findViewById(R.id.detail_cover_image);
        titleTextView = view.findViewById(R.id.detail_title);
        authorTextView = view.findViewById(R.id.detail_author);
        descriptionTextView = view.findViewById(R.id.detail_description);
        publicationDateTextView = view.findViewById(R.id.detail_publication_date);
        languageTextView = view.findViewById(R.id.detail_language);
        priceTextView = view.findViewById(R.id.detail_price);
        accessTypeTextView = view.findViewById(R.id.detail_access_type);
        buyNowButton = view.findViewById(R.id.button_buy_now);
        backButton = view.findViewById(R.id.button_back);

        // Get data from arguments
        Bundle bundle = getArguments();
        if (bundle != null) {
            String title = bundle.getString("title");
            String author = bundle.getString("author");
            String description = bundle.getString("description");
            String publicationDate = bundle.getString("publicationDate");
            String coverImageUrl = bundle.getString("coverImageUrl");
            String language = bundle.getString("language");
            double price = bundle.getDouble("price");
            String accessType = bundle.getString("accessType");
            String fileURL = bundle.getString("fileURL");
            String uploadDate = bundle.getString("uploadDate");


            // Set data to views
            titleTextView.setText("Title : " + title);
            authorTextView.setText("Author : " + author);
            descriptionTextView.setText("Description : " + description);
            publicationDateTextView.setText("upload Date : " + uploadDate);
            languageTextView.setText("url : " + fileURL);
            priceTextView.setText("Price : " + price);
            accessTypeTextView.setText("documentID " + documentId);

            Glide.with(requireContext())
                    .load(coverImageUrl)
                    .into(coverImageView);

            // Set button label based on access type
            if (accessType != null) {
                switch (accessType) {
                    case "Free":
                        buyNowButton.setText("Add to Library");
                        break;
                    case "Paid":
                        buyNowButton.setText("Buy Now");
                        break;
                    case "Subscription":
                        buyNowButton.setText("Subscribe");
                        break;
                    case "All":
                        // Do something for All access type
                        break;
                }
            }

            // Set button click listener
            // Set button click listener
            buyNowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Add purchase to the Purchase table
                    switch (accessType) {
                        case "Free":
                            // Do something for Free access type
                            addPurchaseToDatabase();
                            break;
                        case "Paid":
                            // Show payment options dialog
                            openPaymentOptionDialog();
                            break;
                        case "Subscription":
                            // Open subscription dialog
                            ((MainActivity) requireActivity()).showBottomDialog();
                            break;
                        case "All":
                            // Show all books in the table
                            // Implement logic to show all books
                            showAllBooks();
                            break;
                    }
                }
            });



            // Set back button click listener
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Navigate back to HomeFragment
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            });

            // Retrieve document ID based on criteria
            retrieveDocumentId(fileURL);
        }

        return view;
    }

    private void retrieveDocumentId(String fileURL) {
        // Reference to your "Ebook" collection
        CollectionReference ebookCollection = db.collection("Ebook");

        // Perform a query to get the document
        ebookCollection
                .whereEqualTo("fileURL", fileURL)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {

                            String accessType = document.getString("accessType");


                            if (accessType != null) {
                                switch (accessType) {
                                    case "Free":
                                        break;
                                    case "Paid":

                                        break;
                                    case "Subscription":
                                        ((MainActivity) requireActivity()).showBottomDialog();
                                        break;
                                }
                            }

                            // Get the document ID
                            documentId = document.getId();
                            // Log the document ID
                            Timber.tag(TAG).d("Document ID: %s", documentId);
                            // Update UI or perform other actions with the document ID
                            // For example, you can set it to a TextView
                            accessTypeTextView.setText("Document ID: " + documentId);
                        }
                    } else {
                        // Handle errors
                        String errorMessage = "Error getting documents: ";
                        if (task.getException() != null) {
                            errorMessage += task.getException().getMessage();
                            // Log stack trace for debugging
                            task.getException().printStackTrace();
                        }
                        Timber.tag(TAG).e(task.getException(), errorMessage);
                    }
                });
    }


    public void addPurchaseToDatabase() {
        // Retrieve the user ID from shared preferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("UserID", null);

        if (userId != null && documentId != null) {
            // Create a new Purchase object
            Purchase purchase = new Purchase();
            purchase.setEbook(db.collection("Ebook").document(documentId)); // Use setEbook() instead of setEbookRef()
            purchase.setPrice(200); // Set the price according to your requirement
            purchase.setPurchaseDate(new Timestamp(new Date()));
            purchase.setReader(userId); // Use setReader() instead of setReaderRef()

            // Add the purchase to the Purchase table
            db.collection("Purchase")
                    .add(purchase)
                    .addOnSuccessListener(documentReference -> {
                        // Purchase added successfully
                        Toast.makeText(getContext(), "Purchase added to library!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        // Error adding purchase
                        Log.e(TAG, "Error adding purchase to library", e);
                        Toast.makeText(getContext(), "Failed to add purchase to library", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // User ID or document ID is null
            Toast.makeText(getContext(), "User ID or Document ID is null", Toast.LENGTH_SHORT).show();
        }
    }
    private void openPaymentOptionDialog() {
        // Create an AlertDialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        // Set the title and message for the dialog
        builder.setTitle("Payment Options")
                .setMessage("Select your payment method:")
                // Set the text color to white for the message
                .setMessage(Html.fromHtml("<font color='#FFFFFF'>Select your payment method:</font>"))
                // Add buttons for different payment options
                .setPositiveButton("CBE", (dialog, which) -> {
                    addPurchaseToDatabase();
                })
                .setNegativeButton("Telebirr", (dialog, which) -> {
                    addPurchaseToDatabase();
                })
                .setNeutralButton("Cancel", null); // Neutral button to cancel the dialog

        // Create the AlertDialog
        AlertDialog dialog = builder.create();

        // Show the AlertDialog
        dialog.show();

        // Customize the text color of the buttons to white
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        Button neutralButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
        if (positiveButton != null) {
            positiveButton.setTextColor(getResources().getColor(android.R.color.white));
        }
        if (negativeButton != null) {
            negativeButton.setTextColor(getResources().getColor(android.R.color.white));
        }
        if (neutralButton != null) {
            neutralButton.setTextColor(getResources().getColor(android.R.color.white));
        }
    }

    private void showAllBooks() {
        // Fetch data from Firestore collection and display in the UI
        db.collection("Ebook")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            // Get book details from document
                            String title = document.getString("title");
                            String author = document.getString("author");
                            String description = document.getString("description");
                            // Display book details (you can update this part based on your UI)
                            titleTextView.setText("Title : " + title);
                            authorTextView.setText("Author : " + author);
                            descriptionTextView.setText("Description : " + description);
                            // You can continue similarly for other fields
                        }
                    } else {
                        // Handle errors
                        Log.e(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }
    public interface MainActivityListener {
        void addPurchaseToDatabase();
    }

    // Variable to hold reference to MainActivityListener
    private MainActivityListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MainActivityListener) {
            listener = (MainActivityListener) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement MainActivityListener");
        }
    }

    // Method to call the method in MainActivity
    private void callMethodInMainActivity() {
        listener.addPurchaseToDatabase();
    }

}

