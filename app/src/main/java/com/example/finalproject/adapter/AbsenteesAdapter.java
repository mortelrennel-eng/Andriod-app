package com.example.finalproject.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.R;

import java.util.ArrayList;

public class AbsenteesAdapter extends RecyclerView.Adapter<AbsenteesAdapter.ViewHolder> {

    // This is the data model for each item in the list
    public static class AbsenteeRecord {
        String studentName;
        int absenceCount;

        public AbsenteeRecord(String studentName, int absenceCount) {
            this.studentName = studentName;
            this.absenceCount = absenceCount;
        }
    }

    private ArrayList<AbsenteeRecord> absenteeList;

    public AbsenteesAdapter(ArrayList<AbsenteeRecord> absenteeList) {
        this.absenteeList = absenteeList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_absentee, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AbsenteeRecord record = absenteeList.get(position);
        holder.tvStudentName.setText(record.studentName);
        holder.tvAbsenceCount.setText("Absences: " + record.absenceCount);
    }

    @Override
    public int getItemCount() {
        return absenteeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudentName, tvAbsenceCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvAbsenceCount = itemView.findViewById(R.id.tvAbsenceCount);
        }
    }
}
