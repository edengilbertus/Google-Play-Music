package com.carthigan.playmusic.api.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TrackJson {
    @SerializedName("album")
    public String album;

    @SerializedName("albumArtRef")
    public List<ImageRefJson> albumArtRef;

    @SerializedName("albumArtist")
    public String albumArtist;

    @SerializedName("albumId")
    public String albumId;

    @SerializedName("artist")
    public String artist;

    @SerializedName("artistArtRef")
    public List<ImageRefJson> artistArtRef;

    @SerializedName("title")
    public String title;

    @SerializedName("nid")
    public String nid;

    @SerializedName("durationMillis")
    public long durationMillis;

    @SerializedName("playCount")
    public int playCount;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TrackJson trackJson = (TrackJson) obj;
        if (nid != null) {
            return nid.equals(trackJson.nid);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return nid != null ? nid.hashCode() : super.hashCode();
    }
}
