package com.example.tobiya_books;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class BookPagerAdapter extends FragmentStateAdapter {

    private static final String[] TAB_TITLES = new String[]{"All", "Free", "Paid", "Subscription"};

    public BookPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 1:
                return BookListFragment.newInstance("Free");
            case 2:
                return BookListFragment.newInstance("Paid");
            case 3:
                return BookListFragment.newInstance("Subscription");
            default:
                return BookListFragment.newInstance("All");
        }
    }

    @Override
    public int getItemCount() {
        return TAB_TITLES.length;
    }
}

