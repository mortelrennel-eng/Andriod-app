package com.example.finalproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class SectionAdapter extends RecyclerView.Adapter<SectionAdapter.ViewHolder> {

    private ArrayList<Section> sectionList;
    private ManageSectionsActivity activity;

    public SectionAdapter(ArrayList<Section> sectionList, ManageSectionsActivity activity) {
        this.sectionList = sectionList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_section, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Section section = sectionList.get(position);
        holder.tvSectionName.setText(section.getSectionName());
        holder.tvManagedBy.setText("Managed by: " + section.getManagedBy());

        // --- Connect buttons to the methods in ManageSectionsActivity ---
        holder.btnViewStudents.setOnClickListener(v -> activity.viewStudentsInSection(section.getSectionName()));
        holder.btnAddStudents.setOnClickListener(v -> activity.addStudentsToSection(section.getSectionName()));
        holder.btnDeleteSection.setOnClickListener(v -> activity.deleteSection(section.getSectionName()));
    }

    @Override
    public int getItemCount() {
        return sectionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSectionName, tvManagedBy;
        Button btnViewStudents, btnAddStudents, btnDeleteSection;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSectionName = itemView.findViewById(R.id.tvSectionName);
            tvManagedBy = itemView.findViewById(R.id.tvManagedBy);
            btnViewStudents = itemView.findViewById(R.id.btnViewStudents);
            btnAddStudents = itemView.findViewById(R.id.btnAddStudents);
            btnDeleteSection = itemView.findViewById(R.id.btnDeleteSection);
        }
    }
}
