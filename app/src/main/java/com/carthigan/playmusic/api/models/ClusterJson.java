package com.carthigan.playmusic.api.models;

import java.util.List;

public class ClusterJson {
    public String title;
    public String subtitle;
    public List<TrackJson> items;

    public ClusterJson(String title, String subtitle, List<TrackJson> items) {
        this.title = title;
        this.subtitle = subtitle;
        this.items = items;
    }
}
