package com.example.orderfoodandroid;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.orderfoodandroid.Interface.ItemClickListener;
import com.example.orderfoodandroid.common.Common;
import com.example.orderfoodandroid.database.Database;
import com.example.orderfoodandroid.model.Category;
import com.example.orderfoodandroid.model.Favorites;
import com.example.orderfoodandroid.model.Food;
import com.example.orderfoodandroid.model.Order;
import com.example.orderfoodandroid.viewholder.FoodViewHolder;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import java.util.List;

public class FoodList extends AppCompatActivity {
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference foodlist;
    String categoryId = "";
    FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;
    //search funct
    FirebaseRecyclerAdapter<Food, FoodViewHolder> searchAdapter;
    List<String> suggetList = new ArrayList<>();
    MaterialSearchBar materialSearchBar;
    //Favorite
    Database localDB;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);

        //Init Firebase
        database = FirebaseDatabase.getInstance();
        foodlist = database.getReference("Foods");

        recyclerView = findViewById(R.id.recycler_food);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        //local DB
        localDB = new Database(this);
        //get Intent
        if (getIntent() != null)
            categoryId = getIntent().getStringExtra("CategoryId");

        if (!categoryId.isEmpty() && categoryId != null) {
            loadListFood(categoryId);
        }
        // SearchBar
        materialSearchBar = (MaterialSearchBar) findViewById(R.id.searchBar);
        materialSearchBar.setHint("Nhập Tên Món Ăn");
        loadSuggest();//write function to load Suggest from Firebase
        materialSearchBar.setLastSuggestions(suggetList);//lấy giá trị từ suggest list
        materialSearchBar.setCardViewElevation(10);
        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                List<String> suggest = new ArrayList<String>();
                for (String search : suggetList) //loopin sugget list
                {
                    //ktra xem char cua Firebase co chua char cua search o dang chu LowerCase
                    if (search.toLowerCase().contains(materialSearchBar.getText().toLowerCase())) {
                        suggest.add(search);//tra ve dong nao co Char do
                    }
                }
                materialSearchBar.setLastSuggestions(suggest);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        //enable search bar callback
        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
           //when Search bar close//restore origin adapter
                if (!enabled)
                    recyclerView.setAdapter(adapter);
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                //when search bar finish
                //show result of search adapter
                startSearch(text);
            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });

    }

    private void startSearch(CharSequence text) {
        searchAdapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(
                Food.class,
                R.layout.food_item,
                FoodViewHolder.class,
                foodlist.orderByChild("name").equalTo(text.toString()) //compare name vs kí tự search

        ) {
            @Override
            protected void populateViewHolder(final FoodViewHolder foodViewHolder, final Food food, final int i) {
                foodViewHolder.food_name.setText(food.getName());
                Picasso.with(getBaseContext()).load(food.getImage()).into(foodViewHolder.food_image);
                final Food food1 = food;
                //bat su kien khi click vao Food_item
                foodViewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int posittion, boolean isLongClick) {
                        Intent intent = new Intent(FoodList.this, FoodDetail.class);
                        intent.putExtra("FoodId", searchAdapter.getRef(posittion).getKey());//send foodId
                        startActivity(intent);
                    }
                });
            }
        };
        recyclerView.setAdapter(searchAdapter);//setadapter for recylce view search result
    }

    private void loadSuggest() {
        foodlist.orderByChild("menuId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            Food item = postSnapshot.getValue(Food.class);
                            suggetList.add(item.getName());//them ten mon an vao suggetList
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void loadListFood(String categoryId) {
        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(Food.class, R.layout.food_item,
                FoodViewHolder.class,
                foodlist.orderByChild("menuId").equalTo(categoryId)//giong nhu Select *from Food where MenuId=CategoryId
        ) {
            @Override
            protected void populateViewHolder(final FoodViewHolder viewHolder, final Food model, final int position) {
                viewHolder.food_name.setText(model.getName());
                viewHolder.food_price.setText(String.format("VNĐ : %s", model.getPrice().toString()));
                Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.food_image);

                //quick cart

                viewHolder.quick_cart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        boolean isExists = new Database(getBaseContext()).checkFoodExists(adapter.getRef(position).getKey(),

                                Common.currentUser.getPhone());
                        if (!isExists) {
                            new Database(getBaseContext()).addToCart(new Order(
                                    Common.currentUser.getPhone(),
                                    adapter.getRef(position).getKey(),
                                    model.getName(),
                                    "1",
                                    model.getPrice(),
                                    model.getDiscount(),
                                    model.getImage()


                            ));
                        } else {
                            new Database(getBaseContext()).increaseCart(Common.currentUser.getPhone(), adapter.getRef(position).getKey());

                        }
                        Toast.makeText(FoodList.this, " Đã Thêm  Vào Giỏ Hàng", Toast.LENGTH_SHORT).show();
                    }


                });

                if (localDB.isFavorites(adapter.getRef(position).getKey(), Common.currentUser.getPhone()))
                    viewHolder.fav_image.setImageResource(R.drawable.favorite_solid);

                viewHolder.fav_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Favorites favorites = new Favorites();
                        favorites.setFoodId(adapter.getRef(position).getKey());
                        favorites.setFoodName(model.getName());
                        favorites.setFoodDescription(model.getDescription());
                        favorites.setFoodDiscount(model.getDiscount());
                        favorites.setFoodImage(model.getImage());
                        favorites.setFoodMenuId(model.getMenuId());
                        favorites.setUserPhone(Common.currentUser.getPhone());
                        favorites.setFoodPrice(model.getPrice());

                        if (!localDB.isFavorites(adapter.getRef(position).getKey(), Common.currentUser.getPhone())) {
                            localDB.addToFavorites(favorites);
                            viewHolder.fav_image.setImageResource(R.drawable.favorite_solid);
                            Toast.makeText(FoodList.this, "" + model.getName() + " Đã thêm vào mục yêu thích", Toast.LENGTH_SHORT).show();
                        } else {
                            localDB.removeFromFavorites(adapter.getRef(position).getKey(), Common.currentUser.getPhone());
                            viewHolder.fav_image.setImageResource(R.drawable.favorite);
                            Toast.makeText(FoodList.this, "" + model.getName() + " Đã xóa mục yêu thích", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                final Food food1 = model;
                //bat su kien khi click vao Food_item
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int posittion, boolean isLongClick) {
                        Intent intent = new Intent(FoodList.this, FoodDetail.class);
                        intent.putExtra("FoodId", adapter.getRef(posittion).getKey());
                        startActivity(intent);
                    }
                });

            }
        };
        recyclerView.setAdapter(adapter);
    }
}
