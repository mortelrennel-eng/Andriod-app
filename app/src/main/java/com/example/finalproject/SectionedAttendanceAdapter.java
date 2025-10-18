package com.example.finalproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SectionedAttendanceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private List<Object> itemList;

    public SectionedAttendanceAdapter(List<Object> itemList) {
        this.itemList = itemList;
    }

    @Override
    public int getItemViewType(int position) {
        if (itemList.get(position) instanceof SectionHeader) {
            return TYPE_HEADER;
        } else {
            return TYPE_ITEM;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_section_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_attendance_record, parent, false);
            return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == TYPE_HEADER) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            SectionHeader header = (SectionHeader) itemList.get(position);
            headerHolder.tvSectionHeader.setText(header.getTitle());
        } else {
            ItemViewHolder itemHolder = (ItemViewHolder) holder;
            AdminAttendanceRecord record = (AdminAttendanceRecord) itemList.get(position);
            itemHolder.tvStudentName.setText(record.studentName);
            itemHolder.tvStatus.setText(record.status);
            itemHolder.tvDate.setText(record.date + " - " + record.sessionTitle);
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    // ViewHolder for Section Headers
    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvSectionHeader;
        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSectionHeader = itemView.findViewById(R.id.tvSectionHeader);
        }
    }

    // ViewHolder for Attendance Items
    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudentName, tvStatus, tvDate;
        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}
