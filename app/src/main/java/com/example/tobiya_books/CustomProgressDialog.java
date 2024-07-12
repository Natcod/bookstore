package com.example.tobiya_books;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class CustomProgressDialog {
    private Dialog dialog;
    private ProgressBar progressBar;
    private TextView progressText;

    public CustomProgressDialog(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.custom_progress_dialog, null);
        progressBar = view.findViewById(R.id.progress_bar);
        progressText = view.findViewById(R.id.progress_text);

        dialog = new Dialog(context);
        dialog.setContentView(view);
        dialog.setCancelable(false);
    }

    public void show() {
        dialog.show();
    }

    public void dismiss() {
        dialog.dismiss();
    }

    public void setProgress(int progress) {
        progressBar.setProgress(progress);
        progressText.setText(progress + "%");
    }
}
