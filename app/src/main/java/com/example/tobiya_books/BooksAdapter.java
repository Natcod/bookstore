package com.example.tobiya_books;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class BooksAdapter extends RecyclerView.Adapter<BooksAdapter.BookViewHolder> {

    private List<Book> books;
    private LayoutInflater inflater;
    private OnBookClickListener listener;
    private static Map<String, Integer> imageMap;

    public BooksAdapter(Context context, List<Book> books, OnBookClickListener listener) {
        this.inflater = LayoutInflater.from(context);
        this.books = books;
        this.listener = listener;
        imageMap = DrawableUtils.getDrawableMap(context);
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = books.get(position);
        holder.bind(book, listener);
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {
        private TextView titleTextView;
        private TextView authorTextView;
        private ImageView coverImageView;

        public BookViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.textview_title);
            authorTextView = itemView.findViewById(R.id.textview_author);
            coverImageView = itemView.findViewById(R.id.imageview_cover);
        }

        public void bind(final Book book, final OnBookClickListener listener) {
            titleTextView.setText(book.getTitle());
            authorTextView.setText(book.getAuthor());

            // Load cover image from drawable folder using the map
            Integer imageResId = imageMap.get(book.getCoverImageName());
            if (imageResId != null) {
                coverImageView.setImageResource(imageResId);
            } else {
                coverImageView.setImageResource(R.drawable.yoratorad); // Set a default image if not found
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onBookClick(book);
                }
            });
        }
    }

    public interface OnBookClickListener {
        void onBookClick(Book book);
    }
}