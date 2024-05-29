package com.example.tobiya_books;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso; // or use Glide

public class BookDetailFragment extends Fragment {

    private ImageView coverImageView;
    private TextView titleTextView;
    private TextView authorTextView;
    private TextView descriptionTextView;
    private TextView publicationDateTextView;
    private Button buyNowButton;
    private TextView languageTextView;
    private TextView priceTextView;
    private TextView accessTypeTextView;
    private  Button backButton;


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
            String price = bundle.getString("price");
            String accessType = bundle.getString("accessType");

            // Set data to views
            titleTextView.setText("Title : " + title);
            authorTextView.setText("Author : " + author);
            descriptionTextView.setText("Description : " + description);
            publicationDateTextView.setText("Publication Date : " + publicationDate);
            languageTextView.setText("Language : " + language);
            priceTextView.setText("Price : " + price);
            accessTypeTextView.setText("Access Type: " + accessType);

            Glide.with(requireContext())
                    .load(coverImageUrl)
                    .into(coverImageView);


            // Set button click listener
            buyNowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getContext(), "Buy Now clicked!", Toast.LENGTH_SHORT).show();
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
        }

        return view;
    }
}