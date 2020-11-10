package com.example.orderfoodandroid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.example.orderfoodandroid.Interface.RecyclerItemTouchHelperListener;
import com.example.orderfoodandroid.common.Common;
import com.example.orderfoodandroid.database.Database;
import com.example.orderfoodandroid.helper.RecyclerItemTouchHelper;
import com.example.orderfoodandroid.model.Favorites;
import com.example.orderfoodandroid.viewholder.FavoritesAdapter;
import com.example.orderfoodandroid.viewholder.FavoritesViewHolder;
import com.google.android.material.snackbar.Snackbar;

public class FavoritesActivity extends AppCompatActivity implements RecyclerItemTouchHelperListener {
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FavoritesAdapter adapter;

    RelativeLayout rootLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        recyclerView = findViewById(R.id.recycler_fav);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        rootLayout=findViewById(R.id.root_layout);

        //swipe to delete
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        loadFavorites();

    }

    private void loadFavorites() {

        adapter = new FavoritesAdapter(this, new Database(this).getAllFavorites(Common.currentUser.getPhone()));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof FavoritesViewHolder){
            String name = ((FavoritesAdapter)recyclerView.getAdapter()).getItem(position).getFoodName();

            final Favorites deleteItem = ((FavoritesAdapter)recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition());
            final int deleteIndex = viewHolder.getAdapterPosition();

            adapter.removeItem(viewHolder.getAdapterPosition());
            new Database(getBaseContext()).removeFromFavorites(deleteItem.getFoodId(), Common.currentUser.getPhone());

            Snackbar snackbar = Snackbar.make(rootLayout, name + " đã xoá khỏi danh sách yêu thích", Snackbar.LENGTH_LONG);
            snackbar.setAction("Hoàn tác", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.restoreItem(deleteIndex,deleteItem);
                    new Database(getBaseContext()).addToFavorites(deleteItem);

                }
            });
            snackbar.setActionTextColor(Color.RED);
            snackbar.show();
        }
        
    }
}