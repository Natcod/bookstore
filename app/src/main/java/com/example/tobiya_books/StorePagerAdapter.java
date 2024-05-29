package com.example.tobiya_books;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class StorePagerAdapter extends FragmentStateAdapter {

    public StorePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return BookListFragment.newInstance("All");
            case 1:
                return BookListFragment.newInstance("Free");
            case 2:
                return BookListFragment.newInstance("Paid");
            case 3:
                return BookListFragment.newInstance("Subscription");
            default:
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
