package com.example.finalproject;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class SimpleListAdapter extends ArrayAdapter<String> {
    public SimpleListAdapter(Context context, List<String> items) {
        super(context, android.R.layout.simple_list_item_1, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView v = (TextView) super.getView(position, convertView, parent);
        v.setPadding(16, 16, 16, 16);
        return v;
    }
}
