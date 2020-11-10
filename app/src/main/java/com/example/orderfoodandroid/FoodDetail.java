package com.example.orderfoodandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.orderfoodandroid.common.Common;
import com.example.orderfoodandroid.database.Database;
import com.example.orderfoodandroid.model.Food;
import com.example.orderfoodandroid.model.Order;
import com.example.orderfoodandroid.model.Rating;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class FoodDetail extends AppCompatActivity implements RatingDialogListener {
    TextView food_name, food_price, food_description;
    ImageView food_detail_img;

    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton btnRating;
    CounterFab btnCart;
    RatingBar ratingBar;
    ElegantNumberButton numberButton;
    String foodId = "";

    FirebaseDatabase database;
    DatabaseReference foods;
    DatabaseReference rating_reference;
    Food currentFood;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        //Firebase
        database = FirebaseDatabase.getInstance();
        foods = database.getReference("Foods");
        rating_reference = database.getReference("Rating");

        numberButton = (ElegantNumberButton) findViewById(R.id.number_button);
        btnRating = findViewById(R.id.btn_rating);
        ratingBar = findViewById(R.id.rating_bar);
        btnCart = (CounterFab) findViewById(R.id.btnCart);
        //bat su kien
        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Database(getBaseContext()).addToCart(new Order(
                        Common.currentUser.getPhone(),
                        foodId,
                        currentFood.getName(),
                        numberButton.getNumber(),
                        currentFood.getPrice(),
                        currentFood.getDiscount(),
                        currentFood.getImage()


                ));
                Toast.makeText(FoodDetail.this, " Đã Thêm  Vào Giỏ Hàng", Toast.LENGTH_SHORT).show();
            }
        });
        btnCart.setCount(new Database(this).getCountCart(Common.currentUser.getPhone()));
        btnRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRating();
            }
        });
        food_description = findViewById(R.id.food_description);
        food_name = findViewById(R.id.food_name);
        food_price = findViewById(R.id.food_price);
        food_detail_img = findViewById(R.id.img_food);
        collapsingToolbarLayout = findViewById(R.id.collapsing);

        if (getIntent() != null)
            foodId = getIntent().getStringExtra("FoodId");
        if (!foodId.isEmpty()) {

            getDetail(foodId);
            getRating(foodId);
        }
    }

    private void getRating(String foodId) {
        Query foodRating = rating_reference.orderByChild("foodId").equalTo(foodId);
        foodRating.addValueEventListener(new ValueEventListener() {
            int count = 0, sum = 0;

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Rating item = postSnapshot.getValue(Rating.class);
                    sum += Integer.parseInt(item.getRateValue());
                    count++;
                }
                //tính trung bình Sao
                if (count != 0) {
                    float average = sum / count;
                    ratingBar.setRating(average);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showRating() {
        new AppRatingDialog.Builder()
                .setPositiveButtonText("Gửi")
                .setNegativeButtonText("Hủy")
                .setNoteDescriptions(Arrays.asList("Rất tồi", "Không tốt", "Tạm được", "Rất tốt", "Trên tuyệt vời"))
                .setDefaultRating(1)
                .setTitle("Đánh giá sản phẩm này")
                .setDescription("Hãy để lại số Sao và để lại phản hồi cho chúng tôi")
                .setTitleTextColor(R.color.colorAccent)
                .setDescriptionTextColor(R.color.colorAccent)
                .setHint("Để lại Bình luận tại đây: ")
                .setHintTextColor(R.color.colorAccent)
                .setCommentTextColor(android.R.color.white)
                .create(FoodDetail.this)
                .show();

    }

    private void getDetail(final String foodId) {
        foods.child(foodId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentFood = dataSnapshot.getValue(Food.class);
                //set Image
                Picasso.with(getBaseContext()).load(currentFood.getImage())
                        .into(food_detail_img);
                collapsingToolbarLayout.setTitle(currentFood.getName());
                food_price.setText(currentFood.getPrice());
                food_name.setText(currentFood.getName());
                food_description.setText(currentFood.getDescription());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onNegativeButtonClicked() {

    }

    @Override
    public void onPositiveButtonClicked(int value, @NotNull String comment)         {
        //nhận đánh giá và gửi lên firebase
        final Rating rating = new Rating(Common.currentUser.getPhone(), foodId, String.valueOf(value), comment);
        rating_reference.push()
                .setValue(rating)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(FoodDetail.this, "Thank you for submit rating !!!", Toast.LENGTH_SHORT).show();
                    }
                });
//        rating_reference.child(Common.currentUser.getPhone()).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if (dataSnapshot.child(Common.currentUser.getPhone()).exists()) {
//                    //xóa giá trị cũ
////                    rating_reference.child(Common.currentUser.getPhone()).removeValue();
//                    //update(set) giá trị mới
////                    rating_reference.child(Common.currentUser.getPhone()).setValue(rating);
//                } else {
//                    //update giá trị mới
//                    rating_reference.child(Common.currentUser.getPhone()).setValue(rating);
//                }
//                Toast.makeText(FoodDetail.this, "Cảm ơn bạn đã đã gửi đánh giá", Toast.LENGTH_SHORT).show();
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });

    }
}