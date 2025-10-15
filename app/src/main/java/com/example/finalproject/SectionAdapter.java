package com.example.finalproject;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SectionAdapter extends RecyclerView.Adapter<SectionAdapter.SectionViewHolder> {

    private ArrayList<Section> sectionList;
    private ManageSectionsActivity activity;

    public SectionAdapter(ArrayList<Section> sectionList, ManageSectionsActivity activity) {
        this.sectionList = sectionList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public SectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_section, parent, false);
        return new SectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SectionViewHolder holder, int position) {
        Section section = sectionList.get(position);
        holder.tvSectionName.setText(section.getName());
        holder.tvManagedBy.setText("Managed by: " + section.getManagedBy());

        holder.btnViewStudents.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ViewStudentsInSectionActivity.class);
            intent.putExtra("SECTION_NAME", section.getName());
            v.getContext().startActivity(intent);
        });

        holder.btnAddStudents.setOnClickListener(v -> {
            activity.addStudentsToSection(section.getName());
        });

        holder.btnDeleteSection.setOnClickListener(v -> {
            activity.deleteSection(section.getName());
        });
    }

    @Override
    public int getItemCount() {
        return sectionList.size();
    }

    public static class SectionViewHolder extends RecyclerView.ViewHolder {
        TextView tvSectionName, tvManagedBy;
        Button btnViewStudents, btnAddStudents, btnDeleteSection;

        public SectionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSectionName = itemView.findViewById(R.id.tvSectionName);
            tvManagedBy = itemView.findViewById(R.id.tvManagedBy);
            btnViewStudents = itemView.findViewById(R.id.btnViewStudents);
            btnAddStudents = itemView.findViewById(R.id.btnAddStudents);
            btnDeleteSection = itemView.findViewById(R.id.btnDeleteSection);
        }
    }
}
