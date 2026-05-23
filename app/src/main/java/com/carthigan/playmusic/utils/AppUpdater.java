package com.carthigan.playmusic.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AlertDialog;

import com.carthigan.playmusic.BuildConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AppUpdater {

    private static final String GITHUB_API_URL = "https://api.github.com/repos/edengilbertus/Google-Play-Music/releases/latest";

    public static void checkForUpdates(Activity activity) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(GITHUB_API_URL)
                .header("User-Agent", "PlayMusic-App")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Silently fail if no network
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        JSONObject jsonObject = new JSONObject(json);
                        
                        String tagName = jsonObject.getString("tag_name").replace("v", "");
                        String currentVersion = BuildConfig.VERSION_NAME.replace("v", "");

                        if (isNewerVersion(currentVersion, tagName)) {
                            // Find the APK download URL, fallback to the release page html
                            String downloadUrl = jsonObject.getString("html_url"); 
                            JSONArray assets = jsonObject.getJSONArray("assets");
                            for (int i = 0; i < assets.length(); i++) {
                                JSONObject asset = assets.getJSONObject(i);
                                if (asset.getString("name").endsWith(".apk")) {
                                    downloadUrl = asset.getString("browser_download_url");
                                    break;
                                }
                            }
                            
                            final String finalDownloadUrl = downloadUrl;
                            final String releaseNotes = jsonObject.optString("body", "Bug fixes and performance improvements.");

                            // Show UI on main thread
                            new Handler(Looper.getMainLooper()).post(() -> {
                                if (!activity.isFinishing()) {
                                    showUpdateDialog(activity, tagName, releaseNotes, finalDownloadUrl);
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private static boolean isNewerVersion(String current, String latest) {
        try {
            String[] currParts = current.split("\\.");
            String[] latestParts = latest.split("\\.");
            int length = Math.max(currParts.length, latestParts.length);
            
            for (int i = 0; i < length; i++) {
                int currPart = i < currParts.length ? Integer.parseInt(currParts[i]) : 0;
                int latestPart = i < latestParts.length ? Integer.parseInt(latestParts[i]) : 0;
                if (currPart < latestPart) return true;
                if (currPart > latestPart) return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static void showUpdateDialog(Activity activity, String version, String notes, String downloadUrl) {
        new AlertDialog.Builder(activity)
                .setTitle("Update Available (v" + version + ")")
                .setMessage("A new version of Play Music is available!\n\n" + notes)
                .setPositiveButton("Download", (dialog, which) -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl));
                    activity.startActivity(intent);
                })
                .setNegativeButton("Later", null)
                .show();
    }
}
