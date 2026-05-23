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

import com.carthigan.playmusic.api.models.TrackJson;
import com.carthigan.playmusic.data.LocalMusicHelper;

import java.util.ArrayList;
import java.util.List;

public class TopChartsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_releases, container, false); // Reuse the simple recylerview layout
        RecyclerView recyclerView = view.findViewById(R.id.rv_new_releases);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        ClusterAdapter adapter = new ClusterAdapter(track -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).playTrackFromLibrary(track);
            }
        });

        recyclerView.setAdapter(adapter);
        adapter.setClusters(getTopChartClusters());

        return view;
    }

    private List<ClusterAdapter.Cluster> getTopChartClusters() {
        List<ClusterAdapter.Cluster> clusters = new ArrayList<>();
        
        List<TrackJson> allSongs = new ArrayList<>();
        if (getActivity() instanceof MainActivity) {
            allSongs = ((MainActivity) getActivity()).getLocalMusicHelper().getAllSongs();
        }
        
        // Mock top charts using local songs if available
        List<TrackJson> topSongs = new ArrayList<>();
        List<TrackJson> topAlbums = new ArrayList<>();
        
        if (allSongs != null && !allSongs.isEmpty()) {
            topSongs.addAll(allSongs.subList(0, Math.min(10, allSongs.size())));
            
            // Randomly pick some as "Top Albums"
            java.util.Collections.shuffle(allSongs);
            topAlbums.addAll(allSongs.subList(0, Math.min(6, allSongs.size())));
        } else {
            // Absolute fallback mock
            topSongs.add(createMock("1 step forward, 3 steps back", "Olivia Rodrigo", null));
            topSongs.add(createMock("Shape of You", "Ed Sheeran", "https://upload.wikimedia.org/wikipedia/en/b/b4/Shape_Of_You_%28Official_Single_Cover%29.png"));
            
            topAlbums.add(createMock("Havana", "Camila Cabello", "https://upload.wikimedia.org/wikipedia/en/9/98/Havana_%28featuring_Young_Thug%29_%28Official_Single_Cover%29.png"));
            topAlbums.add(createMock("24/5", "Mimi Webb", null));
        }

        clusters.add(new ClusterAdapter.Cluster("Top Songs", "The most played tracks", true, topSongs));
        clusters.add(new ClusterAdapter.Cluster("Top Albums", "Trending albums this week", true, topAlbums));

        return clusters;
    }

    private TrackJson createMock(String title, String subtitle, String imageUrl) {
        TrackJson t = new TrackJson();
        t.title = title;
        t.artist = subtitle;
        if (imageUrl != null) {
            t.albumArtRef = new ArrayList<>();
            com.carthigan.playmusic.api.models.ImageRefJson img = new com.carthigan.playmusic.api.models.ImageRefJson();
            img.url = imageUrl;
            t.albumArtRef.add(img);
        }
        return t;
    }
}
