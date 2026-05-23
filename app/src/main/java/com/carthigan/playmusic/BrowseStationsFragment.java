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

public class BrowseStationsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_browse_stations, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.rv_browse_stations);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // We will reuse the TrackAdapter for now, but in reality, radio stations had a different card UI
        // known as CuratedRadioPlayCard. We'll use the existing adapter to display mock stations.
        TrackAdapter adapter = new TrackAdapter(track -> {
            // Clicking a station would normally start playing a radio mix
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).playTrackFromLibrary(track);
            }
        }, track -> {
            // Options for station
        });

        recyclerView.setAdapter(adapter);
        adapter.setTracks(getMockStations());

        return view;
    }

    private List<TrackJson> getMockStations() {
        List<TrackJson> list = new ArrayList<>();
        
        String[] stations = {"I'm Feeling Lucky", "Today's Biggest Hits", "Classic Rock Radio", "Focus & Study", "Workout Mix"};
        for (String stationName : stations) {
            TrackJson t = new TrackJson();
            t.title = stationName;
            t.artist = "Radio Station";
            list.add(t);
        }
        return list;
    }
}