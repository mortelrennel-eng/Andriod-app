package com.example.finalproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class EbookAdapter extends RecyclerView.Adapter<EbookAdapter.EbookViewHolder> {

    private ArrayList<Ebook> ebookList;
    private EbookManagerActivity activity;

    public EbookAdapter(ArrayList<Ebook> ebookList, EbookManagerActivity activity) {
        this.ebookList = ebookList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public EbookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ebook_manage, parent, false);
        return new EbookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EbookViewHolder holder, int position) {
        Ebook ebook = ebookList.get(position);
        holder.tvEbookTitle.setText(ebook.getTitle());
        holder.tvEbookCategory.setText("Category: " + ebook.getCategory());

        holder.btnView.setOnClickListener(v -> activity.viewEbook(ebook));
        holder.btnEdit.setOnClickListener(v -> activity.editEbook(ebook));
        holder.btnDelete.setOnClickListener(v -> activity.deleteEbook(ebook));
    }

    @Override
    public int getItemCount() {
        return ebookList.size();
    }

    public static class EbookViewHolder extends RecyclerView.ViewHolder {
        TextView tvEbookTitle, tvEbookCategory;
        Button btnView, btnEdit, btnDelete;

        public EbookViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEbookTitle = itemView.findViewById(R.id.tvEbookTitle);
            tvEbookCategory = itemView.findViewById(R.id.tvEbookCategory);
            btnView = itemView.findViewById(R.id.btnViewEbook);
            btnEdit = itemView.findViewById(R.id.btnEditEbook);
            btnDelete = itemView.findViewById(R.id.btnDeleteEbook);
        }
    }
}
