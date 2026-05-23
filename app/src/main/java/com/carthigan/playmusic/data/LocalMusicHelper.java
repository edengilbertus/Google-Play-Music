package com.carthigan.playmusic.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.carthigan.playmusic.api.models.ImageRefJson;
import com.carthigan.playmusic.api.models.TrackJson;

import java.util.ArrayList;
import java.util.List;

public class LocalMusicHelper {

    private final Context context;
    private final ContentResolver contentResolver;

    public LocalMusicHelper(Context context) {
        this.context = context;
        this.contentResolver = context.getContentResolver();
    }

    private String getAlbumArtUri(long albumId) {
        return Uri.parse("content://media/external/audio/albumart/" + albumId).toString();
    }

    public List<TrackJson> getAllSongs() {
        List<TrackJson> tracks = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";

        try (Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder)) {
            if (cursor != null && cursor.moveToFirst()) {
                int idCol = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
                int titleCol = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                int artistCol = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                int albumCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
                int albumIdCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
                int durationCol = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);

                do {
                    long id = cursor.getLong(idCol);
                    long albumId = cursor.getLong(albumIdCol);
                    
                    TrackJson track = new TrackJson();
                    track.nid = ContentUris.withAppendedId(uri, id).toString();
                    track.title = cursor.getString(titleCol);
                    track.artist = cursor.getString(artistCol);
                    track.album = cursor.getString(albumCol);
                    track.albumId = String.valueOf(albumId);
                    if (durationCol != -1) track.durationMillis = cursor.getLong(durationCol);

                    track.albumArtRef = new ArrayList<>();
                    ImageRefJson img = new ImageRefJson();
                    img.url = getAlbumArtUri(albumId);
                    track.albumArtRef.add(img);

                    tracks.add(track);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) { e.printStackTrace(); }
        return tracks;
    }

    public List<TrackJson> getAlbums() {
        List<TrackJson> albums = new ArrayList<>();
        Uri uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
        String sortOrder = MediaStore.Audio.Albums.ALBUM + " ASC";

        try (Cursor cursor = contentResolver.query(uri, null, null, null, sortOrder)) {
            if (cursor != null && cursor.moveToFirst()) {
                int idCol = cursor.getColumnIndex(MediaStore.Audio.Albums._ID);
                int albumCol = cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM);
                int artistCol = cursor.getColumnIndex(MediaStore.Audio.Albums.ARTIST);
                int numSongsCol = cursor.getColumnIndex(MediaStore.Audio.Albums.NUMBER_OF_SONGS);

                do {
                    long id = cursor.getLong(idCol);
                    TrackJson track = new TrackJson();
                    track.nid = String.valueOf(id); // Use nid to store Album ID
                    track.title = cursor.getString(albumCol);
                    track.artist = cursor.getString(artistCol);
                    
                    // Store song count in duration temporarily for display if needed
                    if (numSongsCol != -1) track.playCount = cursor.getInt(numSongsCol);

                    track.albumArtRef = new ArrayList<>();
                    ImageRefJson img = new ImageRefJson();
                    img.url = getAlbumArtUri(id);
                    track.albumArtRef.add(img);

                    albums.add(track);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) { e.printStackTrace(); }
        return albums;
    }

    public List<TrackJson> getArtists() {
        List<TrackJson> artists = new ArrayList<>();
        Uri uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
        String sortOrder = MediaStore.Audio.Artists.ARTIST + " ASC";

        try (Cursor cursor = contentResolver.query(uri, null, null, null, sortOrder)) {
            if (cursor != null && cursor.moveToFirst()) {
                int idCol = cursor.getColumnIndex(MediaStore.Audio.Artists._ID);
                int artistCol = cursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST);
                int numAlbumsCol = cursor.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS);

                do {
                    long id = cursor.getLong(idCol);
                    TrackJson track = new TrackJson();
                    track.nid = String.valueOf(id); // Use nid for Artist ID
                    track.title = cursor.getString(artistCol);
                    if (numAlbumsCol != -1) {
                        int albumsCount = cursor.getInt(numAlbumsCol);
                        track.artist = albumsCount + (albumsCount == 1 ? " album" : " albums");
                    }
                    artists.add(track);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) { e.printStackTrace(); }
        return artists;
    }

    public List<TrackJson> getPlaylists() {
        List<TrackJson> playlists = new ArrayList<>();
        Uri uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        String sortOrder = MediaStore.Audio.Playlists.NAME + " ASC";

        try (Cursor cursor = contentResolver.query(uri, null, null, null, sortOrder)) {
            if (cursor != null && cursor.moveToFirst()) {
                int idCol = cursor.getColumnIndex(MediaStore.Audio.Playlists._ID);
                int nameCol = cursor.getColumnIndex(MediaStore.Audio.Playlists.NAME);

                do {
                    long id = cursor.getLong(idCol);
                    TrackJson track = new TrackJson();
                    track.nid = String.valueOf(id);
                    track.title = cursor.getString(nameCol);
                    track.artist = "Playlist";
                    playlists.add(track);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) { e.printStackTrace(); }
        return playlists;
    }

    public List<TrackJson> getGenres() {
        List<TrackJson> genres = new ArrayList<>();
        Uri uri = MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI;
        String sortOrder = MediaStore.Audio.Genres.NAME + " ASC";

        try (Cursor cursor = contentResolver.query(uri, null, null, null, sortOrder)) {
            if (cursor != null && cursor.moveToFirst()) {
                int idCol = cursor.getColumnIndex(MediaStore.Audio.Genres._ID);
                int nameCol = cursor.getColumnIndex(MediaStore.Audio.Genres.NAME);

                do {
                    long id = cursor.getLong(idCol);
                    TrackJson track = new TrackJson();
                    track.nid = String.valueOf(id);
                    track.title = cursor.getString(nameCol);
                    track.artist = "Genre";
                    genres.add(track);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) { e.printStackTrace(); }
        return genres;
    }

    // --- Query specifics ---

    public List<TrackJson> getSongsForAlbum(long albumId) {
        String selection = MediaStore.Audio.Media.ALBUM_ID + "=?";
        String[] selectionArgs = { String.valueOf(albumId) };
        return querySongs(selection, selectionArgs, MediaStore.Audio.Media.TRACK + " ASC");
    }

    public List<TrackJson> getSongsForArtist(long artistId) {
        String selection = MediaStore.Audio.Media.ARTIST_ID + "=?";
        String[] selectionArgs = { String.valueOf(artistId) };
        return querySongs(selection, selectionArgs, MediaStore.Audio.Media.TITLE + " ASC");
    }

    public List<TrackJson> getSongsForPlaylist(long playlistId) {
        List<TrackJson> tracks = new ArrayList<>();
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
        String sortOrder = MediaStore.Audio.Playlists.Members.PLAY_ORDER + " ASC";

        try (Cursor cursor = contentResolver.query(uri, null, null, null, sortOrder)) {
            if (cursor != null && cursor.moveToFirst()) {
                int audioIdCol = cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.AUDIO_ID);
                int titleCol = cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.TITLE);
                int artistCol = cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.ARTIST);
                int albumIdCol = cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.ALBUM_ID);

                do {
                    long audioId = cursor.getLong(audioIdCol);
                    long albumId = cursor.getLong(albumIdCol);
                    
                    TrackJson track = new TrackJson();
                    track.nid = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, audioId).toString();
                    track.title = cursor.getString(titleCol);
                    track.artist = cursor.getString(artistCol);

                    track.albumArtRef = new ArrayList<>();
                    ImageRefJson img = new ImageRefJson();
                    img.url = getAlbumArtUri(albumId);
                    track.albumArtRef.add(img);

                    tracks.add(track);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) { e.printStackTrace(); }
        return tracks;
    }
    
    public List<TrackJson> getSongsForGenre(long genreId) {
        List<TrackJson> tracks = new ArrayList<>();
        Uri uri = MediaStore.Audio.Genres.Members.getContentUri("external", genreId);
        
        try (Cursor cursor = contentResolver.query(uri, null, null, null, MediaStore.Audio.Genres.Members.TITLE + " ASC")) {
            if (cursor != null && cursor.moveToFirst()) {
                int audioIdCol = cursor.getColumnIndex(MediaStore.Audio.Genres.Members.AUDIO_ID);
                int titleCol = cursor.getColumnIndex(MediaStore.Audio.Genres.Members.TITLE);
                int artistCol = cursor.getColumnIndex(MediaStore.Audio.Genres.Members.ARTIST);
                int albumIdCol = cursor.getColumnIndex(MediaStore.Audio.Genres.Members.ALBUM_ID);

                do {
                    long audioId = cursor.getLong(audioIdCol);
                    long albumId = cursor.getLong(albumIdCol);
                    
                    TrackJson track = new TrackJson();
                    track.nid = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, audioId).toString();
                    track.title = cursor.getString(titleCol);
                    track.artist = cursor.getString(artistCol);

                    track.albumArtRef = new ArrayList<>();
                    ImageRefJson img = new ImageRefJson();
                    img.url = getAlbumArtUri(albumId);
                    track.albumArtRef.add(img);

                    tracks.add(track);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) { e.printStackTrace(); }
        return tracks;
    }

    private List<TrackJson> querySongs(String selection, String[] selectionArgs, String sortOrder) {
        List<TrackJson> tracks = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        
        // Ensure it's music
        String finalSelection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        if (selection != null) {
            finalSelection += " AND " + selection;
        }

        try (Cursor cursor = contentResolver.query(uri, null, finalSelection, selectionArgs, sortOrder)) {
            if (cursor != null && cursor.moveToFirst()) {
                int idCol = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
                int titleCol = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                int artistCol = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                int albumCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
                int albumIdCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);

                do {
                    long id = cursor.getLong(idCol);
                    long albumId = cursor.getLong(albumIdCol);
                    
                    TrackJson track = new TrackJson();
                    track.nid = ContentUris.withAppendedId(uri, id).toString();
                    track.title = cursor.getString(titleCol);
                    track.artist = cursor.getString(artistCol);
                    track.album = cursor.getString(albumCol);
                    
                    track.albumArtRef = new ArrayList<>();
                    ImageRefJson img = new ImageRefJson();
                    img.url = getAlbumArtUri(albumId);
                    track.albumArtRef.add(img);

                    tracks.add(track);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) { e.printStackTrace(); }
        return tracks;
    }
}
