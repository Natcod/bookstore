package com.example.tobiya_books;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class PurchaseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<Book> books;
    private OnRemoveClickListener removeClickListener;
    private int expandedPosition = -1;
    private static final int VIEW_TYPE_BOOK = 0;
    private static final int VIEW_TYPE_EMPTY = 1;

    public PurchaseAdapter(Context context, List<Book> books, OnRemoveClickListener removeClickListener) {
        this.context = context;
        this.books = books;
        this.removeClickListener = removeClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        if (books.isEmpty()) {
            return VIEW_TYPE_EMPTY;
        } else {
            return VIEW_TYPE_BOOK;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_EMPTY) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_empty_library, parent, false);
            return new EmptyViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_library_group, parent, false);
            return new BookViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof EmptyViewHolder) {
            // No specific binding needed for empty view
        } else if (holder instanceof BookViewHolder) {
            BookViewHolder bookHolder = (BookViewHolder) holder;
            Book book = books.get(position);

            bookHolder.bookTitle.setText(book.getTitle());
            bookHolder.bookAuthor.setText(book.getAuthor());
            bookHolder.bookAccessType.setText(book.getAccessType());

            Glide.with(context)
                    .load(book.getCoverImage())
                    .placeholder(R.drawable.logot)
                    .error(R.drawable.logot)
                    .into(bookHolder.bookCover);

            bookHolder.removeButton.setVisibility(position == expandedPosition ? View.VISIBLE : View.GONE);

            bookHolder.itemView.setOnClickListener(view -> {
                expandedPosition = expandedPosition == position ? -1 : position;
                notifyDataSetChanged();
            });

            bookHolder.openButton.setOnClickListener(view -> {
                String pdfUrl = book.getFileURL();
                String fileName = book.getTitle() + ".pdf";

                if (DownloadUtil.isBookDownloaded(context, fileName)) {
                    openPdf(context.getFilesDir() + "/" + fileName);
                } else {
                    Toast.makeText(context, "Downloading PDF...", Toast.LENGTH_SHORT).show();

                    DownloadUtil.downloadPdf(context, pdfUrl, fileName, new DownloadUtil.DownloadCallback() {
                        @Override
                        public void onDownloadComplete(String filePath) {
                            Toast.makeText(context, "Download complete. Opening PDF...", Toast.LENGTH_SHORT).show();
                            openPdf(filePath);
                        }

                        @Override
                        public void onDownloadError(Exception e) {
                            Toast.makeText(context, "Failed to download PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

            bookHolder.removeButton.setOnClickListener(view -> {
                if (removeClickListener != null) {
                    removeClickListener.onRemoveClick(position);
                }
            });
        }
    }

    private void openPdf(String filePath) {
        FragmentActivity activity = (FragmentActivity) context;
        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, PdfViewerFragment.newInstance(filePath))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public int getItemCount() {
        return books.isEmpty() ? 1 : books.size();
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        ImageView bookCover;
        TextView bookTitle;
        TextView bookAuthor;
        TextView bookAccessType;
        Button openButton;
        Button removeButton;

        public BookViewHolder(View itemView) {
            super(itemView);
            bookCover = itemView.findViewById(R.id.book_cover);
            bookTitle = itemView.findViewById(R.id.book_title);
            bookAuthor = itemView.findViewById(R.id.book_author);
            bookAccessType = itemView.findViewById(R.id.book_access_type);
            openButton = itemView.findViewById(R.id.open_button);
            removeButton = itemView.findViewById(R.id.remove_button);
        }
    }

    public static class EmptyViewHolder extends RecyclerView.ViewHolder {
        public EmptyViewHolder(View itemView) {
            super(itemView);
        }
    }

    public interface OnRemoveClickListener {
        void onRemoveClick(int position);
    }
}
