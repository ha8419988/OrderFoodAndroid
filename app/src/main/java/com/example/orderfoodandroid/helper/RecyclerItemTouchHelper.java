package com.example.orderfoodandroid.helper;

import android.graphics.Canvas;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.orderfoodandroid.Interface.RecyclerItemTouchHelperListener;
import com.example.orderfoodandroid.viewholder.CartViewHolder;
import com.example.orderfoodandroid.viewholder.FavoritesViewHolder;
import com.example.orderfoodandroid.viewholder.FoodViewHolder;

public class RecyclerItemTouchHelper extends ItemTouchHelper.SimpleCallback {
    private RecyclerItemTouchHelperListener listener;

    public RecyclerItemTouchHelper(int dragDirs, int swipeDirs, RecyclerItemTouchHelperListener listener) {
        super(dragDirs, swipeDirs);
        this.listener = listener;
    }


    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        if (listener != null) {
            listener.onSwiped(viewHolder, direction, viewHolder.getAdapterPosition());
        }
    }

    @Override
    public int convertToAbsoluteDirection(int flags, int layoutDirection) {
        return super.convertToAbsoluteDirection(flags, layoutDirection);
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof CartViewHolder) {
            View foreground = ((CartViewHolder) viewHolder).view_foreground;
            getDefaultUIUtil().clearView(foreground);
        } else if (viewHolder instanceof FavoritesViewHolder) {
            View foreground = ((FavoritesViewHolder) viewHolder).view_fg;
            getDefaultUIUtil().clearView(foreground);
        }
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (viewHolder instanceof CartViewHolder) {
            View foreground = ((CartViewHolder) viewHolder).view_foreground;
            getDefaultUIUtil().onDraw(c, recyclerView, foreground, dX, dY, actionState, isCurrentlyActive);
        }
        else if (viewHolder instanceof FavoritesViewHolder){
            View foreground = ((FavoritesViewHolder) viewHolder).view_fg;
            getDefaultUIUtil().onDraw(c, recyclerView, foreground, dX, dY, actionState, isCurrentlyActive);
        }
    }

    @Override
    public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
        if (viewHolder != null) {
            if (viewHolder instanceof CartViewHolder) {
                View foreground = ((CartViewHolder) viewHolder).view_foreground;
                getDefaultUIUtil().onSelected(foreground);
            }
            else if (viewHolder instanceof FavoritesViewHolder){
                View foreground = ((FavoritesViewHolder) viewHolder).view_fg;
                getDefaultUIUtil().onSelected(foreground);
            }
        }
    }

    @Override
    public void onChildDrawOver(@NonNull Canvas c, @NonNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (viewHolder instanceof CartViewHolder) {
            View foreground = ((CartViewHolder) viewHolder).view_foreground;
            getDefaultUIUtil().onDrawOver(c, recyclerView, foreground, dX, dY, actionState, isCurrentlyActive);
        }
        else if (viewHolder instanceof FavoritesViewHolder){
            View foreground = ((FavoritesViewHolder) viewHolder).view_fg;
            getDefaultUIUtil().onDrawOver(c, recyclerView, foreground, dX, dY, actionState, isCurrentlyActive);
        }
    }
}

