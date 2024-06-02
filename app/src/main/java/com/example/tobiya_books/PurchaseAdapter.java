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

public class PurchaseAdapter extends RecyclerView.Adapter<PurchaseAdapter.ViewHolder> {

    private Context context;
    private List<Book> books;

    public PurchaseAdapter(Context context, List<Book> books) {
        this.context = context;
        this.books = books;
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

        Glide.with(context)
                .load(book.getCoverImage())
                .placeholder(R.drawable.logot)
                .error(R.drawable.logot)
                .into(holder.bookCover);

        holder.openButton.setOnClickListener(view -> {
            String pdfUrl = book.getFileURL();
            String fileName = book.getTitle() + ".pdf";

            if (DownloadUtil.isBookDownloaded(context, fileName)) {
                openPdf(context.getFilesDir() + "/" + fileName);
            } else {
                // Show a toast indicating that the download has started
                Toast.makeText(context, "Downloading PDF...", Toast.LENGTH_SHORT).show();

                DownloadUtil.downloadPdf(context, pdfUrl, fileName, new DownloadUtil.DownloadCallback() {
                    @Override
                    public void onDownloadComplete(String filePath) {
                        // Show a toast indicating that the download is complete
                        Toast.makeText(context, "Download complete. Opening PDF...", Toast.LENGTH_SHORT).show();
                        // Open the PDF after download completes
                        openPdf(filePath);
                    }

                    @Override
                    public void onDownloadError(Exception e) {
                        Toast.makeText(context, "Failed to download PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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
        Button openButton;

        public ViewHolder(View itemView) {
            super(itemView);
            bookCover = itemView.findViewById(R.id.book_cover);
            bookTitle = itemView.findViewById(R.id.book_title);
            bookAuthor = itemView.findViewById(R.id.book_author);
            openButton = itemView.findViewById(R.id.open_button);
        }
    }
}
