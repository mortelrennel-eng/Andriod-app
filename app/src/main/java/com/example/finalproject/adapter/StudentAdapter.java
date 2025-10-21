package com.example.finalproject.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.R;
import com.example.finalproject.model.User;

import java.util.ArrayList;

// This adapter now uses the Listener interface for flexibility
public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.ViewHolder> {

    private ArrayList<User> studentList;
    private StudentAdapterListener listener; // Use the interface as the type

    public StudentAdapter(ArrayList<User> studentList, StudentAdapterListener listener) {
        this.studentList = studentList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User student = studentList.get(position);
        holder.tvStudentName.setText(student.getFirstName() + " " + student.getLastName());
        holder.tvStudentId.setText("ID: " + student.getStudentId());

        // Call the methods from the listener interface
        holder.btnEdit.setOnClickListener(v -> listener.editStudent(student));
        holder.btnDelete.setOnClickListener(v -> listener.deleteStudent(student));
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudentName, tvStudentId;
        Button btnView, btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvStudentId = itemView.findViewById(R.id.tvStudentId);
            btnView = itemView.findViewById(R.id.btnViewStudent);
            btnEdit = itemView.findViewById(R.id.btnEditStudent);
            btnDelete = itemView.findViewById(R.id.btnDeleteStudent);
        }
    }
}
