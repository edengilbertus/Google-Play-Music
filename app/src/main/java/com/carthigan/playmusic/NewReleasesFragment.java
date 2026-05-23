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

import java.util.ArrayList;
import java.util.List;

public class NewReleasesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_releases, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.rv_new_releases);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        ClusterAdapter adapter = new ClusterAdapter(track -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).playTrackFromLibrary(track);
            }
        });

        recyclerView.setAdapter(adapter);
        adapter.setClusters(getMockClusters());

        return view;
    }

    private List<ClusterAdapter.Cluster> getMockClusters() {
        List<ClusterAdapter.Cluster> clusters = new ArrayList<>();
        
        List<TrackJson> recommended = new ArrayList<>();
        recommended.add(createMock("1 step forward, 3 steps back", "Olivia Rodrigo", null));
        recommended.add(createMock("ADHD Relief Music", "Greenred Productions", "https://upload.wikimedia.org/wikipedia/en/b/b4/Shape_Of_You_%28Official_Single_Cover%29.png"));
        recommended.add(createMock("24/5", "Mimi Webb", null));
        recommended.add(createMock("Shape of You", "Ed Sheeran", "https://upload.wikimedia.org/wikipedia/en/b/b4/Shape_Of_You_%28Official_Single_Cover%29.png"));
        
        clusters.add(new ClusterAdapter.Cluster(getString(R.string.recommended_new_release_title), null, true, recommended));

        List<TrackJson> all = new ArrayList<>();
        all.add(createMock("Havana", "Camila Cabello", "https://upload.wikimedia.org/wikipedia/en/9/98/Havana_%28featuring_Young_Thug%29_%28Official_Single_Cover%29.png"));
        all.add(createMock("99 Luftballons", "Nena", "https://upload.wikimedia.org/wikipedia/en/9/98/Havana_%28featuring_Young_Thug%29_%28Official_Single_Cover%29.png"));
        all.add(createMock("Not Enough", "Dabin, Stephanie Poetri", null));
        all.add(createMock("Bleachers", "", null));
        
        clusters.add(new ClusterAdapter.Cluster(getString(R.string.all_new_release_title), null, true, all));

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
