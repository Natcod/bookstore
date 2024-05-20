package com.example.tobiya_books;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso; // or use Glide

public class BookDetailFragment extends Fragment {

    private ImageView coverImageView;
    private TextView titleTextView;
    private TextView authorTextView;
    private TextView descriptionTextView;
    private TextView publicationDateTextView;
    private Button buyNowButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_book_detail, container, false);

        // Initialize views
        coverImageView = view.findViewById(R.id.detail_cover_image);
        titleTextView = view.findViewById(R.id.detail_title);
        authorTextView = view.findViewById(R.id.detail_author);
        descriptionTextView = view.findViewById(R.id.detail_description);
        publicationDateTextView = view.findViewById(R.id.detail_publication_date);
        buyNowButton = view.findViewById(R.id.button_buy_now);

        // Get data from arguments
        Bundle bundle = getArguments();
        if (bundle != null) {
            String title = bundle.getString("title");
            String author = bundle.getString("author");
            String description = bundle.getString("description");
            String publicationDate = bundle.getString("publicationDate");
            String coverImageUrl = bundle.getString("coverImageUrl");

            // Set data to views
            titleTextView.setText(title);
            authorTextView.setText(author);
            descriptionTextView.setText(description);
            publicationDateTextView.setText(publicationDate);
            Picasso.get().load(coverImageUrl).into(coverImageView);

            // Set button click listener
            buyNowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Handle "Buy Now" button click
                }
            });
        }

        return view;
    }
}

