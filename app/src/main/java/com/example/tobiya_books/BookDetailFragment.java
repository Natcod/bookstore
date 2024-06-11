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
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

import timber.log.Timber;

public class BookDetailFragment extends Fragment {

    private static final String TAG = "BookDetailFragment";

    private static final String ARG_TITLE = "title";
    private static final String ARG_AUTHOR = "author";
    private static final String ARG_DESCRIPTION = "description";
    private static final String ARG_PUBLICATION_DATE = "publicationDate";
    private static final String ARG_COVER_IMAGE_URL = "coverImageUrl";
    private static final String ARG_LANGUAGE = "language";
    private static final String ARG_PRICE = "price";
    private static final String ARG_ACCESS_TYPE = "accessType";
    private static final String ARG_FILE_URL = "fileURL";
    private static final String ARG_UPLOAD_DATE = "uploadDate";

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
    static int PRICE;

    public static BookDetailFragment newInstance(String title, String author, String description, String publicationDate,
                                                 String coverImageUrl, String language, double price, String accessType, String fileURL, String uploadDate) {
        PRICE = (int) price;
        BookDetailFragment fragment = new BookDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_AUTHOR, author);
        args.putString(ARG_DESCRIPTION, description);
        args.putString(ARG_PUBLICATION_DATE, publicationDate);
        args.putString(ARG_COVER_IMAGE_URL, coverImageUrl);
        args.putString(ARG_LANGUAGE, language);
        args.putDouble(ARG_PRICE, price);
        args.putString(ARG_ACCESS_TYPE, accessType);
        args.putString(ARG_FILE_URL, fileURL);
        args.putString(ARG_UPLOAD_DATE, uploadDate);
        fragment.setArguments(args);
        return fragment;
    }

    private MainActivityListener mainActivityListener;

    public interface MainActivityListener {
        void addPurchaseToDatabase();
    }

    public static BookDetailFragment newInstance(Book book) {
        BookDetailFragment fragment = new BookDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable("book", book);
        fragment.setArguments(args);
        return fragment;
    }

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
            String title = bundle.getString(ARG_TITLE);
            String author = bundle.getString(ARG_AUTHOR);
            String description = bundle.getString(ARG_DESCRIPTION);
            String publicationDate = bundle.getString(ARG_PUBLICATION_DATE);
            String coverImageUrl = bundle.getString(ARG_COVER_IMAGE_URL);
            String language = bundle.getString(ARG_LANGUAGE);
            double price = bundle.getDouble(ARG_PRICE);
            String accessType = bundle.getString(ARG_ACCESS_TYPE);
            String fileURL = bundle.getString(ARG_FILE_URL);
            String uploadDate = bundle.getString(ARG_UPLOAD_DATE);

            // Set data to views
            titleTextView.setText("Title : " + title);
            authorTextView.setText("Author : " + author);
            descriptionTextView.setText("Description : " + description);
            publicationDateTextView.setText("Upload Date : " + uploadDate);
            languageTextView.setText("Language : " + language);
            priceTextView.setText("Price : " + price);
            accessTypeTextView.setText("AccessType : " + accessType);
            publicationDateTextView.setText("Publication Date : " + publicationDate);

            Glide.with(requireContext())
                    .load(coverImageUrl)
                    .into(coverImageView);

            // Retrieve document ID based on criteria
            retrieveDocumentId(fileURL);

            // Set back button click listener
            backButton.setOnClickListener(v -> {
                // Navigate back to HomeFragment
                getActivity().getSupportFragmentManager().popBackStack();
            });
        }

        return view;
    }

    private void checkIfBookInLibrary(String fileURL, String accessType) {
        // Retrieve the user ID from shared preferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("UserID", null);

        if (userId != null) {
            // Reference to your "Purchase" collection
            CollectionReference purchaseCollection = db.collection("Purchase");

            // Query to check if the user already purchased the book
            purchaseCollection
                    .whereEqualTo("reader", userId)
                    .whereEqualTo("ebook", db.document("Ebook/" + documentId))
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            // Book is already in the user's library
                            buyNowButton.setText("Already Added to Library");
                            buyNowButton.setOnClickListener(v -> openLibraryFragment());
                        } else {
                            // Book is not in the user's library
                            setBuyNowButtonClickListener(accessType);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error checking purchase", e);
                        setBuyNowButtonClickListener(accessType);
                    });
        } else {
            Toast.makeText(getContext(), "User ID is null", Toast.LENGTH_SHORT).show();
        }
    }

    private void setBuyNowButtonClickListener(String accessType) {
        // Set button label and click listener based on access type
        if (accessType != null) {
            switch (accessType) {
                case "Free":
                    buyNowButton.setText("Add to Library");
                    buyNowButton.setOnClickListener(v -> addPurchaseToDatabase());
                    break;
                case "Paid":
                    buyNowButton.setText("Buy Now");
                    buyNowButton.setOnClickListener(v -> openPaymentOptionDialog());
                    break;
                case "Subscription":
                    buyNowButton.setText("Subscribe");
                    buyNowButton.setOnClickListener(v -> checkSubscriptionAndAddToLibrary());
                    break;
                case "All":
                    // Handle 'All' access type if necessary
                    break;
            }
        }
    }

    private void checkSubscriptionAndAddToLibrary() {
        // Retrieve the user ID from shared preferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("UserID", null);

        if (userId != null) {
            // Reference to your "Subscription" collection
            CollectionReference subscriptionCollection = db.collection("Subscription");

            // Perform a query to get the subscription document for the user
            subscriptionCollection
                    .whereEqualTo("reader", db.document("Reader/" + userId))
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            DocumentSnapshot document = task.getResult().getDocuments().get(0);
                            Timestamp endDate = document.getTimestamp("endDate");

                            if (endDate != null && endDate.toDate().after(new Date())) {
                                // Subscription is valid
                                addPurchaseToDatabase();
                            } else {
                                // Subscription has expired
                                showSubscriptionDialog();
                            }
                        } else {
                            // No subscription found or error
                            showSubscriptionDialog();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error checking subscription", e);
                        showSubscriptionDialog();
                    });
        } else {
            Toast.makeText(getContext(), "User ID is null", Toast.LENGTH_SHORT).show();
        }
    }

    private void showSubscriptionDialog() {
        Toast.makeText(getContext(), "Please subscribe to access this book.", Toast.LENGTH_SHORT).show();
        ((MainActivity) requireActivity()).showBottomDialog();
    }

    private void retrieveDocumentId(String fileURL) {
        // Reference to your "Ebook" collection
        CollectionReference ebookCollection = db.collection("Ebook");

        // Perform a query to get the document
        ebookCollection
                .whereEqualTo("fileURL", fileURL)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);

                        // Get the document ID
                        documentId = document.getId();
                        // Log the document ID
                        Timber.tag(TAG).d("Document ID: %s", documentId);
                        // Update UI or perform other actions with the document ID
                        // For example, you can set it to a TextView
                        accessTypeTextView.setText("Access Type: " + document.getString("accessType"));

                        // Check if the book is already in the user's library
                        checkIfBookInLibrary(fileURL, document.getString("accessType"));
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
            purchase.setPrice(PRICE); // Set the price according to your requirement
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

    private void openLibraryFragment() {

        Library libraryFragment = new Library();

        FragmentManager fragmentManager = getParentFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, libraryFragment)
                .addToBackStack(null)
                .commit();
    }

}

