package com.carthigan.playmusic;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.carthigan.playmusic.data.RecentActivityManager;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView rv = getListView();
        if (rv != null) {
            float density = getResources().getDisplayMetrics().density;
            rv.setPadding(rv.getPaddingLeft(), rv.getPaddingTop(), rv.getPaddingRight(), rv.getPaddingBottom() + (int)(80 * density));
            rv.setClipToPadding(false);
        }
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.music_settings, rootKey);

        // Account / Refresh
        Preference accountPref = findPreference("account_settings_key");
        if (accountPref != null) {
            accountPref.setOnPreferenceClickListener(preference -> {
                showMockDialog("Google Account", "Select an account to sync your library.");
                return true;
            });
        }

        Preference refreshPref = findPreference("refresh_key");
        if (refreshPref != null) {
            refreshPref.setOnPreferenceClickListener(preference -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).refreshLibrary();
                    Toast.makeText(getContext(), "Library refreshed.", Toast.LENGTH_SHORT).show();
                }
                return true;
            });
        }

        // General
        Preference exportPlaylistsPref = findPreference("settings_playlist_export_key");
        if (exportPlaylistsPref != null) {
            exportPlaylistsPref.setOnPreferenceClickListener(preference -> {
                Toast.makeText(getContext(), "No local playlists found to export.", Toast.LENGTH_SHORT).show();
                return true;
            });
        }

        // Sleep Timer
        Preference sleepTimerPref = findPreference("sleep_timer_settings_key");
        if (sleepTimerPref != null) {
            sleepTimerPref.setOnPreferenceClickListener(preference -> {
                String[] options = {"Off", "10 minutes", "20 minutes", "30 minutes", "1 hour"};
                int[] minutes = {0, 10, 20, 30, 60};
                new AlertDialog.Builder(requireContext())
                        .setTitle("Sleep timer")
                        .setItems(options, (dialog, which) -> {
                            if (getActivity() instanceof MainActivity) {
                                ((MainActivity) getActivity()).setSleepTimer(minutes[which]);
                                if (which == 0) {
                                    sleepTimerPref.setSummary("Off");
                                } else {
                                    sleepTimerPref.setSummary(options[which]);
                                    Toast.makeText(getContext(), "Sleep timer set for " + options[which], Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                return true;
            });
        }

        // Privacy & location
        Preference activityHistoryPref = findPreference("settings_activity_history_key");
        if (activityHistoryPref != null) {
            activityHistoryPref.setOnPreferenceClickListener(preference -> {
                showMockDialog("Manage activity history", "Activity history is managed by your Google Account. As you are using the local player, no activity history is recorded locally.");
                return true;
            });
        }

        Preference deleteRecHistoryPref = findPreference("delete_recommendation_history_setting_key");
        if (deleteRecHistoryPref != null) {
            deleteRecHistoryPref.setOnPreferenceClickListener(preference -> {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Delete recommendation history")
                        .setMessage("Are you sure you want to delete your recommendation history?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            new RecentActivityManager(requireContext()).clearHistory();
                            Toast.makeText(getContext(), "Recommendation history cleared", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                return true;
            });
        }

        // Delete my library
        Preference deleteLibraryPref = findPreference("delete_my_library_setting_key");
        if (deleteLibraryPref != null) {
            deleteLibraryPref.setOnPreferenceClickListener(preference -> {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Delete my library")
                        .setMessage("Are you sure you want to delete your entire music library from this device? This action cannot be undone.")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            if (getActivity() instanceof MainActivity) {
                                ((MainActivity) getActivity()).clearLibrary();
                                Toast.makeText(getContext(), "Library deleted", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                return true;
            });
        }

        // Playback
        Preference equalizerPref = findPreference("equalizer_settings_key");
        if (equalizerPref != null) {
            equalizerPref.setOnPreferenceClickListener(preference -> {
                try {
                    Intent eqIntent = new Intent(android.media.audiofx.AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
                    eqIntent.putExtra(android.media.audiofx.AudioEffect.EXTRA_PACKAGE_NAME, requireContext().getPackageName());
                    startActivityForResult(eqIntent, 0);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "No equalizer found on this device", Toast.LENGTH_SHORT).show();
                }
                return true;
            });
        }

        setupQualityPreference("stream_quality_for_mobile_key", "Quality on mobile network");
        setupQualityPreference("stream_quality_for_wifi_key", "Quality on Wi-Fi network");
        setupQualityPreference("download_quality_key", "Download quality");

        // Downloading
        Preference clearCachePref = findPreference("clear_cache_settings_key");
        if (clearCachePref != null) {
            clearCachePref.setOnPreferenceClickListener(preference -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).clearCache();
                    preference.setSummary("0 MB used");
                    Toast.makeText(getContext(), "Cache cleared", Toast.LENGTH_SHORT).show();
                }
                return true;
            });
        }

        Preference downloadQueuePref = findPreference("download_queue_key");
        if (downloadQueuePref != null) {
            downloadQueuePref.setOnPreferenceClickListener(preference -> {
                Toast.makeText(getContext(), "No active downloads.", Toast.LENGTH_SHORT).show();
                return true;
            });
        }

        // Version
        Preference versionPref = findPreference("music_version_key");
        if (versionPref != null) {
            versionPref.setOnPreferenceClickListener(preference -> {
                Toast.makeText(getContext(), "Play Music by Carthigan", Toast.LENGTH_SHORT).show();
                return true;
            });
        }

        // Privacy Policy
        Preference privacyPref = findPreference("privacy_policy_key");
        if (privacyPref != null) {
            privacyPref.setOnPreferenceClickListener(preference -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://policies.google.com/privacy"));
                startActivity(browserIntent);
                return true;
            });
        }
    }

    private void setupQualityPreference(String key, String title) {
        Preference pref = findPreference(key);
        if (pref != null) {
            // Load saved preference or default to Normal
            String currentQuality = getPreferenceManager().getSharedPreferences().getString(key, "Normal");
            pref.setSummary(currentQuality);
            
            pref.setOnPreferenceClickListener(preference -> {
                String[] options = {"Low", "Normal", "High", "Always Ask"};
                int checkedItem = -1;
                for (int i = 0; i < options.length; i++) {
                    if (options[i].equals(pref.getSummary())) {
                        checkedItem = i;
                        break;
                    }
                }
                
                new AlertDialog.Builder(requireContext())
                        .setTitle(title)
                        .setSingleChoiceItems(options, checkedItem, (dialog, which) -> {
                            String selection = options[which];
                            pref.setSummary(selection);
                            getPreferenceManager().getSharedPreferences().edit().putString(key, selection).apply();
                            dialog.dismiss();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                return true;
            });
        }
    }

    private void showMockDialog(String title, String message) {
        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}
