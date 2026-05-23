package com.carthigan.playmusic.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GpmClient {
    private static final String BASE_URL = "https://mclients.googleapis.com/sj/v2.5/";
    private static Retrofit retrofit = null;

    public static GpmApiService getService() {
        if (retrofit == null) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            
            OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();

            retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        }
        return retrofit.create(GpmApiService.class);
    }
}
