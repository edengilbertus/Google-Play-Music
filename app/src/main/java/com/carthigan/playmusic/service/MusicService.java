package com.carthigan.playmusic.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.media.app.NotificationCompat.MediaStyle;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.carthigan.playmusic.MainActivity;
import com.carthigan.playmusic.R;

public class MusicService extends Service {

    public static final String ACTION_UPDATE = "com.monstertechno.music.UPDATE";
    public static final String ACTION_PLAY_PAUSE = "com.monstertechno.music.PLAY_PAUSE";
    public static final String ACTION_NEXT = "com.monstertechno.music.NEXT";
    public static final String ACTION_PREV = "com.monstertechno.music.PREV";

    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_ARTIST = "artist";
    public static final String EXTRA_ART_URL = "art_url";
    public static final String EXTRA_IS_PLAYING = "is_playing";

    private static final String CHANNEL_ID = "gpm_playback_channel";
    private static final int NOTIFICATION_ID = 1;

    private MediaSessionCompat mediaSession;
    private String currentTitle = "Unknown";
    private String currentArtist = "Unknown";
    private boolean isPlaying = false;
    private Bitmap currentArt = null;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        mediaSession = new MediaSessionCompat(this, "MusicService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_UPDATE.equals(action)) {
                currentTitle = intent.getStringExtra(EXTRA_TITLE);
                currentArtist = intent.getStringExtra(EXTRA_ARTIST);
                isPlaying = intent.getBooleanExtra(EXTRA_IS_PLAYING, false);
                String artUrl = intent.getStringExtra(EXTRA_ART_URL);

                currentArt = null;
                showNotification();

                if (artUrl != null && !artUrl.isEmpty()) {
                    Glide.with(this)
                            .asBitmap()
                            .load(artUrl)
                            .into(new CustomTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                    currentArt = resource;
                                    showNotification();
                                }
                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {}
                                
                                @Override
                                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                    currentArt = null;
                                    showNotification();
                                }
                            });
                }
            } else if (ACTION_PLAY_PAUSE.equals(action) || ACTION_NEXT.equals(action) || ACTION_PREV.equals(action)) {
                // Relay action to MainActivity
                Intent broadcastIntent = new Intent(action);
                sendBroadcast(broadcastIntent);
            }
        }
        return START_NOT_STICKY;
    }

    private void showNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        PendingIntent playPauseIntent = getPendingIntent(ACTION_PLAY_PAUSE);
        PendingIntent nextIntent = getPendingIntent(ACTION_NEXT);
        PendingIntent prevIntent = getPendingIntent(ACTION_PREV);

        int playPauseIcon = isPlaying ? R.drawable.ic_pause : R.drawable.ic_play_authentic;
        String playPauseTitle = isPlaying ? "Pause" : "Play";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_authentic)
                .setContentTitle(currentTitle)
                .setContentText(currentArtist)
                .setLargeIcon(currentArt)
                .setContentIntent(contentIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(R.drawable.ic_skip_previous, "Previous", prevIntent)
                .addAction(playPauseIcon, playPauseTitle, playPauseIntent)
                .addAction(R.drawable.ic_skip_next, "Next", nextIntent)
                .setStyle(new MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2)
                        .setMediaSession(mediaSession.getSessionToken()))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOnlyAlertOnce(true);

        Notification notification = builder.build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }
    }

    private PendingIntent getPendingIntent(String action) {
        Intent intent = new Intent(this, MusicService.class);
        intent.setAction(action);
        return PendingIntent.getService(this, action.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Playback",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Shows music playback controls");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaSession != null) {
            mediaSession.release();
        }
    }
}
