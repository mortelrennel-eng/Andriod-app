package com.example.finalproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private final ArrayList<AttendanceHistoryActivity.HistoryItem> historyList;

    public HistoryAdapter(ArrayList<AttendanceHistoryActivity.HistoryItem> historyList) {
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AttendanceHistoryActivity.HistoryItem item = historyList.get(position);
        holder.tvDate.setText(item.getDate());
        holder.tvAttendanceTitle.setText(item.getTitle());
        holder.tvTimeIn.setText("IN: " + item.getTimeIn());
        holder.tvTimeOut.setText("OUT: " + item.getTimeOut());
        holder.tvStatus.setText(item.getStatus());
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvAttendanceTitle, tvTimeIn, tvTimeOut, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvAttendanceTitle = itemView.findViewById(R.id.tvAttendanceTitle);
            tvTimeIn = itemView.findViewById(R.id.tvTimeIn);
            tvTimeOut = itemView.findViewById(R.id.tvTimeOut);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
