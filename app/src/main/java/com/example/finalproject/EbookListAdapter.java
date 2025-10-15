package com.example.finalproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class EbookListAdapter extends RecyclerView.Adapter<EbookListAdapter.EbookViewHolder> {

    private ArrayList<Ebook> ebookList;
    private EbookListActivity activity;

    public EbookListAdapter(ArrayList<Ebook> ebookList, EbookListActivity activity) {
        this.ebookList = ebookList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public EbookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ebook, parent, false);
        return new EbookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EbookViewHolder holder, int position) {
        Ebook ebook = ebookList.get(position);
        holder.tvEbookTitle.setText(ebook.getTitle());
        holder.tvEbookCategory.setText("Category: " + ebook.getCategory());

        holder.btnOpen.setOnClickListener(v -> activity.openEbook(ebook));
    }

    @Override
    public int getItemCount() {
        return ebookList.size();
    }

    public static class EbookViewHolder extends RecyclerView.ViewHolder {
        TextView tvEbookTitle, tvEbookCategory;
        Button btnOpen;

        public EbookViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEbookTitle = itemView.findViewById(R.id.tvEbookTitle);
            tvEbookCategory = itemView.findViewById(R.id.tvEbookCategory);
            btnOpen = itemView.findViewById(R.id.btnOpenEbook);
        }
    }
}
