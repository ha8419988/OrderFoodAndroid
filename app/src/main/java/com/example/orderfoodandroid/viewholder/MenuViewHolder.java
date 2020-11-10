package com.example.orderfoodandroid.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.orderfoodandroid.Interface.ItemClickListener;
import com.example.orderfoodandroid.R;

public class MenuViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView tvMenuName;
    public ImageView imgMenu;
    private ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public MenuViewHolder(@NonNull View itemView) {
        super(itemView);
        tvMenuName = (TextView) itemView.findViewById(R.id.menu_name);
        imgMenu = (ImageView) itemView.findViewById(R.id.menu_imgage);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view, getAdapterPosition(), false);

    }

}
