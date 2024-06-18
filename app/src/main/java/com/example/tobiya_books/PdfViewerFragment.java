package com.example.tobiya_books;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;

public class PdfViewerFragment extends Fragment {

    private static final String ARG_PDF_PATH = "pdf_path";
    private String pdfPath;
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton floatingActionButton;
    private BottomAppBar bottomAppBar;

    public PdfViewerFragment() {
        // Required empty public constructor
    }

    public static PdfViewerFragment newInstance(String pdfPath) {
        PdfViewerFragment fragment = new PdfViewerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PDF_PATH, pdfPath);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            pdfPath = getArguments().getString(ARG_PDF_PATH);
        }
        // Notify the fragment that it should participate in populating the options menu
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pdf_viewer, container, false);
        PDFView pdfView = view.findViewById(R.id.pdfView);

        // Find and hide the BottomNavigationView, FloatingActionButton, and BottomAppBar
        bottomNavigationView = getActivity().findViewById(R.id.bottom_navigation);
        floatingActionButton = getActivity().findViewById(R.id.fab);
        bottomAppBar = getActivity().findViewById(R.id.bottomAppBar);

        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.GONE);
        }
        if (floatingActionButton != null) {
            floatingActionButton.setVisibility(View.GONE);
        }
        if (bottomAppBar != null) {
            bottomAppBar.setVisibility(View.GONE);
        }

        // Load the PDF file from the local storage
        pdfView.fromFile(new File(pdfPath))
                .defaultPage(0)
                .enableSwipe(true)
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .onLoad(new OnLoadCompleteListener() {
                    @Override
                    public void loadComplete(int nbPages) {
                        // Do something on PDF load complete
                    }
                })
                .onPageChange(new OnPageChangeListener() {
                    @Override
                    public void onPageChanged(int page, int pageCount) {
                        // Do something on page change
                    }
                })
                .scrollHandle(new DefaultScrollHandle(getContext()))
                .spacing(10) // in dp
                .pageFitPolicy(FitPolicy.BOTH)
                .load();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Show the BottomNavigationView, FloatingActionButton, and BottomAppBar when the fragment is destroyed
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.VISIBLE);
        }
        if (floatingActionButton != null) {
            floatingActionButton.setVisibility(View.VISIBLE);
        }
        if (bottomAppBar != null) {
            bottomAppBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // Hide the search and notification menu items
        MenuItem searchItem = menu.findItem(R.id.action_search);
        if (searchItem != null) {
            searchItem.setVisible(false);
        }
    }
}
