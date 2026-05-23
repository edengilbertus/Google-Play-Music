package com.carthigan.playmusic;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.carthigan.playmusic.api.models.TrackJson;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StationAdapter extends RecyclerView.Adapter<StationAdapter.StationViewHolder> {

    private List<TrackJson> items = new ArrayList<>();
    private final OnItemClickListener clickListener;
    private final String[] bgColors = {"#FF5722", "#E91E63", "#9C27B0", "#673AB7", "#3F51B5", "#00BCD4", "#009688", "#4CAF50"};

    public interface OnItemClickListener {
        void onItemClick(TrackJson item);
    }

    public StationAdapter(OnItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setItems(List<TrackJson> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_station_card, parent, false);
        return new StationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StationViewHolder holder, int position) {
        TrackJson item = items.get(position);
        
        holder.tvTitle.setText(item.title != null ? item.title : "");
        holder.tvSubtitle.setText(item.artist != null ? item.artist : "");

        // Assign a random vibrant color for the background
        int colorIndex = Math.abs((item.title != null ? item.title.hashCode() : position)) % bgColors.length;
        holder.cardView.setCardBackgroundColor(Color.parseColor(bgColors[colorIndex]));

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

    static class StationViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView ivPlayButton;
        TextView tvTitle;
        TextView tvSubtitle;

        public StationViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            ivPlayButton = itemView.findViewById(R.id.iv_station_play);
            tvTitle = itemView.findViewById(R.id.tv_station_title);
            tvSubtitle = itemView.findViewById(R.id.tv_station_subtitle);
        }
    }
}
