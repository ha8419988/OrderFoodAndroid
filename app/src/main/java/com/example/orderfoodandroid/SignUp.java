package com.example.orderfoodandroid;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

public class SignUp extends AppCompatActivity {
    EditText edt_sdt, edt_pass, edt_ten;
    Button btn_signup1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        edt_ten = findViewById(R.id.edt_ten);
        edt_sdt = findViewById(R.id.edt_sdt1);
        edt_pass = findViewById(R.id.edt_password1);
        btn_signup1 = findViewById(R.id.btn_signup1);

        //Init Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("User");

        //bat su kien
        btn_signup1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Common.isConnectedToInternet(getBaseContext())) {
                    final ProgressDialog mDialog = new ProgressDialog(SignUp.this);
                    mDialog.setMessage("Vui Lòng Đợi...");
                    mDialog.show();

                    table_user.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            //check if phone  exist In DATA
                            if (dataSnapshot.child(edt_sdt.getText().toString()).exists()) {
                                mDialog.dismiss();
                                Toast.makeText(SignUp.this, "SĐT ĐÃ TỒN TẠI", Toast.LENGTH_SHORT).show();

                            }
                            if (!dataSnapshot.child(edt_sdt.getText().toString()).exists()) {
                                if (edt_pass.getText().length() == 0) {
                                    mDialog.dismiss();
                                    Toast.makeText(SignUp.this, "BẠN CHƯA NHẬP MK", Toast.LENGTH_SHORT).show();
                                } else if (edt_ten.getText().length() == 0) {
                                    mDialog.dismiss();
                                    Toast.makeText(SignUp.this, "BẠN CHƯA ĐIỀN TÊN", Toast.LENGTH_SHORT).show();

                                } else {
                                    mDialog.dismiss();
                                    User user = new User(edt_ten.getText().toString(), edt_pass.getText().toString());
                                    table_user.child(edt_sdt.getText().toString()).setValue(user);//set value de luu du lieu
                                    Toast.makeText(SignUp.this, "ĐĂNG KÍ THÀNH CÔNG", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                } else {
                    Toast.makeText(SignUp.this, "Hãy Kiểm Tra lại đường truyền Internet của bạn", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });
    }
}
