package com.carthigan.playmusic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.carthigan.playmusic.api.models.ImageRefJson;
import com.carthigan.playmusic.api.models.TrackJson;

import java.util.ArrayList;
import java.util.List;

public class PodcastsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_podcasts, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.rv_podcasts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        PodcastAdapter adapter = new PodcastAdapter(track -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).playTrackFromLibrary(track);
            }
        });

        recyclerView.setAdapter(adapter);
        adapter.setItems(getMockPodcasts());

        return view;
    }

    private List<TrackJson> getMockPodcasts() {
        List<TrackJson> podcasts = new ArrayList<>();
        
        podcasts.add(createMockPodcast("1. The History of Rome", "Mike Duncan", null));
        podcasts.add(createMockPodcast("Episode 142: Reply All", "Gimlet Media", "https://upload.wikimedia.org/wikipedia/en/9/98/Crawl Outta Love_%28featuring_Young_Thug%29_%28Official_Single_Cover%29.png"));
        podcasts.add(createMockPodcast("Stuff You Should Know - 100", "iHeartRadio", null));
        podcasts.add(createMockPodcast("Hardcore History: Blueprint for Armageddon", "Dan Carlin", "https://i.scdn.co/image/ab67616d0000b27376c666fb271b3e83b482cb8d"));
        podcasts.add(createMockPodcast("The Daily: What to know today", "The New York Times", null));
        
        return podcasts;
    }

    private TrackJson createMockPodcast(String title, String publisher, String imageUrl) {
        TrackJson t = new TrackJson();
        t.title = title;
        t.artist = publisher;
        if (imageUrl != null) {
            t.albumArtRef = new ArrayList<>();
            ImageRefJson img = new ImageRefJson();
            img.url = imageUrl;
            t.albumArtRef.add(img);
        }
        return t;
    }
}
