package com.example.finalproject.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.R;
import com.example.finalproject.model.Announcement;
import com.example.finalproject.superadmin.ManageAnnouncementsActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ManageAnnouncementsAdapter extends RecyclerView.Adapter<ManageAnnouncementsAdapter.ViewHolder> {

    private ArrayList<Announcement> announcementList;
    private ManageAnnouncementsActivity activity;

    public ManageAnnouncementsAdapter(ArrayList<Announcement> announcementList, ManageAnnouncementsActivity activity) {
        this.announcementList = announcementList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manage_announcement, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Announcement announcement = announcementList.get(position);
        holder.tvTitle.setText(announcement.getTitle());

        // Truncate long content
        String content = announcement.getContent();
        if (content.length() > 100) {
            holder.tvContent.setText(content.substring(0, 100) + "...");
        } else {
            holder.tvContent.setText(content);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
        holder.tvTimestamp.setText(sdf.format(new Date(announcement.getTimestamp())));

        holder.btnEdit.setOnClickListener(v -> activity.showAnnouncementDialog(announcement));
        holder.btnDelete.setOnClickListener(v -> activity.deleteAnnouncement(announcement));
    }

    @Override
    public int getItemCount() {
        return announcementList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvTimestamp;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvAnnouncementTitle);
            tvContent = itemView.findViewById(R.id.tvAnnouncementContent);
            tvTimestamp = itemView.findViewById(R.id.tvAnnouncementTimestamp);
            btnEdit = itemView.findViewById(R.id.btnEditAnnouncement);
            btnDelete = itemView.findViewById(R.id.btnDeleteAnnouncement);
        }
    }
}
