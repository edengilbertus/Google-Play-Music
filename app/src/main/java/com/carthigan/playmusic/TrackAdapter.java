package com.carthigan.playmusic;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.carthigan.playmusic.api.models.TrackJson;
import java.util.ArrayList;
import java.util.List;

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.TrackViewHolder> {

    private List<TrackJson> tracks = new ArrayList<>();
    private OnTrackClickListener listener;
    private OnTrackOptionsClickListener optionsListener;

    public interface OnTrackClickListener {
        void onTrackClick(TrackJson track);
    }
    
    public interface OnTrackOptionsClickListener {
        void onOptionsClick(TrackJson track);
    }

    public TrackAdapter(OnTrackClickListener listener, OnTrackOptionsClickListener optionsListener) {
        this.listener = listener;
        this.optionsListener = optionsListener;
    }

    public List<TrackJson> getTracks() {
        return tracks;
    }

    public void setTracks(List<TrackJson> tracks) {
        this.tracks = tracks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_track, parent, false);
        return new TrackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position) {
        TrackJson track = tracks.get(position);
        holder.bind(track);
    }

    @Override
    public int getItemCount() {
        return tracks.size();
    }

    class TrackViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAlbumArt;
        TextView tvTitle;
        TextView tvArtist;
        ImageView ivOptions;

        public TrackViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAlbumArt = itemView.findViewById(R.id.iv_album_art);
            tvTitle = itemView.findViewById(R.id.tv_track_title);
            tvArtist = itemView.findViewById(R.id.tv_track_artist);
            ivOptions = itemView.findViewById(R.id.iv_options);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onTrackClick(tracks.get(position));
                }
            });
            
            ivOptions.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && optionsListener != null) {
                    optionsListener.onOptionsClick(tracks.get(position));
                }
            });
        }

        public void bind(TrackJson track) {
            tvTitle.setText(track.title != null ? track.title : "Unknown Title");
            
            if (track.artist != null && !track.artist.isEmpty()) {
                tvArtist.setVisibility(View.VISIBLE);
                tvArtist.setText(track.artist);
            } else {
                tvArtist.setVisibility(View.GONE);
            }

            if (track.albumArtRef != null && !track.albumArtRef.isEmpty() && track.albumArtRef.get(0).url != null) {
                Glide.with(itemView.getContext())
                        .load(track.albumArtRef.get(0).url)
                        .centerCrop()
                        .placeholder(R.drawable.placeholder_music)
                        .error(R.drawable.placeholder_music)
                        .into(ivAlbumArt);
            } else {
                ivAlbumArt.setImageResource(R.drawable.placeholder_music);
            }
        }
    }
}
