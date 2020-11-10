package com.example.orderfoodandroid;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.example.orderfoodandroid.common.Common;
import com.example.orderfoodandroid.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.paperdb.Paper;

public class SignIn extends AppCompatActivity {
    EditText edt_sdt, edt_pass;
    Button btn_signin1;
    CheckBox checkBox;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        edt_sdt = findViewById(R.id.edt_sdt);
        edt_pass = findViewById(R.id.edt_password);
        btn_signin1 = findViewById(R.id.btn_signin1);
        checkBox = findViewById(R.id.check_Remember);
        // Paper
        //help u write key-value to Android memory
        Paper.init(this);

        //Init Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("User");

        //bat su kien
        btn_signin1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Common.isConnectedToInternet(getBaseContext())) {
                    //lưu tài khoản và pass
                    if (checkBox.isChecked()) {
                        Paper.book().write(Common.USER_KEY, edt_sdt.getText().toString());
                        Paper.book().write(Common.PASS_KEY, edt_pass.getText().toString());
                    }

                    final ProgressDialog mDialog = new ProgressDialog(SignIn.this);
                    mDialog.setMessage("Vui Lòng Đợi");
                    mDialog.show();

                    table_user.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            //check if user not exist In DATA
                            if (dataSnapshot.child(edt_sdt.getText().toString()).exists()) {
                                //get user Info
                                mDialog.dismiss();
                                User user = dataSnapshot.child(edt_sdt.getText().toString()).getValue(User.class);
                                //set Phone for shipper if he not can Find address
                                user.setPhone(edt_sdt.getText().toString());
                                if (user.getPassword().equals(edt_pass.getText().toString())) {
                                    {
                                        Intent intent = new Intent(SignIn.this, Home.class);
                                        Common.currentUser = user;
                                        startActivity(intent);
                                        finish();
                                        table_user.removeEventListener(this);
                                    }
                                } else {
                                    Toast.makeText(SignIn.this, "Sai Mật Khẩu", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                mDialog.dismiss();
                                Toast.makeText(SignIn.this, "Tài Khoản K Tồn Taị", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                } else {
                    Toast.makeText(SignIn.this, "Hãy Kiểm Tra lại đường truyền Internet của bạn", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });
    }
}
