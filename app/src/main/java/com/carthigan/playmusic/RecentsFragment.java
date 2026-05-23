package com.carthigan.playmusic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.carthigan.playmusic.api.models.TrackJson;
import com.carthigan.playmusic.data.RecentActivityManager;

import java.util.List;

public class RecentsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library_tab, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.rv_library_tab);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        
        View shuffleAllView = view.findViewById(R.id.ll_shuffle_all);
        shuffleAllView.setVisibility(View.GONE);

        RecentActivityManager recentManager = new RecentActivityManager(requireContext());
        List<TrackJson> recentTracks = recentManager.getRecentTracks();

        if (recentTracks.isEmpty()) {
            // Show empty state if possible
            TextView emptyText = new TextView(getContext());
            emptyText.setText("No recent activity.");
            emptyText.setPadding(32, 32, 32, 32);
            emptyText.setTextColor(getResources().getColor(R.color.gpm_text_secondary));
            emptyText.setTextSize(16);
            
            ViewGroup parent = (ViewGroup) recyclerView.getParent();
            parent.addView(emptyText);
            recyclerView.setVisibility(View.GONE);
        } else {
            LibraryCardAdapter adapter = new LibraryCardAdapter(track -> {
                if (getActivity() instanceof MainActivity) {
                    int index = recentTracks.indexOf(track);
                    ((MainActivity) getActivity()).playQueue(recentTracks, index != -1 ? index : 0);
                }
            });

            recyclerView.setAdapter(adapter);
            adapter.setItems(recentTracks);
        }

        return view;
    }
}
