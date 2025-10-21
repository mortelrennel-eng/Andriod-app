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
import com.example.finalproject.admin.ViewMySectionActivity;

import java.util.ArrayList;

public class RemovableStudentAdapter extends RecyclerView.Adapter<RemovableStudentAdapter.ViewHolder> {

    private ArrayList<User> studentList;
    private ViewMySectionActivity activity;

    public RemovableStudentAdapter(ArrayList<User> studentList, ViewMySectionActivity activity) {
        this.studentList = studentList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_simple, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User student = studentList.get(position);
        holder.tvUserName.setText(student.getFirstName() + " " + student.getLastName());
        holder.tvUserRole.setText("Student");

        if (holder.btnRemove != null) {
            holder.btnRemove.setOnClickListener(v -> {
                if (activity != null) {
                    activity.removeStudentFromSection(student);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvUserRole;
        Button btnRemove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserRole = itemView.findViewById(R.id.tvUserRole);
            btnRemove = itemView.findViewById(R.id.btnRemoveStudent);
        }
    }
}
