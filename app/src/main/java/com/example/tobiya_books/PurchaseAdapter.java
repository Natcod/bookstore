package com.example.tobiya_books;

import android.app.ProgressDialog;
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

public class PurchaseAdapter extends RecyclerView.Adapter<PurchaseAdapter.ViewHolder> {

    private Context context;
    private List<Book> books;
    private int selectedItemPosition = RecyclerView.NO_POSITION; // Track selected item position
    private OnRemoveClickListener removeClickListener;

    public PurchaseAdapter(Context context, List<Book> books, OnRemoveClickListener removeClickListener) {
        this.context = context;
        this.books = books;
        this.removeClickListener = removeClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_library_group, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Book book = books.get(position);
        holder.bookTitle.setText(book.getTitle());
        holder.bookAuthor.setText(book.getAuthor());
        holder.bookAccessType.setText(book.getAccessType()); // Bind accessType

        Glide.with(context)
                .load(book.getCoverImage())
                .placeholder(R.drawable.logot)
                .error(R.drawable.logot)
                .into(holder.bookCover);

        // Toggle remove button visibility based on selected item
        if (holder.getBindingAdapterPosition() == RecyclerView.NO_POSITION) {
            holder.removeButton.setVisibility(View.GONE);
        } else {
            holder.removeButton.setVisibility(holder.getBindingAdapterPosition() == selectedItemPosition ? View.VISIBLE : View.GONE);
        }

        // Handle card click to toggle remove button visibility
        holder.itemView.setOnClickListener(view -> {
            int clickedPosition = holder.getBindingAdapterPosition();
            if (clickedPosition == RecyclerView.NO_POSITION) {
                return; // Invalid position
            }

            if (holder.removeButton.getVisibility() == View.VISIBLE) {
                holder.removeButton.setVisibility(View.GONE);
            } else {
                holder.removeButton.setVisibility(View.VISIBLE);
            }
        });

        holder.openButton.setOnClickListener(view -> {
            String pdfUrl = book.getFileURL();
            String fileName = book.getTitle() + ".pdf";

            if (DownloadUtil.isBookDownloaded(context, fileName)) {
                openPdf(context.getFilesDir() + "/" + fileName);
            } else {
                // Show a ProgressDialog indicating that the download has started
                ProgressDialog progressDialog = new ProgressDialog(context);
                progressDialog.setMessage("Downloading PDF...");
                progressDialog.setCancelable(false);
                progressDialog.show();

                DownloadUtil.downloadPdf(context, pdfUrl, fileName, new DownloadUtil.DownloadCallback() {
                    @Override
                    public void onDownloadComplete(String filePath) {
                        // Dismiss the ProgressDialog and update the UI on the main thread
                        ((FragmentActivity) context).runOnUiThread(() -> {
                            progressDialog.dismiss();
                            Toast.makeText(context, "Download complete. Opening PDF...", Toast.LENGTH_SHORT).show();
                            openPdf(filePath);
                        });
                    }

                    @Override
                    public void onDownloadError(Exception e) {
                        // Dismiss the ProgressDialog and update the UI on the main thread
                        ((FragmentActivity) context).runOnUiThread(() -> {
                            progressDialog.dismiss();
                            Toast.makeText(context, "Failed to download PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            }
        });

        holder.removeButton.setOnClickListener(view -> {
            int clickedPosition = holder.getBindingAdapterPosition();
            if (removeClickListener != null && clickedPosition != RecyclerView.NO_POSITION) {
                removeClickListener.onRemoveClick(clickedPosition);
            }
        });
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
        return books.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView bookCover;
        TextView bookTitle;
        TextView bookAuthor;
        TextView bookAccessType; // New TextView for accessType
        Button openButton;
        Button removeButton;

        public ViewHolder(View itemView) {
            super(itemView);
            bookCover = itemView.findViewById(R.id.book_cover);
            bookTitle = itemView.findViewById(R.id.book_title);
            bookAuthor = itemView.findViewById(R.id.book_author);
            bookAccessType = itemView.findViewById(R.id.book_access_type); // Initialize the new TextView
            openButton = itemView.findViewById(R.id.open_button);
            removeButton = itemView.findViewById(R.id.remove_button);
        }
    }

    // Interface for remove button click listener
    public interface OnRemoveClickListener {
        void onRemoveClick(int position);
    }
}
