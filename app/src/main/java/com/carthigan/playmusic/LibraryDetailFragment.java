package com.carthigan.playmusic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.carthigan.playmusic.api.models.TrackJson;
import com.carthigan.playmusic.data.LocalMusicHelper;

import java.util.List;

public class LibraryDetailFragment extends Fragment {

    private static final String ARG_TYPE = "type";
    private static final String ARG_ID = "id";
    private static final String ARG_TITLE = "title";
    private static final String ARG_SUBTITLE = "subtitle";
    private static final String ARG_ART_URI = "art_uri";

    public static LibraryDetailFragment newInstance(String type, long id, String title, String subtitle, String artUri) {
        LibraryDetailFragment fragment = new LibraryDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        args.putLong(ARG_ID, id);
        args.putString(ARG_TITLE, title);
        args.putString(ARG_SUBTITLE, subtitle);
        args.putString(ARG_ART_URI, artUri);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library_detail, container, false);

        Toolbar toolbar = view.findViewById(R.id.detail_toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        String type = getArguments() != null ? getArguments().getString(ARG_TYPE) : "";
        long id = getArguments() != null ? getArguments().getLong(ARG_ID) : -1;
        String title = getArguments() != null ? getArguments().getString(ARG_TITLE) : "";
        String subtitle = getArguments() != null ? getArguments().getString(ARG_SUBTITLE) : "";
        String artUri = getArguments() != null ? getArguments().getString(ARG_ART_URI) : "";

        TextView tvTitle = view.findViewById(R.id.detail_title);
        TextView tvSubtitle = view.findViewById(R.id.detail_subtitle);
        ImageView ivHeader = view.findViewById(R.id.detail_header_image);

        tvTitle.setText(title);
        if (subtitle != null && !subtitle.isEmpty()) {
            tvSubtitle.setText(subtitle);
            tvSubtitle.setVisibility(View.VISIBLE);
        } else {
            tvSubtitle.setVisibility(View.GONE);
        }

        if (artUri != null && !artUri.isEmpty()) {
            Glide.with(this)
                    .load(artUri)
                    .centerCrop()
                    .placeholder(R.drawable.placeholder_music)
                    .error(R.drawable.placeholder_music)
                    .into(ivHeader);
        }

        RecyclerView recyclerView = view.findViewById(R.id.rv_detail_tracks);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        List<TrackJson> tracks = null;
        if (getActivity() instanceof MainActivity) {
            LocalMusicHelper helper = ((MainActivity) getActivity()).getLocalMusicHelper();
            if ("ALBUM".equals(type)) {
                tracks = helper.getSongsForAlbum(id);
            } else if ("ARTIST".equals(type)) {
                tracks = helper.getSongsForArtist(id);
            } else if ("PLAYLIST".equals(type)) {
                tracks = helper.getSongsForPlaylist(id);
            } else if ("GENRE".equals(type)) {
                tracks = helper.getSongsForGenre(id);
            }
        }

        if (tracks != null) {
            final List<TrackJson> finalTracks = tracks;
            TrackAdapter adapter = new TrackAdapter(track -> {
                if (getActivity() instanceof MainActivity) {
                    int index = finalTracks.indexOf(track);
                    ((MainActivity) getActivity()).playQueue(finalTracks, index != -1 ? index : 0);
                }
            }, track -> {
                // Handle options
            });
            recyclerView.setAdapter(adapter);
            adapter.setTracks(tracks);
            
            FloatingActionButton fab = view.findViewById(R.id.fab_play_all);
            fab.setOnClickListener(v -> {
                if (finalTracks != null && !finalTracks.isEmpty()) {
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).playQueue(finalTracks, 0);
                    }
                }
            });
        }

        return view;
    }
}
