package com.example.adrian.cloudclientv3;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.adrian.cloudclientv3.model.Item;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.MyViewHolder> {

    private List<Item> items;

    public ItemAdapter(List<Item> items) {
        this.items = items;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView txtItemName;
        private Button btnItemAction;

        public MyViewHolder(LinearLayout layout) {
            super(layout);
            txtItemName = layout.findViewById(R.id.txt_item_name);
            btnItemAction = layout.findViewById(R.id.btn_item_action);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LinearLayout layout = (LinearLayout) LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_item, viewGroup, false);

        MyViewHolder holder = new MyViewHolder(layout);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ItemAdapter.MyViewHolder holder, int i) {
        holder.txtItemName.setText(items.get(i).getItemName());

        holder.btnItemAction.setText(items.get(i).getBtnName());
        holder.btnItemAction.setOnClickListener(items.get(i).getOnClick());
    }

    @Override
    public int getItemCount() {
        return (items == null) ? 0 : items.size();
    }
}