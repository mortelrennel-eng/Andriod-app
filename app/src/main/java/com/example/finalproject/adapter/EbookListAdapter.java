package com.example.finalproject.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.R;
import com.example.finalproject.model.Ebook;
import com.example.finalproject.student.EbookListActivity;

import java.util.ArrayList;

public class EbookListAdapter extends RecyclerView.Adapter<EbookListAdapter.ViewHolder> {

    private ArrayList<Ebook> ebookList;
    private EbookListActivity activity;

    public EbookListAdapter(ArrayList<Ebook> ebookList, EbookListActivity activity) {
        this.ebookList = ebookList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ebook, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Ebook ebook = ebookList.get(position);
        holder.tvTitle.setText(ebook.getTitle());
        holder.tvCategory.setText("Category: " + ebook.getCategory());
        holder.btnOpen.setOnClickListener(v -> activity.openEbook(ebook));
    }

    @Override
    public int getItemCount() {
        return ebookList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCategory;
        Button btnOpen;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvEbookTitle);
            tvCategory = itemView.findViewById(R.id.tvEbookCategory);
            btnOpen = itemView.findViewById(R.id.btnOpenEbook);
        }
    }
}
