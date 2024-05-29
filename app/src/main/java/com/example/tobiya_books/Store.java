package com.example.tobiya_books;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class Store extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private Button buttonAll;
    private Button buttonFree;
    private Button buttonPaid;
    private Button buttonSubscription;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_store, container, false);

        tabLayout = view.findViewById(R.id.tab_layout);
        viewPager = view.findViewById(R.id.view_pager);

        buttonAll = view.findViewById(R.id.button_all);
        buttonFree = view.findViewById(R.id.button_free);
        buttonPaid = view.findViewById(R.id.button_paid);
        buttonSubscription = view.findViewById(R.id.button_subscription);

        BookPagerAdapter adapter = new BookPagerAdapter(getActivity());
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("All");
                            break;
                        case 1:
                            tab.setText("Free");
                            break;
                        case 2:
                            tab.setText("Paid");
                            break;
                        case 3:
                            tab.setText("Subscription");
                            break;
                    }
                }).attach();

        // Set button click listeners to switch tabs
        buttonAll.setOnClickListener(v -> viewPager.setCurrentItem(0));
        buttonFree.setOnClickListener(v -> viewPager.setCurrentItem(1));
        buttonPaid.setOnClickListener(v -> viewPager.setCurrentItem(2));
        buttonSubscription.setOnClickListener(v -> viewPager.setCurrentItem(3));

        return view;
    }
}
