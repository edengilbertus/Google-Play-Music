package com.carthigan.playmusic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.carthigan.playmusic.api.models.TrackJson;
import com.carthigan.playmusic.data.LocalMusicHelper;

import java.util.ArrayList;
import java.util.List;

public class ShopFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shop, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.rv_shop);
        
        // Shop uses a 2-column grid to look like a digital storefront
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        ShopAdapter adapter = new ShopAdapter();
        recyclerView.setAdapter(adapter);
        adapter.setItems(getMockShopItems());

        return view;
    }

    private List<TrackJson> getMockShopItems() {
        List<TrackJson> list = new ArrayList<>();
        
        if (getActivity() instanceof MainActivity) {
            List<TrackJson> allAlbums = ((MainActivity) getActivity()).getLocalMusicHelper().getAlbums();
            if (allAlbums != null && !allAlbums.isEmpty()) {
                // Shuffle to get a random assortment for the storefront
                java.util.Collections.shuffle(allAlbums);
                int limit = Math.min(10, allAlbums.size());
                list.addAll(allAlbums.subList(0, limit));
                return list;
            }
        }
        
        // Absolute fallback if no local music exists
        String[] titles = {"Awake", "Ascend", "Fallen Embers"};
        String[] artists = {"Illenium", "Illenium", "Illenium"};
        
        for (int i = 0; i < titles.length; i++) {
            TrackJson t = new TrackJson();
            t.title = titles[i];
            t.artist = artists[i];
            
            t.albumArtRef = new ArrayList<>();
            com.carthigan.playmusic.api.models.ImageRefJson img = new com.carthigan.playmusic.api.models.ImageRefJson();
            img.url = "https://i.scdn.co/image/ab67616d0000b27376c666fb271b3e83b482cb8d";
            t.albumArtRef.add(img);
            
            list.add(t);
        }
        return list;
    }
}