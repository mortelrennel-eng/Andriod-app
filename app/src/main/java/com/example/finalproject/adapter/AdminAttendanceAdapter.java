package com.example.finalproject.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.R;
import com.example.finalproject.model.AdminAttendanceRecord;

import java.util.ArrayList;

public class AdminAttendanceAdapter extends RecyclerView.Adapter<AdminAttendanceAdapter.ViewHolder> {

    private ArrayList<AdminAttendanceRecord> recordList;

    public AdminAttendanceAdapter(ArrayList<AdminAttendanceRecord> recordList) {
        this.recordList = recordList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_attendance_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdminAttendanceRecord record = recordList.get(position);
        holder.tvStudentName.setText(record.studentName);
        holder.tvStatus.setText("Status: " + record.status);
        holder.tvDate.setText(record.date + " - " + record.sessionTitle);
    }

    @Override
    public int getItemCount() {
        return recordList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudentName, tvStatus, tvDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}
