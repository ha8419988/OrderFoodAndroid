package com.example.orderfoodandroid;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.orderfoodandroid.Interface.RecyclerItemTouchHelperListener;
import com.example.orderfoodandroid.common.Common;
import com.example.orderfoodandroid.database.Database;
import com.example.orderfoodandroid.helper.RecyclerItemTouchHelper;
import com.example.orderfoodandroid.model.MyResponse;
import com.example.orderfoodandroid.model.Notification;
import com.example.orderfoodandroid.model.Order;
import com.example.orderfoodandroid.model.Request;
import com.example.orderfoodandroid.model.Sender;
import com.example.orderfoodandroid.model.Token;
import com.example.orderfoodandroid.remote.APIService;
import com.example.orderfoodandroid.remote.IGoogleService;
import com.example.orderfoodandroid.viewholder.CartAdapter;
import com.example.orderfoodandroid.viewholder.CartViewHolder;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Cart extends AppCompatActivity implements RecyclerItemTouchHelperListener{
//        GoogleApiClient.ConnectionCallbacks,
//        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static final int LOCATION_REQUEST = 9999;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9997;
    private static final int LOCATION_PERMISSION_REQUEST = 1001;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    FirebaseDatabase database;
    DatabaseReference requests;
    public TextView txtTotal_Price;
    Button btn_Place_Order;

    List<Order> cart = new ArrayList<>();
    CartAdapter adapter;
    RelativeLayout rootLayout;
    APIService mService;
    IGoogleService mGoogleMapService;

    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private static int UPDATE_INTERVAL = 5000;
    private static int FATEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        //firebase
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        //API SERVER
        mService = Common.getFCMService();
        mGoogleMapService = Common.getGoogleMapAPI();


        //init
        recyclerView = findViewById(R.id.listCart);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        rootLayout = findViewById(R.id.rootLayout);
        //swipe to delete
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);


        txtTotal_Price = findViewById(R.id.total_price);
        btn_Place_Order = findViewById(R.id.btn_place);
        btn_Place_Order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cart.size() > 0)
                    showAlert();
                else
                    Toast.makeText(Cart.this, "Giỏ hàng của bạn trống!!!", Toast.LENGTH_SHORT).show();
            }
        });
        loadListFood();
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private boolean checkPlayService() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(Cart.this, "Thiết Bị Này Không Hỗ Trợ", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    private void showAlert() {
        AlertDialog.Builder alert = new AlertDialog.Builder(Cart.this);
        alert.setTitle("Thông Tin");
        alert.setMessage("Nhập Địa Chỉ");
        final EditText edtAddress = new EditText(Cart.this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );//layout params:set up các view con trong view cha(Linear layout)

        edtAddress.setLayoutParams(layoutParams);
        alert.setView(edtAddress);
        alert.setIcon(R.drawable.shopping_cart1_24);
        alert.setPositiveButton("Đồng Ý", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Request request = new Request(
                        Common.currentUser.getPhone(),
                        Common.currentUser.getName(),
                        edtAddress.getText().toString(),
                        txtTotal_Price.getText().toString(),
                        cart);

                //gui len firebase //su dung currentmillis lam key
                String order_number = String.valueOf(System.currentTimeMillis());
                requests.child(order_number).setValue(request);
                //delete cart
                new Database(getBaseContext()).cleanCart(Common.currentUser.getPhone());
                sendNotificationOrder(order_number);


            }
        });
        alert.setNegativeButton("Không", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        alert.show();
    }

    private void sendNotificationOrder(final String order_number) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query data = tokens.orderByChild("serverToken").equalTo(true);//get all node with  isServerToken  is true
        data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Token serverToken = postSnapshot.getValue(Token.class);
                    //create raw payload to send
                    Notification notification = new Notification("", "Bạn Có Đơn Hàng Mới #" + order_number);
                    Sender content = new Sender(serverToken.getToken(), notification);
                    mService.sendNotification(content).enqueue(new Callback<MyResponse>() {
                        @Override
                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                            //only run when get result
                            if (response.code() == 200) {
                                if (response.body().success == 1) {
                                    Toast.makeText(Cart.this, "Cảm Ơn Bạn Đã Đặt Hàng !", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    Toast.makeText(Cart.this, "Lỗi!!!", Toast.LENGTH_SHORT).show();

                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<MyResponse> call, Throwable t) {
                            Log.e("ERROR", t.getMessage());

                        }
                    });


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }


    private void loadListFood() {
        cart = new Database(this).getCarts(Common.currentUser.getPhone());
        adapter = new CartAdapter(cart, this);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

        //tinh toan tong tien
        int total = 0;
        for (Order order : cart)
            total += (Integer.parseInt(order.getPrice())) * (Integer.parseInt(order.getQuantity()));
        Locale locale = new Locale("vi", "VN");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
        txtTotal_Price.setText(fmt.format(total));


    }


    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getTitle().equals(Common.DELETE))
            deleteCart(item.getOrder());
        return true;
    }

    private void deleteCart(int order) {
        // Xóa item tại List<Order> bởi position
        cart.remove(order);
//        //sau đó sẽ xóa dữ liệu cũ từ Sqlite
        new Database(this).cleanCart(Common.currentUser.getPhone());
//        //update dữ liệu mới từ List<order> tới Sqlite
        for (Order item : cart)
            new Database(this).addToCart(item);
//        //refesh lại dữ liệu đã bị xóa
        loadListFood();
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof CartViewHolder) {
            String name = ((CartAdapter) recyclerView.getAdapter())
                    .getItem(viewHolder.getAdapterPosition()).getProductName();
            final Order deleteItem = ((CartAdapter) recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition());
            final int deleteIndex = viewHolder.getAdapterPosition();

            adapter.removeItem(deleteIndex);
            new Database(getBaseContext()).removeFromCart(deleteItem.getProductId(), Common.currentUser.getPhone());

            int total = 0;
            List<Order> orders = new Database(getBaseContext()).getCarts(Common.currentUser.getPhone());
            for (Order item : orders)
                total += (Integer.parseInt(item.getPrice())) * (Integer.parseInt(item.getQuantity()));
            Locale locale = new Locale("vi", "VN");
            NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
            txtTotal_Price.setText(fmt.format(total));

           //tao Snackbar
            Snackbar snackbar = Snackbar.make(rootLayout, name + " đã xoá khỏi giỏ hàng!", Snackbar.LENGTH_LONG);
            snackbar.setAction("Hoàn  Tác", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    adapter.restoreItem(deleteItem, deleteIndex);
                    new Database(getBaseContext()).addToCart(deleteItem);

                    int total = 0;
                    List<Order> orders = new Database(getBaseContext()).getCarts(Common.currentUser.getPhone());
                    for (Order item : orders)
                        total += (Integer.parseInt(item.getPrice())) * (Integer.parseInt(item.getQuantity()));
                    Locale locale = new Locale("vi", "VN");
                    NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
                    txtTotal_Price.setText(fmt.format(total));

                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }

}