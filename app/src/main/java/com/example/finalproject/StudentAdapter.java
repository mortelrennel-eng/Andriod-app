package com.example.finalproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    private ArrayList<User> studentList;
    private StudentListBySectionActivity activity;

    public StudentAdapter(ArrayList<User> studentList, StudentListBySectionActivity activity) {
        this.studentList = studentList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        User student = studentList.get(position);
        holder.tvStudentName.setText(student.getFirstName() + " " + student.getLastName());
        holder.tvStudentId.setText("ID: " + student.getStudentId());

        holder.btnView.setOnClickListener(v -> activity.viewStudent(student));
        holder.btnEdit.setOnClickListener(v -> activity.editStudent(student));
        holder.btnDelete.setOnClickListener(v -> activity.deleteStudent(student));
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    public static class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudentName, tvStudentId;
        Button btnView, btnEdit, btnDelete;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvStudentId = itemView.findViewById(R.id.tvStudentId);
            btnView = itemView.findViewById(R.id.btnViewStudent);
            btnEdit = itemView.findViewById(R.id.btnEditStudent);
            btnDelete = itemView.findViewById(R.id.btnDeleteStudent);
        }
    }
}
