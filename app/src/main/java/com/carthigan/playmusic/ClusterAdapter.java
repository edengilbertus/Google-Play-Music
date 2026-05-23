package com.carthigan.playmusic;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.carthigan.playmusic.api.models.TrackJson;

import java.util.ArrayList;
import java.util.List;

public class ClusterAdapter extends RecyclerView.Adapter<ClusterAdapter.ClusterViewHolder> {

    private List<Cluster> clusters = new ArrayList<>();
    private final NewReleaseCardAdapter.OnItemClickListener itemClickListener;

    public static class Cluster {
        public String title;
        public String subtitle;
        public boolean hasMore;
        public List<TrackJson> items;

        public Cluster(String title, String subtitle, boolean hasMore, List<TrackJson> items) {
            this.title = title;
            this.subtitle = subtitle;
            this.hasMore = hasMore;
            this.items = items;
        }
    }

    public ClusterAdapter(NewReleaseCardAdapter.OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void setClusters(List<Cluster> clusters) {
        this.clusters = clusters;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ClusterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cluster, parent, false);
        return new ClusterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClusterViewHolder holder, int position) {
        Cluster cluster = clusters.get(position);
        
        holder.tvTitle.setText(cluster.title);
        
        if (cluster.subtitle != null && !cluster.subtitle.isEmpty()) {
            holder.tvSubtitle.setVisibility(View.VISIBLE);
            holder.tvSubtitle.setText(cluster.subtitle);
        } else {
            holder.tvSubtitle.setVisibility(View.GONE);
        }

        if (cluster.hasMore) {
            holder.btnMore.setVisibility(View.VISIBLE);
        } else {
            holder.btnMore.setVisibility(View.GONE);
        }

        holder.adapter.setItems(cluster.items);
    }

    @Override
    public int getItemCount() {
        return clusters.size();
    }

    class ClusterViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvSubtitle;
        Button btnMore;
        RecyclerView rvContent;
        NewReleaseCardAdapter adapter;

        public ClusterViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_cluster_title);
            tvSubtitle = itemView.findViewById(R.id.tv_cluster_subtitle);
            btnMore = itemView.findViewById(R.id.btn_cluster_more);
            rvContent = itemView.findViewById(R.id.rv_cluster_content);

            rvContent.setLayoutManager(new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
            adapter = new NewReleaseCardAdapter(itemClickListener);
            rvContent.setAdapter(adapter);
        }
    }
}
