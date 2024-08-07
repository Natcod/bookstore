package com.example.tobiya_books;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
    static Double PRICE;
    private SubscriptionManager subscriptionManager;

    public static BookDetailFragment newInstance(String title, String author, String description, String publicationDate,
                                                 String coverImageUrl, String language, double price, String accessType, String fileURL, String uploadDate) {
        PRICE = (Double) price;
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

    public static BookDetailFragment newInstance(String bookId) {
        BookDetailFragment fragment = new BookDetailFragment();
        Bundle args = new Bundle();
        args.putString("book_id", bookId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_book_detail, container, false);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        subscriptionManager = new SubscriptionManager(getActivity());
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
            if (bundle.containsKey("book")) {
                Book book = (Book) bundle.getSerializable("book");
                if (book != null) {
                    setBookDetails(book);
                }
            } else if (bundle.containsKey("book_id")) {
                String bookId = bundle.getString("book_id");
                if (bookId != null) {
                    fetchBookDetails(bookId);
                }
            } else {
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

                // Extract year from publication date
                String[] dateParts = publicationDate.split(" ");
                String publicationYear = dateParts[dateParts.length - 1];
                // Set data to views
                titleTextView.setText("Title : " + title);
                authorTextView.setText("Author : " + author);
                descriptionTextView.setText("Description : " + description);
                languageTextView.setText("Language : " + language);
                priceTextView.setText("Price : " + price);
                accessTypeTextView.setText("AccessType : " + accessType);
                publicationDateTextView.setText("Publication Year : " + publicationYear);

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
        }

        return view;
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

    private void fetchBookDetails(String bookId) {
        db.document(bookId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Assuming the book document has fields title, author, etc.
                String title = documentSnapshot.getString("title");
                String author = documentSnapshot.getString("author");
                String description = documentSnapshot.getString("description");
                String publicationDate = documentSnapshot.getString("publication_date");
                String coverImageUrl = documentSnapshot.getString("cover_image_url");
                String language = documentSnapshot.getString("language");
                double price = documentSnapshot.getDouble("price");
                String accessType = documentSnapshot.getString("access_type");

                // Set data to views
                titleTextView.setText("Title: " + title);
                authorTextView.setText("Author: " + author);
                descriptionTextView.setText("Description: " + description);
                languageTextView.setText("Language: " + language);
                priceTextView.setText("Price: " + price);
                accessTypeTextView.setText("AccessType: " + accessType);
                publicationDateTextView.setText("Publication Year: " + publicationDate.split(" ")[publicationDate.split(" ").length - 1]);

                Glide.with(requireContext()).load(coverImageUrl).into(coverImageView);
            }
        });
    }

    private void setBookDetails(Book book) {
        titleTextView.setText("Title : " + book.getTitle());
        authorTextView.setText("Author : " + book.getAuthor());
        descriptionTextView.setText("Description : " + book.getDescription());
        languageTextView.setText("Language : " + book.getLanguage());
        priceTextView.setText("Price : " + book.getPrice());
        accessTypeTextView.setText("AccessType : " + book.getAccessType());

        // Format publication date to display only the year
        if (book.getPublicationDate() != null) {
            Date publicationDate = book.getPublicationDate().toDate(); // Convert Timestamp to Date
            String publicationYear = new SimpleDateFormat("yyyy", Locale.getDefault()).format(publicationDate);
            publicationDateTextView.setText("Publication Year : " + publicationYear);
        }


        Glide.with(requireContext())
                .load(book.getCoverImage())
                .into(coverImageView);

        // Retrieve document ID based on criteria
        retrieveDocumentId(book.getFileURL());

        // Set back button click listener
        backButton.setOnClickListener(v -> {
            // Navigate back to HomeFragment
            getActivity().getSupportFragmentManager().popBackStack();
        });
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
                    .whereEqualTo("reader", db.collection("Reader").document(userId))
                    .whereEqualTo("ebook", db.document("Ebook/" + documentId))
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            // Check each document to find the approval status
                            boolean bookFound = false;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String approvalStatus = document.getString("approvalStatus");
                                if (!approvalStatus.equals("expired") && !approvalStatus.equals("rejected")) {
                                    bookFound = true;
                                    if (approvalStatus.equals("pending")) {
                                        buyNowButton.setText("Already Added to Library, Pending Approval");
                                    } else if (approvalStatus.equals("approved")) {
                                        buyNowButton.setText("Already Added to Library, Open");
                                    }
                                    buyNowButton.setOnClickListener(v -> openLibraryFragment());
                                    break;
                                }
                            }
                            if (!bookFound) {
                                // Book is not in the user's library with valid approval status
                                setBuyNowButtonClickListener(accessType);
                            }
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
                    buyNowButton.setOnClickListener(v -> addPurchaseToDatabase(null, null)); // For free, no transaction ID
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
            // Use SubscriptionRepository to check subscription status
            SubscriptionRepository subscriptionRepository = new SubscriptionRepository();
            subscriptionRepository.isReaderSubscribed(userId, isSubscribed -> {
                if (isSubscribed) {
                    // Subscription is valid, proceed with adding to library
                    CollectionReference subscriptionCollection = db.collection("Subscription");

                    // Perform a query to get the subscription documents for the user
                    subscriptionCollection
                            .whereEqualTo("reader", db.document("Reader/" + userId))
                            .get()
                            .addOnCompleteListener(subscriptionTask -> {
                                if (subscriptionTask.isSuccessful() && !subscriptionTask.getResult().isEmpty()) {
                                    boolean foundApproved = false;
                                    boolean foundPending = false;
                                    for (DocumentSnapshot subscriptionDocument : subscriptionTask.getResult().getDocuments()) {
                                        String approvalStatus = subscriptionDocument.getString("approvalStatus");
                                        Timestamp endDate = subscriptionDocument.getTimestamp("endDate");
                                        String type = subscriptionDocument.getString("type");
                                        String subscriptionId = subscriptionDocument.getId();

                                        if ("approved".equals(approvalStatus) && endDate != null && endDate.toDate().after(new Date()) && type != null && !type.isEmpty()) {
                                            foundApproved = true;
                                            SubscriptionManager subscriptionManager = new SubscriptionManager(getContext());
                                            subscriptionManager.updateSubscriptionType(type, subscriptionId);

                                            if (subscriptionManager.canAddBook(endDate.toDate())) {
                                                // Replace "book_id" with the actual book ID you are about to add
                                                Date endedDate = subscriptionDocument.getTimestamp("endDate").toDate();
                                                String documentId = "book_id"; // Get the actual book ID here
                                                subscriptionManager.addBookToLibrary(documentId);
                                                addPurchaseToDatabase(null, endedDate); // For subscription, no transaction ID

                                                // Call checkIfBookInLibrary to ensure the book is not added again
                                                checkIfBookInLibrary(documentId, "subscription");
                                            } else {
                                                Log.e(TAG, "Book limit reached or subscription expired");
                                                updateApprovalStatus(subscriptionId, "rejected"); // Update status to "rejected"
                                                showSubscriptionDialog("limit_reached");
                                            }
                                            break; // Exit the loop once an approved subscription is processed
                                        } else if ("pending".equals(approvalStatus)) {
                                            foundPending = true;
                                        }
                                    }

                                    if (!foundApproved) {
                                        if (foundPending) {
                                            showSubscriptionDialog("pending");
                                        } else {
                                            showSubscriptionDialog(null);
                                        }
                                    }
                                } else {
                                    // No subscription found or error
                                    showSubscriptionDialog(null);
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error checking subscription", e);
                                showSubscriptionDialog(null);
                            });
                } else {
                    // No valid subscription found
                    showSubscriptionDialog(null);
                }
            });
        } else {
            Toast.makeText(getContext(), "User ID is null", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateApprovalStatus(String subscriptionId, String newStatus) {
        if (subscriptionId != null && !subscriptionId.isEmpty()) {
            DocumentReference subscriptionRef = db.collection("Subscription").document(subscriptionId);
            subscriptionRef.update("approvalStatus", newStatus)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Subscription status updated to " + newStatus))
                    .addOnFailureListener(e -> Log.e(TAG, "Error updating subscription status", e));
        }
    }

    private void showSubscriptionDialog(String approvalStatus) {
        String message;
        if ("pending".equals(approvalStatus)) {
            message = "Your subscription is pending approval.";
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            return; // Do not show the bottom sheet if subscription is pending
        } else if ("rejected".equals(approvalStatus)) {
            message = "Your subscription has been rejected.";
        } else if ("approved".equals(approvalStatus)) {
            message = "Your subscription is expired. Please renew to access this book.";
        } else if ("limit_reached".equals(approvalStatus)) {
            message = "Book limit reached for your subscription type. Please renew to access this book.";
        } else {
            message = "Please subscribe to access this book.";
        }

        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
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

    private void openPaymentOptionDialog() {
        // Create an AlertDialog builder with the custom style
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.AlertDialogCustomStyle);

        // Create a container for the custom view
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50); // Add padding to the container
        layout.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.var)); // Set background color

        // Create a TextView for the dialog message
        TextView messageTextView = new TextView(requireContext());
        messageTextView.setText("Select your payment method:");
        messageTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple));
        messageTextView.setPadding(0, 0, 0, 20); // Add some padding for better UI
        layout.addView(messageTextView); // Add the TextView to the container

        // Set the title and custom view for the dialog
        builder.setTitle("Payment Options")
                .setView(layout)
                // Add buttons for different payment options
                .setPositiveButton("CBE", (dialog, which) -> {
                    showTransactionIdDialog();
                })
                .setNegativeButton("Telebirr", (dialog, which) -> {
                    showTransactionIdDialog();
                })
                .setNeutralButton("Cancel", null); // Neutral button to cancel the dialog

        // Create the AlertDialog
        AlertDialog dialog = builder.create();

        // Show the AlertDialog
        dialog.show();

        // Customize the text color of the buttons to use the same color from resources
        int buttonTextColor = ContextCompat.getColor(requireContext(), R.color.black);
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        Button neutralButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);

        if (positiveButton != null) {
            positiveButton.setTextColor(buttonTextColor);
        }
        if (negativeButton != null) {
            negativeButton.setTextColor(buttonTextColor);
        }
        if (neutralButton != null) {
            neutralButton.setTextColor(buttonTextColor);
        }
    }

    private void showTransactionIdDialog() {
        // Create an AlertDialog builder with the custom style
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.AlertDialogCustomStyle);

        // Create a container for the custom view
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50); // Add padding to the container
        layout.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.var)); // Set background color

        // Create a TextView for the transaction message
        TextView transactionMessageTextView = new TextView(requireContext());
        transactionMessageTextView.setText("After completing payment using this account number 1111, enter the transaction ID here:");
        transactionMessageTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple));
        transactionMessageTextView.setPadding(0, 0, 0, 20); // Add some padding for better UI
        layout.addView(transactionMessageTextView); // Add the TextView to the container

        // Create an EditText for the user to enter the transaction ID
        EditText transactionIdEditText = new EditText(requireContext());
        transactionIdEditText.setHint("Transaction ID");
        transactionIdEditText.setTextColor(ContextCompat.getColor(requireContext(),R.color.purple));
        layout.addView(transactionIdEditText); // Add the EditText to the container

        // Set the title and custom view for the dialog
        builder.setTitle("Enter Transaction ID")
                .setView(layout)
                .setPositiveButton("Submit", (dialog, which) -> {
                    // Get the transaction ID entered by the user
                    String transactionId = transactionIdEditText.getText().toString().trim();
                    if (!transactionId.isEmpty()) {
                        // Proceed with adding purchase to database with the transaction ID
                        addPurchaseToDatabase(transactionId,null);
                    } else {
                        Toast.makeText(getContext(), "Transaction ID cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null); // Neutral button to cancel the dialog

        // Create the AlertDialog
        AlertDialog dialog = builder.create();

        // Show the AlertDialog
        dialog.show();

        // Customize the text color of the buttons to use the same color from resources
        int buttonTextColor = ContextCompat.getColor(requireContext(), R.color.black);
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        Button neutralButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);

        if (positiveButton != null) {
            positiveButton.setTextColor(buttonTextColor);
        }
        if (negativeButton != null) {
            negativeButton.setTextColor(buttonTextColor);
        }
        if (neutralButton != null) {
            neutralButton.setTextColor(buttonTextColor);
        }
    }

    private void addPurchaseToDatabase(String transactionId, Date endDate) {
        // Retrieve the user ID from shared preferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("UserID", null);

        if (userId != null && documentId != null) {
            // Fetch the ebook document to get the price
            db.collection("Ebook").document(documentId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Get the price from the document
                            Double price = documentSnapshot.getDouble("price");
                            String accessType = documentSnapshot.getString("accessType");

                            if (price != null) {
                                // Create a new Purchase object
                                Purchase purchase = new Purchase();
                                purchase.setEbook(db.collection("Ebook").document(documentId));
                                purchase.setPrice(price); // Set the price from the document
                                purchase.setPurchaseDate(new Timestamp(new Date()));
                                purchase.setReader(db.collection("Reader").document(userId));
                                purchase.setTransactionId(transactionId); // Set the transaction ID
                                purchase.setApprovalStatus("pending"); // Set approval status to "pending"
                                purchase.setAccessType(accessType);
                                if (endDate != null) {
                                    purchase.setEndDate(new Timestamp(endDate)); // Set the end date for subscriptions
                                }

                                // Add the purchase to the Purchase collection
                                db.collection("Purchase")
                                        .add(purchase)
                                        .addOnSuccessListener(documentReference -> {
                                            // Purchase added successfully
                                            Toast.makeText(getContext(), "Purchase added to library pending approval!", Toast.LENGTH_SHORT).show();
                                            buyNowButton.setText("Already Added to Library, PENDING APPROVAL");
                                            buyNowButton.setOnClickListener(v -> openLibraryFragment());
                                        })
                                        .addOnFailureListener(e -> {
                                            // Error adding purchase
                                            Log.e(TAG, "Error adding purchase to library", e);
                                            Toast.makeText(getContext(), "Failed to add purchase to library", Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                Toast.makeText(getContext(), "Price not found in document", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getContext(), "Ebook document not found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error fetching ebook document", e);
                        Toast.makeText(getContext(), "Failed to fetch ebook details", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // User ID or document ID is null
            Toast.makeText(getContext(), "User ID or Document ID is null", Toast.LENGTH_SHORT).show();
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
