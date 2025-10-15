package com.example.finalproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.AnnouncementViewHolder> {

    private final ArrayList<Announcement> announcementList;

    public AnnouncementAdapter(ArrayList<Announcement> announcementList) {
        this.announcementList = announcementList;
    }

    @NonNull
    @Override
    public AnnouncementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_announcement, parent, false);
        return new AnnouncementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnnouncementViewHolder holder, int position) {
        Announcement announcement = announcementList.get(position);
        if (announcement == null) return;

        holder.tvAnnouncementTitle.setText(announcement.getTitle());
        holder.tvAnnouncementContent.setText(announcement.getContent());

        if (announcement.getTimestamp() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault());
            holder.tvAnnouncementTimestamp.setText(sdf.format(new Date(announcement.getTimestamp())));
        }
    }

    @Override
    public int getItemCount() {
        return announcementList != null ? announcementList.size() : 0;
    }

    public static class AnnouncementViewHolder extends RecyclerView.ViewHolder {
        TextView tvAnnouncementTitle, tvAnnouncementContent, tvAnnouncementTimestamp;

        public AnnouncementViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAnnouncementTitle = itemView.findViewById(R.id.tvAnnouncementTitle);
            tvAnnouncementContent = itemView.findViewById(R.id.tvAnnouncementContent);
            tvAnnouncementTimestamp = itemView.findViewById(R.id.tvAnnouncementTimestamp);
        }
    }
}
