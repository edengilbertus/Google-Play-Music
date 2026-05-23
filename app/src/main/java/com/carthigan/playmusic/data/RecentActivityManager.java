package com.carthigan.playmusic.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.carthigan.playmusic.api.models.TrackJson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class RecentActivityManager {

    private static final String PREF_NAME = "RecentActivityPrefs";
    private static final String KEY_RECENT_TRACKS = "recent_tracks";
    private static final int MAX_RECENT_ITEMS = 50;

    private final SharedPreferences prefs;
    private final Gson gson;

    public RecentActivityManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void addTrackToRecents(TrackJson track) {
        if (track == null || track.nid == null) return;

        List<TrackJson> recents = getRecentTracks();

        // Remove track if it already exists to move it to the top
        for (int i = 0; i < recents.size(); i++) {
            if (track.nid.equals(recents.get(i).nid)) {
                recents.remove(i);
                break;
            }
        }

        // Add to the top of the list
        recents.add(0, track);

        // Enforce max size
        if (recents.size() > MAX_RECENT_ITEMS) {
            recents = recents.subList(0, MAX_RECENT_ITEMS);
        }

        saveRecents(recents);
    }

    public List<TrackJson> getRecentTracks() {
        String json = prefs.getString(KEY_RECENT_TRACKS, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<TrackJson>>() {}.getType();
        List<TrackJson> list = gson.fromJson(json, type);
        return list != null ? list : new ArrayList<>();
    }

    public void clearHistory() {
        prefs.edit().remove(KEY_RECENT_TRACKS).apply();
    }

    private void saveRecents(List<TrackJson> recents) {
        String json = gson.toJson(recents);
        prefs.edit().putString(KEY_RECENT_TRACKS, json).apply();
    }
}
