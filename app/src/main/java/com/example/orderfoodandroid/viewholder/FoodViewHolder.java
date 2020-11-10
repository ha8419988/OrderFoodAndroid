package com.example.orderfoodandroid.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.orderfoodandroid.Interface.ItemClickListener;
import com.example.orderfoodandroid.R;

public class FoodViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView food_name, food_price;
    public ImageView food_image, fav_image, quick_cart;
    private ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public FoodViewHolder(@NonNull View itemView) {
        super(itemView);
        food_name = (TextView) itemView.findViewById(R.id.food_name);
        food_image = (ImageView) itemView.findViewById(R.id.food_imgage);
        food_price = itemView.findViewById(R.id.food_price1);
        fav_image = (ImageView) itemView.findViewById(R.id.img_food_favorite);
        quick_cart = itemView.findViewById(R.id.btn_quick_cart);
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view, getAdapterPosition(), false);
    }
}
