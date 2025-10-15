package com.example.finalproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class EbookManagerAdapter extends RecyclerView.Adapter<EbookManagerAdapter.EbookViewHolder> {

    private ArrayList<Ebook> ebookList;
    private EbookManagerActivity activity;

    public EbookManagerAdapter(ArrayList<Ebook> ebookList, EbookManagerActivity activity) {
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

        holder.btnViewEbook.setOnClickListener(v -> {
            try { activity.viewEbook(ebook); } catch (Exception ex) { android.util.Log.e("EbookAdapter","view",ex); }
        });
        holder.btnEditEbook.setOnClickListener(v -> {
            try { activity.editEbook(ebook); } catch (Exception ex) { android.util.Log.e("EbookAdapter","edit",ex); }
        });
        holder.btnDeleteEbook.setOnClickListener(v -> {
            try { activity.deleteEbook(ebook); } catch (Exception ex) { android.util.Log.e("EbookAdapter","delete",ex); }
        });
    }

    @Override
    public int getItemCount() {
        return ebookList.size();
    }

    public static class EbookViewHolder extends RecyclerView.ViewHolder {
        TextView tvEbookTitle;
        Button btnViewEbook, btnEditEbook, btnDeleteEbook;

        public EbookViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEbookTitle = itemView.findViewById(R.id.tvEbookTitle);
            btnViewEbook = itemView.findViewById(R.id.btnViewEbook);
            btnEditEbook = itemView.findViewById(R.id.btnEditEbook);
            btnDeleteEbook = itemView.findViewById(R.id.btnDeleteEbook);
        }
    }
}
