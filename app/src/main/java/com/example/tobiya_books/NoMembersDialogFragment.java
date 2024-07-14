package com.example.tobiya_books;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class NoMembersDialogFragment extends DialogFragment {

    public static NoMembersDialogFragment newInstance() {
        return new NoMembersDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CustomDialog);
        builder.setMessage("No members found in this book club.")
                .setPositiveButton("OK", (dialog, id) -> dialog.dismiss());
        AlertDialog dialog = builder.create();

        // Set the background color to white
        dialog.setOnShowListener(d -> {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.white);
        });

        return dialog;
    }
}
