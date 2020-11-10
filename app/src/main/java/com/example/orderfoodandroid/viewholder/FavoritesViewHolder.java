package com.example.orderfoodandroid.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.orderfoodandroid.Interface.ItemClickListener;
import com.example.orderfoodandroid.R;


public class FavoritesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView food_name, food_price;
    public ImageView food_image, fav_image, quick_cart;

    private ItemClickListener itemClickListener;

    public RelativeLayout view_bg;
    public LinearLayout view_fg;

    public FavoritesViewHolder(@NonNull View itemView) {
        super(itemView);

        food_name = itemView.findViewById(R.id.food_name1);
        food_price = itemView.findViewById(R.id.food_price1);
        food_image = itemView.findViewById(R.id.food_image1);
        fav_image = itemView.findViewById(R.id.fav1);
        quick_cart = itemView.findViewById(R.id.btn_quick_cart1);

        view_bg = itemView.findViewById(R.id.view_background1);
        view_fg = itemView.findViewById(R.id.view_foreground1);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view, getAdapterPosition(), false);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
}