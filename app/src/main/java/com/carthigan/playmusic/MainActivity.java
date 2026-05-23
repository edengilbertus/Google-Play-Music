package com.carthigan.playmusic;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
import com.carthigan.playmusic.api.models.ImageRefJson;
import com.carthigan.playmusic.api.models.ClusterJson;
import com.carthigan.playmusic.api.models.TrackJson;
import com.carthigan.playmusic.data.LocalMusicHelper;
import com.carthigan.playmusic.data.RecentActivityManager;
import com.carthigan.playmusic.service.MusicService;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.annotation.SuppressLint;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private SlidingUpPanelLayout mLayout;
    private ImageButton play, pause, play_main, pause_main, like, dislike, next, prev, btn_shuffle, btn_repeat;
    private TextView songs_title, songs_artist_name, mini_songs_title, mini_songs_artist_name;
    private TextView startTime, endTime;
    private SeekBar seekBar;
    private ImageView songs_cover_one;
    private ViewPager2 playerViewPager;
    private PlayerPagerAdapter playerPagerAdapter;
    private RecyclerView rvMainContent;
    private TrackAdapter trackAdapter;
    private ListenNowClusterAdapter clusterAdapter;
    private Toolbar toolbar;
    
    private RecentActivityManager recentActivityManager;

    private List<TrackJson> allTracksList = new ArrayList<>();
    private List<TrackJson> tracksList = new ArrayList<>();
    private List<TrackJson> playbackQueue = new ArrayList<>();
    private int currentTrackIndex = -1;
    private TrackJson currentPlayingTrack = null;
    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener;
    private Handler handler = new Handler();
    private boolean isLiked = false;
    private boolean isDisliked = false;
    private boolean isShuffle = false;
    private int repeatMode = 0; // 0 = off, 1 = repeat all, 2 = repeat one

    private Toolbar mainToolbar;
    private Toolbar settingsToolbar;
    private View searchPlate;
    private android.widget.EditText searchBox;
    private TabLayout libraryTabs;
    private ViewPager2 libraryViewPager;
    
    private LocalMusicHelper localMusicHelper;

    private BroadcastReceiver musicControlReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                switch (intent.getAction()) {
                    case MusicService.ACTION_PLAY_PAUSE:
                        togglePlayPause();
                        break;
                    case MusicService.ACTION_NEXT:
                        playNextTrack();
                        break;
                    case MusicService.ACTION_PREV:
                        playPrevTrack();
                        break;
                }
            }
        }
    };

    public LocalMusicHelper getLocalMusicHelper() {
        if (localMusicHelper == null) {
            localMusicHelper = new LocalMusicHelper(this);
        }
        return localMusicHelper;
    }

    public void openLibraryDetail(String type, long id, String title, String subtitle, String artUri) {
        if (mainToolbar != null) mainToolbar.setVisibility(View.GONE);
        if (searchPlate != null) searchPlate.setVisibility(View.GONE);
        if (settingsToolbar != null) settingsToolbar.setVisibility(View.GONE);
        if (libraryTabs != null) libraryTabs.setVisibility(View.GONE);
        
        rvMainContent.setVisibility(View.GONE);
        if (libraryViewPager != null) libraryViewPager.setVisibility(View.GONE);
        findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);
        
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, LibraryDetailFragment.newInstance(type, id, title, subtitle, artUri))
                .addToBackStack(null)
                .commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        recentActivityManager = new RecentActivityManager(this);

        audioManager = (AudioManager) getSystemService(android.content.Context.AUDIO_SERVICE);
        audioFocusChangeListener = focusChange -> {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) togglePlayPause();
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) togglePlayPause();
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                if (mediaPlayer != null) mediaPlayer.setVolume(0.2f, 0.2f);
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                if (mediaPlayer != null) {
                    mediaPlayer.setVolume(1.0f, 1.0f);
                    if (!mediaPlayer.isPlaying()) togglePlayPause();
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(MusicService.ACTION_PLAY_PAUSE);
        filter.addAction(MusicService.ACTION_NEXT);
        filter.addAction(MusicService.ACTION_PREV);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(musicControlReceiver, filter, RECEIVER_EXPORTED);
        } else {
            registerReceiver(musicControlReceiver, filter);
        }

        // Setup Custom UI Elements
        mainToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);
        
        searchPlate = findViewById(R.id.search_plate);
        searchBox = findViewById(R.id.search_box_text_input);
        settingsToolbar = findViewById(R.id.settings_toolbar);
        
        // Setup Navigation Drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_listen_now);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, mainToolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        ImageButton searchBackButton = findViewById(R.id.search_back_button);
        if (searchBackButton != null) {
            searchBackButton.setOnClickListener(v -> closeSearch());
        }
        
        ImageButton actionButton = findViewById(R.id.action_button);
        if (actionButton != null) {
            actionButton.setOnClickListener(v -> {
                if (searchBox != null) searchBox.setText("");
                filterTracks("");
            });
        }
        
        settingsToolbar.setNavigationOnClickListener(v -> {
            if (drawerLayout != null) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // Wire up custom search box
        searchBox.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTracks(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // Init RecyclerView
        rvMainContent = findViewById(R.id.rv_main_content);
        rvMainContent.setLayoutManager(new LinearLayoutManager(this));

        trackAdapter = new TrackAdapter(track -> {
            currentTrackIndex = tracksList.indexOf(track);
            playTrack(currentTrackIndex);
            if (mLayout != null && mLayout.getPanelState() != SlidingUpPanelLayout.PanelState.EXPANDED) {
                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            }
        }, track -> {
            showTrackOptionsBottomSheet(track);
        });

        clusterAdapter = new ListenNowClusterAdapter((track, fullList) -> {
            int index = fullList.indexOf(track);
            playQueue(fullList, index != -1 ? index : 0);
        });

        rvMainContent.setAdapter(clusterAdapter);

        // Init Library ViewPager & Tabs
        libraryTabs = findViewById(R.id.library_tabs);
        libraryViewPager = findViewById(R.id.library_viewpager);
        
        LibraryPagerAdapter pagerAdapter = new LibraryPagerAdapter(this);
        libraryViewPager.setAdapter(pagerAdapter);
        
        new TabLayoutMediator(libraryTabs, libraryViewPager, (tab, position) -> {
            tab.setText(pagerAdapter.getTabTitle(position));
        }).attach();

        // Init Panel Controls
        mLayout = findViewById(R.id.activity_main);
        songs_title = findViewById(R.id.songs_title);
        songs_artist_name = findViewById(R.id.songs_artist_name);
        mini_songs_title = findViewById(R.id.mini_songs_title);
        mini_songs_artist_name = findViewById(R.id.mini_songs_artist_name);
        songs_cover_one = findViewById(R.id.songs_cover_one);

        playerViewPager = findViewById(R.id.player_viewpager);
        playerPagerAdapter = new PlayerPagerAdapter(track -> {
            int index = tracksList.indexOf(track);
            if (index != -1) {
                playTrack(index);
            }
        });
        if (playerViewPager != null) {
            playerViewPager.setAdapter(playerPagerAdapter);
        }

        startTime = findViewById(R.id.StartTime);
        endTime = findViewById(R.id.endTime);
        seekBar = findViewById(R.id.seekBar3);

        like = findViewById(R.id.imageButton2);
        dislike = findViewById(R.id.button);

        play = findViewById(R.id.play_button);
        pause = findViewById(R.id.pause_button);
        play_main = findViewById(R.id.play_button_main);
        pause_main = findViewById(R.id.pause_button_main);
        
        prev = findViewById(R.id.imageButton);
        next = findViewById(R.id.imageButton_next);
        
        btn_shuffle = findViewById(R.id.btn_shuffle);
        btn_repeat = findViewById(R.id.btn_repeat);

        setupClickListeners();

        // Request Permissions & Load Music
        checkPermissionsAndLoadMusic();
    }

    public void refreshLibrary() {
        checkPermissionsAndLoadMusic();
    }

    private void checkPermissionsAndLoadMusic() {
        List<String> permissionsToRequest = new ArrayList<>();
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_AUDIO);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.READ_CONTACTS);
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]), 1);
        } else {
            loadLocalMusic();
            loadUserProfile();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            boolean audioGranted = false;
            boolean contactsGranted = false;
            
            for (int i = 0; i < permissions.length; i++) {
                if (Manifest.permission.READ_MEDIA_AUDIO.equals(permissions[i]) || Manifest.permission.READ_EXTERNAL_STORAGE.equals(permissions[i])) {
                    audioGranted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
                }
                if (Manifest.permission.READ_CONTACTS.equals(permissions[i])) {
                    contactsGranted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
                }
            }
            
            if (audioGranted) {
                loadLocalMusic();
            } else {
                Toast.makeText(this, "Permission denied, loading mock data", Toast.LENGTH_SHORT).show();
                loadMockData();
            }
            
            if (contactsGranted) {
                loadUserProfile();
            }
        }
    }

    private void loadLocalMusic() {
        new Thread(() -> {
            List<TrackJson> localTracks = getLocalMusicHelper().getAllSongs();
            
            runOnUiThread(() -> {
                if (localTracks.isEmpty()) {
                    loadMockData();
                } else {
                    allTracksList = new ArrayList<>(localTracks);
                    tracksList = new ArrayList<>(localTracks);
                    trackAdapter.setTracks(tracksList);
                }
            });
        }).start();
    }

    @SuppressLint("Range")
    private void loadUserProfile() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) return;

        NavigationView navigationView = findViewById(R.id.nav_view);
        if (navigationView == null || navigationView.getHeaderCount() == 0) return;

        View headerView = navigationView.getHeaderView(0);
        TextView tvName = headerView.findViewById(R.id.nav_header_profile_name);
        TextView tvEmail = headerView.findViewById(R.id.nav_header_profile_email);
        ImageView ivAvatar = headerView.findViewById(R.id.nav_header_profile_image);

        if (tvName == null || ivAvatar == null) return;

        String displayName = "Local User";
        String photoUri = null;

        try {
            Cursor cursor = getContentResolver().query(
                    ContactsContract.Profile.CONTENT_URI,
                    null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Profile.DISPLAY_NAME_PRIMARY));
                photoUri = cursor.getString(cursor.getColumnIndex(ContactsContract.Profile.PHOTO_URI));
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        tvName.setText(displayName != null ? displayName : "Local User");
        
        // Android device profiles usually don't expose email directly through ContactsContract.Profile 
        // without complex GET_ACCOUNTS logic, so we leave the generic or device ID.
        if (tvEmail != null) {
            tvEmail.setText("Device Profile");
        }

        if (photoUri != null) {
            Glide.with(this)
                    .load(Uri.parse(photoUri))
                    .circleCrop()
                    .placeholder(R.drawable.ic_default_avatar)
                    .error(R.drawable.ic_default_avatar)
                    .into(ivAvatar);
        } else {
            ivAvatar.setImageResource(R.drawable.ic_default_avatar);
        }
    }

    private void loadMockData() {
        allTracksList = new ArrayList<>();
        tracksList = new ArrayList<>();

        TrackJson t1 = new TrackJson();
        t1.title = "Crawl Outta Love";
        t1.artist = "Illenium";
        t1.albumArtRef = new ArrayList<>();
        ImageRefJson i1 = new ImageRefJson();
        i1.url = "https://upload.wikimedia.org/wikipedia/en/9/98/Crawl Outta Love_%28featuring_Young_Thug%29_%28Official_Single_Cover%29.png";
        t1.albumArtRef.add(i1);

        TrackJson t2 = new TrackJson();
        t2.title = "Fractures";
        t2.artist = "Illenium";
        t2.albumArtRef = new ArrayList<>();
        ImageRefJson i2 = new ImageRefJson();
        i2.url = "https://i.scdn.co/image/ab67616d0000b27376c666fb271b3e83b482cb8d";
        t2.albumArtRef.add(i2);

        allTracksList.add(t1);
        allTracksList.add(t2);
        tracksList.addAll(allTracksList);

        trackAdapter.setTracks(tracksList);
    }

    private Runnable updateSeekBarRunnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                startTime.setText(formatTime(mediaPlayer.getCurrentPosition()));
                handler.postDelayed(this, 1000);
            }
        }
    };

    private void playTrack(int index) {
        if (index < 0 || index >= tracksList.size()) return;
        
        currentTrackIndex = index;
        TrackJson track = tracksList.get(index);
        currentPlayingTrack = track;
        recentActivityManager.addTrackToRecents(track);
        updatePlayerUI(track);

        if (mediaPlayer != null) {
            handler.removeCallbacks(updateSeekBarRunnable);
            mediaPlayer.release();
            mediaPlayer = null;
        }

        if (track.nid != null && !track.nid.startsWith("http")) { // Local file
            try {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(getApplicationContext(), Uri.parse(track.nid));
                mediaPlayer.setOnPreparedListener(mp -> {
                    int result = audioManager.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                    if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                        mp.start();
                        playPauseAction(true);
                        int duration = mp.getDuration();
                        seekBar.setMax(duration);
                        endTime.setText(formatTime(duration));
                        handler.postDelayed(updateSeekBarRunnable, 0);
                    }
                });
                mediaPlayer.setOnCompletionListener(mp -> {
                    handler.removeCallbacks(updateSeekBarRunnable);
                    if (repeatMode != 2) {
                        playNextTrack();
                    } else {
                        // Loop is handled natively by setLooping(true), but we reset UI just in case
                        playTrack(currentTrackIndex);
                    }
                });
                mediaPlayer.prepareAsync();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to play track", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void playNextTrack() {
        if (tracksList.isEmpty()) return;
        
        if (isShuffle) {
            int nextIndex = currentTrackIndex;
            while (nextIndex == currentTrackIndex && tracksList.size() > 1) {
                nextIndex = new java.util.Random().nextInt(tracksList.size());
            }
            playTrack(nextIndex);
        } else {
            if (currentTrackIndex >= 0 && currentTrackIndex < tracksList.size() - 1) {
                playTrack(currentTrackIndex + 1);
            } else if (repeatMode == 1 || currentTrackIndex == -1) { // Repeat All
                playTrack(0);
            } else {
                playPauseAction(false);
            }
        }
    }

    private void playPrevTrack() {
        if (playbackQueue.isEmpty()) return;

        if (mediaPlayer != null && mediaPlayer.getCurrentPosition() > 3000) {
            // If played more than 3 seconds, restart current track
            mediaPlayer.seekTo(0);
            return;
        }

        if (isShuffle) {
            playNextTrack(); // In basic shuffle, prev just picks another random
        } else {
            if (currentTrackIndex > 0) {
                playTrack(currentTrackIndex - 1);
            } else if (repeatMode == 1 || currentTrackIndex == -1) {
                playTrack(playbackQueue.size() - 1);
            }
        }
    }

    private String formatTime(int millis) {
        int seconds = (millis / 1000) % 60;
        int minutes = (millis / (1000 * 60)) % 60;
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
    }

    private void updatePlayerUI(TrackJson track) {
        String title = track.title != null ? track.title : "Unknown Title";
        String artist = track.artist != null ? track.artist : "Unknown Artist";

        songs_title.setText(title);
        songs_artist_name.setText(artist);
        mini_songs_title.setText(title);
        mini_songs_artist_name.setText(artist);

        if (track.albumArtRef != null && !track.albumArtRef.isEmpty() && track.albumArtRef.get(0).url != null) {
            Glide.with(this)
                    .load(track.albumArtRef.get(0).url)
                    .centerCrop()
                    .error(R.drawable.songs_cover)
                    .into(songs_cover_one);
        } else {
            songs_cover_one.setImageResource(R.drawable.songs_cover);
        }

        if (playerPagerAdapter != null) {
            // Provide the current track + the rest of the queue
            List<TrackJson> upcomingQueue = new ArrayList<>();
            if (currentTrackIndex >= 0 && currentTrackIndex < playbackQueue.size()) {
                // Show upcoming tracks in queue
                upcomingQueue.addAll(playbackQueue.subList(currentTrackIndex + 1, playbackQueue.size()));
            }
            playerPagerAdapter.updateData(track, upcomingQueue);
        }
    }

    private void setupClickListeners() {
        like.setOnClickListener(v -> {
            isLiked = !isLiked;
            if (isLiked) {
                isDisliked = false;
                like.setImageResource(R.drawable.ic_thumbs_up_selected);
                dislike.setImageResource(R.drawable.ic_thumbs_down_default);
                Toast.makeText(MainActivity.this, "Added to Thumbs up", Toast.LENGTH_SHORT).show();
            } else {
                like.setImageResource(R.drawable.ic_thumbs_up_default);
            }
        });

        dislike.setOnClickListener(v -> {
            isDisliked = !isDisliked;
            if (isDisliked) {
                isLiked = false;
                dislike.setImageResource(R.drawable.ic_thumbs_down_selected);
                like.setImageResource(R.drawable.ic_thumbs_up_default);
            } else {
                dislike.setImageResource(R.drawable.ic_thumbs_down_default);
            }
        });

        play.setOnClickListener(v -> togglePlayPause());
        play_main.setOnClickListener(v -> togglePlayPause());
        pause.setOnClickListener(v -> togglePlayPause());
        pause_main.setOnClickListener(v -> togglePlayPause());

        prev.setOnClickListener(v -> playPrevTrack());
        next.setOnClickListener(v -> playNextTrack());

        btn_repeat.setOnClickListener(v -> {
            repeatMode = (repeatMode + 1) % 3;
            if (repeatMode == 0) {
                btn_repeat.setImageResource(R.drawable.ic_repeat_dark);
                btn_repeat.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.gpm_text_secondary));
                if (mediaPlayer != null) mediaPlayer.setLooping(false);
            } else if (repeatMode == 1) {
                btn_repeat.setImageResource(R.drawable.ic_repeat_dark);
                btn_repeat.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary));
                if (mediaPlayer != null) mediaPlayer.setLooping(false);
            } else if (repeatMode == 2) {
                btn_repeat.setImageResource(R.drawable.ic_repeat_one_song_dark);
                btn_repeat.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary));
                if (mediaPlayer != null) mediaPlayer.setLooping(true);
            }
        });

        btn_shuffle.setOnClickListener(v -> {
            isShuffle = !isShuffle;
            if (isShuffle) {
                btn_shuffle.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary));
            } else {
                btn_shuffle.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.gpm_text_secondary));
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                    startTime.setText(formatTime(progress));
                }
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void togglePlayPause() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                playPauseAction(false);
                handler.removeCallbacks(updateSeekBarRunnable);
                audioManager.abandonAudioFocus(audioFocusChangeListener);
            } else {
                int result = audioManager.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    mediaPlayer.start();
                    playPauseAction(true);
                    handler.postDelayed(updateSeekBarRunnable, 0);
                }
            }
        }
    }

    private void updateServiceNotification(TrackJson track, boolean isPlaying) {
        Intent serviceIntent = new Intent(this, MusicService.class);
        serviceIntent.setAction(MusicService.ACTION_UPDATE);
        serviceIntent.putExtra(MusicService.EXTRA_TITLE, track.title != null ? track.title : "Unknown Title");
        serviceIntent.putExtra(MusicService.EXTRA_ARTIST, track.artist != null ? track.artist : "Unknown Artist");
        serviceIntent.putExtra(MusicService.EXTRA_IS_PLAYING, isPlaying);
        if (track.albumArtRef != null && !track.albumArtRef.isEmpty() && track.albumArtRef.get(0).url != null) {
            serviceIntent.putExtra(MusicService.EXTRA_ART_URL, track.albumArtRef.get(0).url);
        }
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    private void playPauseAction(boolean isPlaying) {
        if (currentPlayingTrack != null) {
            updateServiceNotification(currentPlayingTrack, isPlaying);
        }
        if (isPlaying) {
            play.setVisibility(View.GONE);
            play_main.setVisibility(View.GONE);
            pause.setVisibility(View.VISIBLE);
            pause_main.setVisibility(View.VISIBLE);
        } else {
            pause.setVisibility(View.GONE);
            pause_main.setVisibility(View.GONE);
            play.setVisibility(View.VISIBLE);
            play_main.setVisibility(View.VISIBLE);
        }
    }

    private void showTrackOptionsBottomSheet(TrackJson track) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_track_options, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        TextView tvTitle = bottomSheetView.findViewById(R.id.bs_track_title);
        TextView tvArtist = bottomSheetView.findViewById(R.id.bs_track_artist);
        ImageView ivArt = bottomSheetView.findViewById(R.id.bs_album_art);

        tvTitle.setText(track.title != null ? track.title : "Unknown Title");
        tvArtist.setText(track.artist != null ? track.artist : "Unknown Artist");

        if (track.albumArtRef != null && !track.albumArtRef.isEmpty() && track.albumArtRef.get(0).url != null) {
            Glide.with(this)
                    .load(track.albumArtRef.get(0).url)
                    .centerCrop()
                    .error(R.drawable.songs_cover)
                    .into(ivArt);
        } else {
            ivArt.setImageResource(R.drawable.songs_cover);
        }

        bottomSheetDialog.show();

        // Wire up actions
        View actionPlayNext = bottomSheetView.findViewById(R.id.action_play_next);
        if (actionPlayNext != null) {
            actionPlayNext.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                if (playbackQueue.isEmpty()) {
                    playTrackFromLibrary(track);
                } else {
                    int insertIndex = currentTrackIndex + 1;
                    if (insertIndex > playbackQueue.size()) insertIndex = playbackQueue.size();
                    playbackQueue.add(insertIndex, track);
                    if (playerPagerAdapter != null) {
                        playerPagerAdapter.updateData(currentPlayingTrack, new ArrayList<>(playbackQueue.subList(currentTrackIndex + 1, playbackQueue.size())));
                    }
                    Toast.makeText(this, "Playing next: " + track.title, Toast.LENGTH_SHORT).show();
                }
            });
        }

        View actionAddToQueue = bottomSheetView.findViewById(R.id.action_add_to_queue);
        if (actionAddToQueue != null) {
            actionAddToQueue.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                if (playbackQueue.isEmpty()) {
                    playTrackFromLibrary(track);
                } else {
                    playbackQueue.add(track);
                    if (playerPagerAdapter != null) {
                        playerPagerAdapter.updateData(currentPlayingTrack, new ArrayList<>(playbackQueue.subList(currentTrackIndex + 1, playbackQueue.size())));
                    }
                    Toast.makeText(this, "Added to queue: " + track.title, Toast.LENGTH_SHORT).show();
                }
            });
        }
        
        View actionGoToAlbum = bottomSheetView.findViewById(R.id.action_go_to_album);
        if (actionGoToAlbum != null) {
            actionGoToAlbum.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                long albumId = -1;
                try {
                    albumId = Long.parseLong(track.albumId);
                } catch (Exception ignored) {}
                if (albumId != -1) {
                    if (mLayout != null) mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    String artUri = (track.albumArtRef != null && !track.albumArtRef.isEmpty()) ? track.albumArtRef.get(0).url : null;
                    openLibraryDetail("ALBUM", albumId, track.album != null ? track.album : "Unknown Album", track.artist, artUri);
                } else {
                    Toast.makeText(this, "Album not available", Toast.LENGTH_SHORT).show();
                }
            });
        }

        View actionGoToArtist = bottomSheetView.findViewById(R.id.action_go_to_artist);
        if (actionGoToArtist != null) {
            actionGoToArtist.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                // Since we don't have artistId easily available in MediaStore query, we just fallback gracefully or search
                Toast.makeText(this, "Artist search coming soon", Toast.LENGTH_SHORT).show();
            });
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            openSearch();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openSearch() {
        if (mainToolbar != null) mainToolbar.setVisibility(View.GONE);
        if (searchPlate != null) {
            searchPlate.setVisibility(View.VISIBLE);
            searchBox.requestFocus();
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(searchBox, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void closeSearch() {
        if (searchPlate != null) {
            searchPlate.setVisibility(View.GONE);
            searchBox.setText("");
            searchBox.clearFocus();
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(searchBox.getWindowToken(), 0);
        }
        if (mainToolbar != null) mainToolbar.setVisibility(View.VISIBLE);
    }

    private void filterTracks(String query) {
        if (rvMainContent.getAdapter() != trackAdapter) {
            rvMainContent.setAdapter(trackAdapter);
        }
        
        if (query == null || query.trim().isEmpty()) {
            tracksList = new ArrayList<>(allTracksList);
        } else {
            String lowerCaseQuery = query.toLowerCase(Locale.getDefault());
            tracksList = new ArrayList<>();
            for (TrackJson track : allTracksList) {
                if ((track.title != null && track.title.toLowerCase(Locale.getDefault()).contains(lowerCaseQuery)) ||
                    (track.artist != null && track.artist.toLowerCase(Locale.getDefault()).contains(lowerCaseQuery))) {
                    tracksList.add(track);
                }
            }
        }
        trackAdapter.setTracks(tracksList);
        
        if (currentPlayingTrack != null) {
            currentTrackIndex = tracksList.indexOf(currentPlayingTrack);
        }
    }

    private void showSettings() {
        if (mainToolbar != null) mainToolbar.setVisibility(View.GONE);
        if (searchPlate != null) searchPlate.setVisibility(View.GONE);
        if (settingsToolbar != null) settingsToolbar.setVisibility(View.VISIBLE);
        if (libraryTabs != null) libraryTabs.setVisibility(View.GONE);
        
        rvMainContent.setVisibility(View.GONE);
        if (libraryViewPager != null) libraryViewPager.setVisibility(View.GONE);
        findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);
        
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new SettingsFragment())
                .commit();
    }

    private void hideSettings() {
        findViewById(R.id.fragment_container).setVisibility(View.GONE);
        if (settingsToolbar != null) settingsToolbar.setVisibility(View.GONE);
        if (searchPlate != null) searchPlate.setVisibility(View.GONE);
        if (mainToolbar != null) mainToolbar.setVisibility(View.VISIBLE);
    }

    private void showShop() {
        if (mainToolbar != null) mainToolbar.setVisibility(View.VISIBLE);
        if (searchPlate != null) searchPlate.setVisibility(View.GONE);
        if (settingsToolbar != null) settingsToolbar.setVisibility(View.GONE);
        if (libraryTabs != null) libraryTabs.setVisibility(View.GONE);
        
        rvMainContent.setVisibility(View.GONE);
        if (libraryViewPager != null) libraryViewPager.setVisibility(View.GONE);
        findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);
        
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new ShopFragment())
                .commit();
    }

    private void showNewReleases() {
        if (mainToolbar != null) mainToolbar.setVisibility(View.VISIBLE);
        if (searchPlate != null) searchPlate.setVisibility(View.GONE);
        if (settingsToolbar != null) settingsToolbar.setVisibility(View.GONE);
        if (libraryTabs != null) libraryTabs.setVisibility(View.GONE);
        
        rvMainContent.setVisibility(View.GONE);
        if (libraryViewPager != null) libraryViewPager.setVisibility(View.GONE);
        findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);
        
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new NewReleasesFragment())
                .commit();
    }

    private void showBrowseStations() {
        if (mainToolbar != null) mainToolbar.setVisibility(View.VISIBLE);
        if (searchPlate != null) searchPlate.setVisibility(View.GONE);
        if (settingsToolbar != null) settingsToolbar.setVisibility(View.GONE);
        if (libraryTabs != null) libraryTabs.setVisibility(View.GONE);
        
        rvMainContent.setVisibility(View.GONE);
        if (libraryViewPager != null) libraryViewPager.setVisibility(View.GONE);
        findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);
        
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new BrowseStationsFragment())
                .commit();
    }

    private void showPodcasts() {
        if (mainToolbar != null) mainToolbar.setVisibility(View.VISIBLE);
        if (searchPlate != null) searchPlate.setVisibility(View.GONE);
        if (settingsToolbar != null) settingsToolbar.setVisibility(View.GONE);
        if (libraryTabs != null) libraryTabs.setVisibility(View.GONE);

        rvMainContent.setVisibility(View.GONE);
        if (libraryViewPager != null) libraryViewPager.setVisibility(View.GONE);
        findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new PodcastsFragment())
                .commit();
    }

    private void showTopCharts() {
        if (mainToolbar != null) mainToolbar.setVisibility(View.VISIBLE);
        if (searchPlate != null) searchPlate.setVisibility(View.GONE);
        if (settingsToolbar != null) settingsToolbar.setVisibility(View.GONE);
        if (libraryTabs != null) libraryTabs.setVisibility(View.GONE);
        
        rvMainContent.setVisibility(View.GONE);
        if (libraryViewPager != null) libraryViewPager.setVisibility(View.GONE);
        findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);
        
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new TopChartsFragment())
                .commit();
    }

    private void showRecents() {
        if (mainToolbar != null) mainToolbar.setVisibility(View.VISIBLE);
        if (searchPlate != null) searchPlate.setVisibility(View.GONE);
        if (settingsToolbar != null) settingsToolbar.setVisibility(View.GONE);
        if (libraryTabs != null) libraryTabs.setVisibility(View.GONE);

        rvMainContent.setVisibility(View.GONE);
        if (libraryViewPager != null) libraryViewPager.setVisibility(View.GONE);
        findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new RecentsFragment())
                .commit();
    }

    private void showListenNow() {
        hideSettings();
        if (libraryTabs != null) libraryTabs.setVisibility(View.GONE);
        if (libraryViewPager != null) libraryViewPager.setVisibility(View.GONE);
        
        rvMainContent.setAdapter(clusterAdapter);
        rvMainContent.setVisibility(View.VISIBLE);
        
        loadListenNowClusters();
    }
    
    private void loadListenNowClusters() {
        List<ClusterJson> dashboard = new ArrayList<>();
        LocalMusicHelper helper = getLocalMusicHelper();
        
        List<TrackJson> recent = helper.getAlbums();
        if (recent != null && !recent.isEmpty()) {
            // Take up to 10
            dashboard.add(new ClusterJson("Recent Activity", null, recent.subList(0, Math.min(10, recent.size()))));
        }
        
        List<TrackJson> suggested = helper.getAllSongs();
        if (suggested != null && !suggested.isEmpty()) {
            java.util.Collections.shuffle(suggested);
            dashboard.add(new ClusterJson("Recommended for you", "Based on your library", suggested.subList(0, Math.min(10, suggested.size()))));
        }
        
        List<TrackJson> artists = helper.getArtists();
        if (artists != null && !artists.isEmpty()) {
            dashboard.add(new ClusterJson("Your favorite artists", null, artists.subList(0, Math.min(10, artists.size()))));
        }

        clusterAdapter.setClusters(dashboard);
    }

    private void showMusicLibrary() {
        hideSettings();
        rvMainContent.setVisibility(View.GONE);
        if (libraryTabs != null) libraryTabs.setVisibility(View.VISIBLE);
        if (libraryViewPager != null) libraryViewPager.setVisibility(View.VISIBLE);
    }

    public void clearLibrary() {
        allTracksList.clear();
        tracksList.clear();
        trackAdapter.notifyDataSetChanged();
        
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        
        songs_title.setText("Unknown Title");
        songs_artist_name.setText("Unknown Artist");
        mini_songs_title.setText("Unknown Title");
        mini_songs_artist_name.setText("Unknown Artist");
        songs_cover_one.setImageResource(R.drawable.songs_cover);
        if (playerPagerAdapter != null) {
            playerPagerAdapter.updateData(null, new ArrayList<>());
        }
        playPauseAction(false);
    }

    public List<TrackJson> getAllTracks() {
        return new ArrayList<>(allTracksList);
    }

    public void playTrackFromLibrary(TrackJson track) {
        int index = allTracksList.indexOf(track);
        if (index != -1) {
            currentTrackIndex = index;
            tracksList = new ArrayList<>(allTracksList);
            trackAdapter.setTracks(tracksList);
            playTrack(currentTrackIndex);
        } else {
            // Fallback if track not found in allTracksList
            tracksList = new ArrayList<>();
            tracksList.add(track);
            trackAdapter.setTracks(tracksList);
            playTrack(0);
        }
        if (mLayout != null && mLayout.getPanelState() != SlidingUpPanelLayout.PanelState.EXPANDED) {
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
        }
    }

    public void playQueue(List<TrackJson> queue, int startIndex) {
        if (queue == null || queue.isEmpty() || startIndex < 0 || startIndex >= queue.size()) return;
        
        tracksList = new ArrayList<>(queue);
        trackAdapter.setTracks(tracksList);
        currentTrackIndex = startIndex;
        
        playTrack(currentTrackIndex);
        
        if (mLayout != null && mLayout.getPanelState() != SlidingUpPanelLayout.PanelState.EXPANDED) {
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
        }
    }

    public void clearCache() {
        try {
            java.io.File cacheDir = getCacheDir();
            if (cacheDir != null && cacheDir.isDirectory()) {
                deleteDir(cacheDir);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean deleteDir(java.io.File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    boolean success = deleteDir(new java.io.File(dir, child));
                    if (!success) return false;
                }
            }
        }
        return dir != null && dir.delete();
    }

    private Handler sleepTimerHandler = new Handler();
    private Runnable sleepTimerRunnable = () -> {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            playPauseAction(false);
            Toast.makeText(this, "Sleep timer: Music paused", Toast.LENGTH_SHORT).show();
        }
    };

    public void setSleepTimer(int minutes) {
        sleepTimerHandler.removeCallbacks(sleepTimerRunnable);
        if (minutes > 0) {
            sleepTimerHandler.postDelayed(sleepTimerRunnable, (long) minutes * 60 * 1000);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_listen_now) {
            showListenNow();
            if (searchBox != null) searchBox.setHint("Listen Now");
            if (mainToolbar != null) mainToolbar.setTitle("Listen Now");
        } else if (id == R.id.nav_recents) {
            showRecents();
            if (searchBox != null) searchBox.setHint("Recents");
            if (mainToolbar != null) mainToolbar.setTitle("Recents");
        } else if (id == R.id.nav_music_library) {
            showMusicLibrary();
            if (searchBox != null) searchBox.setHint("Music library");
            if (mainToolbar != null) mainToolbar.setTitle("Music library");
        } else if (id == R.id.nav_podcasts) {
            showPodcasts();
            if (searchBox != null) searchBox.setHint(R.string.title_podcasts);
            if (mainToolbar != null) mainToolbar.setTitle(R.string.title_podcasts);
        } else if (id == R.id.nav_top_charts) {
            showTopCharts();
            if (searchBox != null) searchBox.setHint("Top charts");
            if (mainToolbar != null) mainToolbar.setTitle("Top charts");
        } else if (id == R.id.nav_new_releases) {
            showNewReleases();
            if (searchBox != null) searchBox.setHint("New releases");
            if (mainToolbar != null) mainToolbar.setTitle("New releases");
        } else if (id == R.id.nav_settings) {
            showSettings();
        } else if (id == R.id.nav_browse_stations) {
            showBrowseStations();
            if (searchBox != null) searchBox.setHint("Browse stations");
            if (mainToolbar != null) mainToolbar.setTitle("Browse stations");
        } else if (id == R.id.nav_help_feedback) {
            android.content.Intent browserIntent = new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://t.me/carthigan"));
            startActivity(browserIntent);
        } else if (id == R.id.nav_shop) {
            showShop();
            if (searchBox != null) searchBox.setHint("Shop");
            if (mainToolbar != null) mainToolbar.setTitle("Shop");
        } else {
            showListenNow();
            if (searchBox != null) searchBox.setHint(item.getTitle());
            if (mainToolbar != null) mainToolbar.setTitle(item.getTitle());
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (searchPlate != null && searchPlate.getVisibility() == View.VISIBLE) {
            closeSearch();
        } else if (mLayout != null &&
                (mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED || mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            // Restore visibility state for Library if coming back from LibraryDetail
            if (getSupportFragmentManager().getBackStackEntryCount() == 1) { // Was 1, now 0 essentially
                 showMusicLibrary();
            }
        } else if (findViewById(R.id.fragment_container) != null && findViewById(R.id.fragment_container).getVisibility() == View.VISIBLE) {
            showListenNow();
            if (searchBox != null) searchBox.setHint(R.string.title_listen_now);
            if (mainToolbar != null) mainToolbar.setTitle(R.string.title_listen_now);
            NavigationView navigationView = findViewById(R.id.nav_view);
            if (navigationView != null) navigationView.setCheckedItem(R.id.nav_listen_now);
        } else if (libraryViewPager != null && libraryViewPager.getVisibility() == View.VISIBLE) {
            showListenNow();
            if (searchBox != null) searchBox.setHint(R.string.title_listen_now);
            NavigationView navigationView = findViewById(R.id.nav_view);
            navigationView.setCheckedItem(R.id.nav_listen_now);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(updateSeekBarRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            handler.postDelayed(updateSeekBarRunnable, 0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(musicControlReceiver);
        } catch (IllegalArgumentException e) {
            // Already unregistered
        }
        Intent serviceIntent = new Intent(this, MusicService.class);
        stopService(serviceIntent);
        
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (audioManager != null) {
            audioManager.abandonAudioFocus(audioFocusChangeListener);
        }
    }
}
