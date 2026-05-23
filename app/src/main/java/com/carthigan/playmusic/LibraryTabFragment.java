package com.carthigan.playmusic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.carthigan.playmusic.api.models.ImageRefJson;
import com.carthigan.playmusic.api.models.TrackJson;
import com.carthigan.playmusic.data.LocalMusicHelper;

import java.util.ArrayList;
import java.util.List;

public class LibraryTabFragment extends Fragment {

    private static final String ARG_TAB_NAME = "tab_name";

    public static LibraryTabFragment newInstance(String tabName) {
        LibraryTabFragment fragment = new LibraryTabFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TAB_NAME, tabName);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library_tab, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.rv_library_tab);
        View shuffleAllView = view.findViewById(R.id.ll_shuffle_all);
        
        String tabName = getArguments() != null ? getArguments().getString(ARG_TAB_NAME) : "";
        List<TrackJson> items;
        
        if (getActivity() instanceof MainActivity) {
            LocalMusicHelper helper = ((MainActivity) getActivity()).getLocalMusicHelper();
            items = processTracksForTab(helper, tabName);
        } else {
            items = getMockDataForTab(tabName);
        }

        if ("SONGS".equals(tabName)) {
            shuffleAllView.setVisibility(View.VISIBLE);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            TrackAdapter adapter = new TrackAdapter(track -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).playTrackFromLibrary(track);
                }
            }, track -> {
                // Options
            });
            recyclerView.setAdapter(adapter);
            adapter.setTracks(items);
            
            shuffleAllView.setOnClickListener(v -> {
                 if (getActivity() instanceof MainActivity) {
                     // TODO: Add proper shuffle all intent to MainActivity
                     Toast.makeText(getContext(), "Shuffle All Clicked", Toast.LENGTH_SHORT).show();
                 }
            });
        } else {
            shuffleAllView.setVisibility(View.GONE);
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            LibraryCardAdapter adapter = new LibraryCardAdapter(track -> {
                if (getActivity() instanceof MainActivity) {
                    if ("PLAYLISTS".equals(tabName)) {
                        if ("Last added".equals(track.title) || "Favorites".equals(track.title)) {
                            Toast.makeText(getContext(), "Auto-playlist clicked", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    
                    long id = -1;
                    try {
                        id = Long.parseLong(track.nid);
                    } catch (Exception ignored) {}
                    
                    String artUri = (track.albumArtRef != null && !track.albumArtRef.isEmpty()) ? track.albumArtRef.get(0).url : null;
                    
                    String type = "ALBUM";
                    if ("ARTISTS".equals(tabName)) type = "ARTIST";
                    else if ("PLAYLISTS".equals(tabName)) type = "PLAYLIST";
                    else if ("GENRES".equals(tabName)) type = "GENRE";
                    
                    ((MainActivity) getActivity()).openLibraryDetail(type, id, track.title, track.artist, artUri);
                }
            });
            recyclerView.setAdapter(adapter);
            adapter.setItems(items);
        }

        return view;
    }

    private List<TrackJson> processTracksForTab(LocalMusicHelper helper, String tabName) {
        if ("PLAYLISTS".equals(tabName)) {
            List<TrackJson> processed = new ArrayList<>();
            processed.add(createMock("Last added", "Auto-Playlists", null));
            processed.addAll(helper.getPlaylists());
            return processed;
        } else if ("ARTISTS".equals(tabName)) {
            return helper.getArtists();
        } else if ("ALBUMS".equals(tabName)) {
            return helper.getAlbums();
        } else if ("GENRES".equals(tabName)) {
            return helper.getGenres();
        } else {
            // SONGS
            return helper.getAllSongs();
        }
    }

    private String getFirstImage(TrackJson track) {
        if (track.albumArtRef != null && !track.albumArtRef.isEmpty() && track.albumArtRef.get(0).url != null) {
            return track.albumArtRef.get(0).url;
        }
        return null;
    }

    private List<TrackJson> getMockDataForTab(String tabName) {
        List<TrackJson> list = new ArrayList<>();
        
        if ("PLAYLISTS".equals(tabName)) {
            list.add(createMock("Last added", "Auto-Playlists", "https://i.scdn.co/image/ab67616d0000b27376c666fb271b3e83b482cb8d"));
            list.add(createMock("Favorites", "Auto-Playlists", null));
        } else if ("ARTISTS".equals(tabName)) {
            list.add(createMock("Above & Beyond", "", "https://upload.wikimedia.org/wikipedia/en/9/98/Crawl Outta Love_%28featuring_Young_Thug%29_%28Official_Single_Cover%29.png"));
            list.add(createMock("Andrea Bocelli", "", "https://i.scdn.co/image/ab67616d0000b27376c666fb271b3e83b482cb8d"));
            list.add(createMock("Bleachers", "", null));
            list.add(createMock("Boygenius", "", null));
        } else if ("ALBUMS".equals(tabName)) {
            list.add(createMock("1 step forward, 3 steps back", "Olivia Rodrigo", null));
            list.add(createMock("99 Luftballons", "Nena", "https://upload.wikimedia.org/wikipedia/en/9/98/Crawl Outta Love_%28featuring_Young_Thug%29_%28Official_Single_Cover%29.png"));
            list.add(createMock("24/5", "Mimi Webb", null));
            list.add(createMock("ADHD Relief Music", "Greenred Productions", "https://i.scdn.co/image/ab67616d0000b27376c666fb271b3e83b482cb8d"));
        } else if ("GENRES".equals(tabName)) {
            list.add(createMock("Alternative", "", "https://upload.wikimedia.org/wikipedia/en/9/98/Crawl Outta Love_%28featuring_Young_Thug%29_%28Official_Single_Cover%29.png"));
            list.add(createMock("Alternative/Rock", "", null));
            list.add(createMock("Classical", "", "https://i.scdn.co/image/ab67616d0000b27376c666fb271b3e83b482cb8d"));
            list.add(createMock("Country", "", null));
        } else {
            // SONGS and fallback
            list.add(createMock("Not Enough", "Dabin, Stephanie Poetri", null));
            list.add(createMock("Fractures", "Illenium", "https://i.scdn.co/image/ab67616d0000b27376c666fb271b3e83b482cb8d"));
            list.add(createMock("Crawl Outta Love", "Illenium", "https://upload.wikimedia.org/wikipedia/en/9/98/Crawl Outta Love_%28featuring_Young_Thug%29_%28Official_Single_Cover%29.png"));
        }
        
        return list;
    }
    
    private TrackJson createMock(String title, String subtitle, String imageUrl) {
        TrackJson t = new TrackJson();
        t.title = title;
        t.artist = subtitle;
        if (imageUrl != null) {
            t.albumArtRef = new ArrayList<>();
            ImageRefJson img = new ImageRefJson();
            img.url = imageUrl;
            t.albumArtRef.add(img);
        }
        return t;
    }
}
