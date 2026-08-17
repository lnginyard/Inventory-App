package com.example.cs360inventoryapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying inventory items in a ListView / Grid table.
 * Supports updating quantity values (+/-) and deleting items directly from SQLite.
 */
public class InventoryAdapter extends BaseAdapter {

    public interface OnItemActionListener {
        void onQuantityChanged(InventoryItem item, int newQuantity);
        void onItemDeleted(InventoryItem item);
    }

    private final Context context;
    private List<InventoryItem> itemList = new ArrayList<>();
    private final OnItemActionListener actionListener;
    private final LayoutInflater inflater;

    public InventoryAdapter(Context context, OnItemActionListener actionListener) {
        this.context = context;
        this.actionListener = actionListener;
        this.inflater = LayoutInflater.from(context);
    }

    public void setItems(List<InventoryItem> items) {
        this.itemList = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Object getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return itemList.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.inventory_row, parent, false);
            holder = new ViewHolder();
            holder.textViewRowItemName = convertView.findViewById(R.id.textViewRowItemName);
            holder.textViewRowQuantity = convertView.findViewById(R.id.textViewRowQuantity);
            holder.buttonDecreaseQuantity = convertView.findViewById(R.id.buttonDecreaseQuantity);
            holder.buttonIncreaseQuantity = convertView.findViewById(R.id.buttonIncreaseQuantity);
            holder.buttonDeleteItem = convertView.findViewById(R.id.buttonDeleteItem);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final InventoryItem item = itemList.get(position);
        holder.textViewRowItemName.setText(item.getName());
        holder.textViewRowQuantity.setText(String.valueOf(item.getQuantity()));

        // Highlight zero stock in red
        if (item.getQuantity() == 0) {
            holder.textViewRowQuantity.setTextColor(0xFFEB5757); // Red alert
        } else {
            holder.textViewRowQuantity.setTextColor(0xFF1F2933);
        }

        holder.buttonDecreaseQuantity.setOnClickListener(v -> {
            int current = item.getQuantity();
            if (current > 0 && actionListener != null) {
                actionListener.onQuantityChanged(item, current - 1);
            }
        });

        holder.buttonIncreaseQuantity.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onQuantityChanged(item, item.getQuantity() + 1);
            }
        });

        holder.buttonDeleteItem.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onItemDeleted(item);
            }
        });

        return convertView;
    }

    private static class ViewHolder {
        TextView textViewRowItemName;
        TextView textViewRowQuantity;
        Button buttonDecreaseQuantity;
        Button buttonIncreaseQuantity;
        Button buttonDeleteItem;
    }
}
