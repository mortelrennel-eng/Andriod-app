package com.example.finalproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AdminAdapter extends RecyclerView.Adapter<AdminAdapter.AdminViewHolder> {

    private ArrayList<Admin> adminList;
    private ManageAdminsActivity activity;

    public AdminAdapter(ArrayList<Admin> adminList, ManageAdminsActivity activity) {
        this.adminList = adminList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public AdminViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        // Ensure R.layout.item_admin_row exists and is correctly defined
        View view = inflater.inflate(R.layout.item_admin_row, parent, false);
        return new AdminViewHolder(view, activity, this);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminViewHolder holder, int position) {
        Admin admin = adminList.get(position);
        holder.tvAdminName.setText(admin.getName());
        holder.tvAdminEmail.setText(admin.getEmail());
    }

    @Override
    public int getItemCount() {
        return adminList.size();
    }

    public void removeItem(int position) {
        adminList.remove(position);
        notifyItemRemoved(position);
    }

    public static class AdminViewHolder extends RecyclerView.ViewHolder {
        TextView tvAdminName;
        TextView tvAdminEmail;
        Button btnDeleteAdmin;
        ManageAdminsActivity activity;
        AdminAdapter adapter;

        public AdminViewHolder(@NonNull View itemView, ManageAdminsActivity activity, AdminAdapter adapter) {
            super(itemView);
            this.activity = activity;
            this.adapter = adapter;

            tvAdminName = itemView.findViewById(R.id.tvAdminName);
            tvAdminEmail = itemView.findViewById(R.id.tvAdminEmail);
            btnDeleteAdmin = itemView.findViewById(R.id.btnDeleteAdmin);

            btnDeleteAdmin.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Admin adminToDelete = adapter.adminList.get(position);
                    activity.deleteAdmin(adminToDelete);
                    // Optionally remove from UI immediately if delete is successful
                    // adapter.removeItem(position);
                }
            });
        }
    }
}
