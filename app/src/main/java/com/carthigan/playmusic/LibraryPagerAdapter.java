package com.carthigan.playmusic;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class LibraryPagerAdapter extends FragmentStateAdapter {

    private final String[] tabTitles = new String[]{"PLAYLISTS", "ARTISTS", "ALBUMS", "SONGS", "GENRES"};

    public LibraryPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return LibraryTabFragment.newInstance(tabTitles[position]);
    }

    @Override
    public int getItemCount() {
        return tabTitles.length;
    }
    
    public String getTabTitle(int position) {
        return tabTitles[position];
    }
}
