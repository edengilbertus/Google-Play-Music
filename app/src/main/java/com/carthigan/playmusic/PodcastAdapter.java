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

public class PodcastAdapter extends RecyclerView.Adapter<PodcastAdapter.PodcastViewHolder> {

    private List<TrackJson> items = new ArrayList<>();
    private final OnItemClickListener clickListener;

    public interface OnItemClickListener {
        void onItemClick(TrackJson item);
    }

    public PodcastAdapter(OnItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setItems(List<TrackJson> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PodcastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_podcast, parent, false);
        return new PodcastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PodcastViewHolder holder, int position) {
        TrackJson item = items.get(position);
        
        holder.tvTitle.setText(item.title != null ? item.title : "Unknown Episode");
        
        String publisherAndDuration = (item.artist != null ? item.artist : "Unknown Publisher") + " • 45 min";
        holder.tvPublisher.setText(publisherAndDuration);

        if (item.albumArtRef != null && !item.albumArtRef.isEmpty() && item.albumArtRef.get(0).url != null) {
            Glide.with(holder.itemView.getContext())
                    .load(item.albumArtRef.get(0).url)
                    .centerCrop()
                    .placeholder(R.drawable.placeholder_music)
                    .error(R.drawable.placeholder_music)
                    .into(holder.ivArt);
        } else {
            holder.ivArt.setImageResource(R.drawable.placeholder_music);
        }

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class PodcastViewHolder extends RecyclerView.ViewHolder {
        ImageView ivArt;
        ImageView ivInfo;
        TextView tvTitle;
        TextView tvPublisher;

        public PodcastViewHolder(@NonNull View itemView) {
            super(itemView);
            ivArt = itemView.findViewById(R.id.iv_podcast_art);
            ivInfo = itemView.findViewById(R.id.iv_podcast_info);
            tvTitle = itemView.findViewById(R.id.tv_podcast_title);
            tvPublisher = itemView.findViewById(R.id.tv_podcast_publisher);
        }
    }
}
