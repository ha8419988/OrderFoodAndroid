package com.example.orderfoodandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.orderfoodandroid.common.Common;
import com.example.orderfoodandroid.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {
    Button btn_signin, btn_signup;
    ImageView bgimage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //anh xa
        btn_signin = (Button) findViewById(R.id.btn_signin);
        btn_signup = (Button) findViewById(R.id.btn_signup);
        bgimage = findViewById(R.id.back2);
//Paper
        Paper.init(this);
        //bat su kien
        btn_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SignIn.class);
                startActivity(intent);
            }
        });
        btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SignUp.class);
                startActivity(intent);
            }
        });
        //checkbox Remember
        String user = Paper.book().read(Common.USER_KEY);
        String pass = Paper.book().read(Common.PASS_KEY);
        if (user != null && pass != null) {
            if (!user.isEmpty() && !pass.isEmpty()) {
                loginAuto(user, pass);
            }
        }
    }

    private void loginAuto(final String phone, final String pass) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("User");
        if (Common.isConnectedToInternet(getBaseContext())) {
            final ProgressDialog mDialog = new ProgressDialog(MainActivity.this);
            mDialog.setMessage("Vui Lòng Đợi");
            mDialog.show();

            table_user.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    //check if user not exist In DATA
                    if (dataSnapshot.child(phone).exists()) {
                        //get user Info
                        mDialog.dismiss();
                        User user = dataSnapshot.child(phone).getValue(User.class);
                        //set Phone for shipper if he not can find address
                        user.setPhone(phone);
                        if (user.getPassword().equals(pass)) {
                            {
                                Intent intent = new Intent(MainActivity.this, Home.class);
                                Common.currentUser = user;
                                startActivity(intent);

                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Đăng Nhập Thất Bại", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        mDialog.dismiss();
                        Toast.makeText(MainActivity.this, "Tài Khoản K Tồn Tại", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            Toast.makeText(MainActivity.this, "Hãy Kiểm Tra lại đường truyền Internet của bạn", Toast.LENGTH_LONG).show();
            return;
        }
    }
}
