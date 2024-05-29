package com.example.tobiya_books;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerAdapterChat extends FragmentStateAdapter {

    public ViewPagerAdapterChat(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new AllGroupsFragment();
            case 1:
                return new JoinedFragment();
            default:
                return new AllGroupsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;  // Number of tabs
    }
}
