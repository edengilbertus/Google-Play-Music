package com.carthigan.playmusic;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.carthigan.playmusic.api.models.ClusterJson;
import com.carthigan.playmusic.api.models.TrackJson;

import java.util.ArrayList;
import java.util.List;

public class ListenNowClusterAdapter extends RecyclerView.Adapter<ListenNowClusterAdapter.ClusterViewHolder> {

    private List<ClusterJson> clusters = new ArrayList<>();
    private final ListenNowCardAdapter.OnItemClickListener cardClickListener;

    public ListenNowClusterAdapter(ListenNowCardAdapter.OnItemClickListener cardClickListener) {
        this.cardClickListener = cardClickListener;
    }

    public void setClusters(List<ClusterJson> clusters) {
        this.clusters = clusters;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ClusterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_listen_now_cluster, parent, false);
        return new ClusterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClusterViewHolder holder, int position) {
        ClusterJson cluster = clusters.get(position);
        holder.tvTitle.setText(cluster.title != null ? cluster.title : "");
        
        if (cluster.subtitle != null && !cluster.subtitle.isEmpty()) {
            holder.tvSubtitle.setVisibility(View.VISIBLE);
            holder.tvSubtitle.setText(cluster.subtitle);
        } else {
            holder.tvSubtitle.setVisibility(View.GONE);
        }

        ListenNowCardAdapter cardAdapter = new ListenNowCardAdapter(cluster.items, cardClickListener);
        holder.rvItems.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
        holder.rvItems.setAdapter(cardAdapter);
    }

    @Override
    public int getItemCount() {
        return clusters.size();
    }

    static class ClusterViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvSubtitle;
        RecyclerView rvItems;

        public ClusterViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_cluster_title);
            tvSubtitle = itemView.findViewById(R.id.tv_cluster_subtitle);
            rvItems = itemView.findViewById(R.id.rv_cluster_items);
        }
    }
}
