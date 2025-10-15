package com.example.finalproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class StudentHistoryAdapter extends RecyclerView.Adapter<StudentHistoryAdapter.ViewHolder> {

    private ArrayList<AttendanceHistoryForStudentActivity.StudentAttendanceRecord> recordList;
    private AttendanceHistoryForStudentActivity activity;

    public StudentHistoryAdapter(ArrayList<AttendanceHistoryForStudentActivity.StudentAttendanceRecord> recordList, AttendanceHistoryForStudentActivity activity) {
        this.recordList = recordList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AttendanceHistoryForStudentActivity.StudentAttendanceRecord record = recordList.get(position);
        holder.tvSessionTitle.setText(record.sessionTitle);
        holder.tvDate.setText("Date: " + record.date);
        holder.tvStatus.setText("Status: " + record.status);

        holder.btnEdit.setOnClickListener(v -> activity.editRecord(record));
    }

    @Override
    public int getItemCount() {
        return recordList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSessionTitle, tvDate, tvStatus;
        Button btnEdit;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSessionTitle = itemView.findViewById(R.id.tvSessionTitle);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnEdit = itemView.findViewById(R.id.btnEditRecord);
        }
    }
}
