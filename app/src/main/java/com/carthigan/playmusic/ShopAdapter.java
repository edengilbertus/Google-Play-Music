package com.carthigan.playmusic;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.carthigan.playmusic.api.models.TrackJson;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.ShopViewHolder> {

    private List<TrackJson> shopItems = new ArrayList<>();

    public void setItems(List<TrackJson> items) {
        this.shopItems = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ShopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shop_card, parent, false);
        return new ShopViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShopViewHolder holder, int position) {
        TrackJson item = shopItems.get(position);
        holder.title.setText(item.title != null ? item.title : "Unknown Title");
        holder.artist.setText(item.artist != null ? item.artist : "Unknown Artist");
        
        // Mock prices based on position just for the prototype
        String price = (position % 2 == 0) ? "$9.99" : "$1.29";
        holder.price.setText(price);

        if (item.albumArtRef != null && !item.albumArtRef.isEmpty() && item.albumArtRef.get(0).url != null) {
            Glide.with(holder.itemView.getContext())
                    .load(item.albumArtRef.get(0).url)
                    .centerCrop()
                    .error(R.drawable.songs_cover)
                    .into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.songs_cover);
        }

        holder.price.setOnClickListener(v -> {
            Toast.makeText(holder.itemView.getContext(), "Buy " + item.title + " for " + price, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return shopItems.size();
    }

    static class ShopViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;
        TextView artist;
        TextView price;

        public ShopViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.shop_item_image);
            title = itemView.findViewById(R.id.shop_item_title);
            artist = itemView.findViewById(R.id.shop_item_artist);
            price = itemView.findViewById(R.id.shop_item_price);
        }
    }
}