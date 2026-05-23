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

public class NewReleaseCardAdapter extends RecyclerView.Adapter<NewReleaseCardAdapter.CardViewHolder> {

    private List<TrackJson> items = new ArrayList<>();
    private final OnItemClickListener clickListener;

    public interface OnItemClickListener {
        void onItemClick(TrackJson item);
    }

    public NewReleaseCardAdapter(OnItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setItems(List<TrackJson> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_new_release_card, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        TrackJson item = items.get(position);
        
        holder.tvTitle.setText(item.title != null ? item.title : "");
        if (item.artist != null && !item.artist.isEmpty()) {
            holder.tvSubtitle.setVisibility(View.VISIBLE);
            holder.tvSubtitle.setText(item.artist);
        } else {
            holder.tvSubtitle.setVisibility(View.GONE);
        }

        if (item.albumArtRef != null && !item.albumArtRef.isEmpty() && item.albumArtRef.get(0).url != null) {
            Glide.with(holder.itemView.getContext())
                    .load(item.albumArtRef.get(0).url)
                    .centerCrop()
                    .placeholder(R.drawable.placeholder_music)
                    .error(R.drawable.placeholder_music)
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(R.drawable.placeholder_music);
        }

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(item);
            }
        });
        
        holder.ivPlayButton.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        ImageView ivPlayButton;
        ImageView ivOverflow;
        TextView tvTitle;
        TextView tvSubtitle;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_card_image);
            ivPlayButton = itemView.findViewById(R.id.iv_play_button);
            ivOverflow = itemView.findViewById(R.id.iv_overflow);
            tvTitle = itemView.findViewById(R.id.tv_card_title);
            tvSubtitle = itemView.findViewById(R.id.tv_card_subtitle);
        }
    }
}
