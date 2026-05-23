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
        
        String[] titles = {"Random Access Memories", "1989", "Thriller", "Abbey Road", "The Dark Side of the Moon", "Rumours"};
        String[] artists = {"Daft Punk", "Taylor Swift", "Michael Jackson", "The Beatles", "Pink Floyd", "Fleetwood Mac"};
        
        for (int i = 0; i < titles.length; i++) {
            TrackJson t = new TrackJson();
            t.title = titles[i];
            t.artist = artists[i];
            list.add(t);
        }
        return list;
    }
}