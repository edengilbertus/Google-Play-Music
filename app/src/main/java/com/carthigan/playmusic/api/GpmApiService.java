package com.carthigan.playmusic.api;

import com.carthigan.playmusic.api.models.TrackJson;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Header;

public interface GpmApiService {

    // Base URL: https://mclients.googleapis.com/sj/v2.5/

    @GET("fetchtrack")
    Call<TrackJson> fetchTrack(
        @Header("Authorization") String authorization,
        @Query("nid") String trackId,
        @Query("alt") String alt
    );

    @GET("browse/topchart")
    Call<Object> getTopCharts(
        @Header("Authorization") String authorization,
        @Query("alt") String alt
    );
    
    // Note: Stream authentication is typically at: https://mclients.googleapis.com/music
    // using GET /mplay?songid=... 
}
