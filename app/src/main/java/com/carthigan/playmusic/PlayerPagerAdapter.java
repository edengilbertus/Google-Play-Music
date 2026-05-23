package com.carthigan.playmusic;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.carthigan.playmusic.api.models.TrackJson;

import java.util.List;

public class PlayerPagerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ART = 0;
    private static final int TYPE_QUEUE = 1;

    private TrackJson currentTrack;
    private List<TrackJson> queue;
    private TrackAdapter.OnTrackClickListener queueClickListener;

    public PlayerPagerAdapter(TrackAdapter.OnTrackClickListener queueClickListener) {
        this.queueClickListener = queueClickListener;
    }

    public void updateData(TrackJson currentTrack, List<TrackJson> queue) {
        this.currentTrack = currentTrack;
        this.queue = queue;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? TYPE_ART : TYPE_QUEUE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ART) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_player_art, parent, false);
            return new ArtViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_player_queue, parent, false);
            return new QueueViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ArtViewHolder) {
            ArtViewHolder artHolder = (ArtViewHolder) holder;
            if (currentTrack != null && currentTrack.albumArtRef != null && !currentTrack.albumArtRef.isEmpty()) {
                Glide.with(artHolder.itemView.getContext())
                        .load(currentTrack.albumArtRef.get(0).url)
                        .centerCrop()
                        .placeholder(R.drawable.songs_cover)
                        .error(R.drawable.songs_cover)
                        .into(artHolder.ivArt);
            } else {
                artHolder.ivArt.setImageResource(R.drawable.songs_cover);
            }
        } else if (holder instanceof QueueViewHolder) {
            QueueViewHolder queueHolder = (QueueViewHolder) holder;
            if (queueHolder.adapter == null) {
                queueHolder.adapter = new TrackAdapter(queueClickListener, null);
                queueHolder.rvQueue.setLayoutManager(new LinearLayoutManager(queueHolder.itemView.getContext()));
                queueHolder.rvQueue.setAdapter(queueHolder.adapter);
            }
            queueHolder.adapter.setTracks(queue);
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Art page + Queue page
    }

    static class ArtViewHolder extends RecyclerView.ViewHolder {
        ImageView ivArt;

        ArtViewHolder(View itemView) {
            super(itemView);
            ivArt = itemView.findViewById(R.id.vp_album_art);
        }
    }

    static class QueueViewHolder extends RecyclerView.ViewHolder {
        RecyclerView rvQueue;
        TrackAdapter adapter;

        QueueViewHolder(View itemView) {
            super(itemView);
            rvQueue = itemView.findViewById(R.id.rv_up_next);
        }
    }
}