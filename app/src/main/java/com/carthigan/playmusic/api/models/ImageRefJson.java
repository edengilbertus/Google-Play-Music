package com.carthigan.playmusic.api.models;

import com.google.gson.annotations.SerializedName;

public class ImageRefJson {
    @SerializedName("url")
    public String url;

    @SerializedName("aspectRatio")
    public String aspectRatio;

    @SerializedName("autogen")
    public boolean autogen;
}
