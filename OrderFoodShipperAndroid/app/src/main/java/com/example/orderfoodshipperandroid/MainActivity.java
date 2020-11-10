package com.example.orderfoodshipperandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.orderfoodshipperandroid.Common.Common;
import com.example.orderfoodshipperandroid.Model.Shipper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

public class MainActivity extends AppCompatActivity {
    Button btnSignIn;
    MaterialEditText edt_phone, edt_password;

    FirebaseDatabase database;
    DatabaseReference shippers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        edt_phone = findViewById(R.id.edt_phone);
        edt_password = findViewById(R.id.edt_password);
        btnSignIn = findViewById(R.id.btn_signIn);

        database = FirebaseDatabase.getInstance();
        shippers = database.getReference("Shippers");
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login(edt_phone.getText().toString(),edt_password.getText().toString());
            }
        });
    }

    private void login(String phone, final String password) {
        shippers.child(phone).
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Shipper shipper = dataSnapshot.getValue(Shipper.class);
                            if (shipper.getPassword().equals(password)) {
                                Intent intent=new Intent(MainActivity.this,Home.class);
                                Common.currentShipper=shipper;
                                startActivity(intent);
                                finish();

                            } else {
                                Toast.makeText(MainActivity.this, "Mật Khẩu Sai", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            Toast.makeText(MainActivity.this, "Tài Khoản Không Tồn Tại", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}