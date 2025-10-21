package com.example.finalproject.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.R;
import com.example.finalproject.model.Admin;
import com.example.finalproject.superadmin.ManageAdminsActivity;

import java.util.ArrayList;

public class AdminAdapter extends RecyclerView.Adapter<AdminAdapter.ViewHolder> {

    private ArrayList<Admin> adminList;
    private ManageAdminsActivity activity;

    public AdminAdapter(ArrayList<Admin> adminList, ManageAdminsActivity activity) {
        this.adminList = adminList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Admin admin = adminList.get(position);
        holder.tvAdminName.setText(admin.getFirstName() + " " + admin.getLastName());
        holder.tvAdminEmail.setText(admin.getEmail());
        holder.btnDeleteAdmin.setOnClickListener(v -> activity.deleteAdmin(admin));
    }

    @Override
    public int getItemCount() {
        return adminList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAdminName, tvAdminEmail;
        Button btnDeleteAdmin;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAdminName = itemView.findViewById(R.id.tvAdminName);
            tvAdminEmail = itemView.findViewById(R.id.tvAdminEmail);
            btnDeleteAdmin = itemView.findViewById(R.id.btnDeleteAdmin);
        }
    }
}
