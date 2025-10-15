package com.example.finalproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class SectionAttendanceDetailAdapter extends RecyclerView.Adapter<SectionAttendanceDetailAdapter.ViewHolder> {

    private ArrayList<AttendanceListBySectionActivity.AttendanceRecord> attendanceList;
    private AttendanceListBySectionActivity activity;

    public SectionAttendanceDetailAdapter(ArrayList<AttendanceListBySectionActivity.AttendanceRecord> attendanceList, AttendanceListBySectionActivity activity) {
        this.attendanceList = attendanceList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attendance, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AttendanceListBySectionActivity.AttendanceRecord record = attendanceList.get(position);
        holder.tvSessionTitle.setText(record.getSessionTitle());
        holder.tvDate.setText("Date: " + record.getDate());
        holder.tvStudentName.setText(record.getStudentName());
        holder.tvStatus.setText("Status: " + record.getStatus());

        holder.btnEdit.setOnClickListener(v -> activity.editAttendance(record));
    }

    @Override
    public int getItemCount() {
        return attendanceList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSessionTitle, tvDate, tvStudentName, tvStatus;
        Button btnEdit;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSessionTitle = itemView.findViewById(R.id.tvSessionTitle);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnEdit = itemView.findViewById(R.id.btnEditAttendance);
        }
    }
}
