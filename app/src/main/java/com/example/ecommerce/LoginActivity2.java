package com.example.ecommerce;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

import com.example.ecommerce.R;
import com.example.ecommerce.model.Users;
import com.example.ecommerce.model.prevalent.Prevalent;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rey.material.widget.CheckBox;

import java.util.HashMap;
import java.util.Map;

import io.paperdb.Paper;

public class LoginActivity2 extends AppCompatActivity {

    private EditText InputPhoneNumber, InputPassword;
    private Button LoginButton;
    private ProgressDialog loadingBar;
    private String parentDbName="Users";
    private CheckBox ChkBoxRememberMe;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);

        LoginButton = (Button) findViewById(R.id.login_btn);
        InputPassword = (EditText) findViewById(R.id.login_password_input);
        InputPhoneNumber = (EditText) findViewById(R.id.login_phone_number_input);
        loadingBar = new ProgressDialog(this);
        ChkBoxRememberMe = (CheckBox) findViewById(R.id.remember_me_chkb);
        Paper.init(this);


        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }

            private void loginUser() {
                String phone = InputPhoneNumber.getText().toString();
                String password = InputPassword.getText().toString();

                 if(TextUtils.isEmpty(phone)){

                     Toast.makeText(getApplicationContext(), "plese write your phone number", Toast.LENGTH_SHORT).show();
                }
                else if(TextUtils.isEmpty(password)){

                    Toast.makeText(getApplicationContext(), "plese write your password", Toast.LENGTH_SHORT).show();
                }
                 else{
                     loadingBar.setTitle("login account");
                     loadingBar.setMessage("Please wait while we are checking teh credentials");
                     loadingBar.setCanceledOnTouchOutside(false);
                     loadingBar.show();

                     AllowAccessToAccount(phone, password);
                 }

            }

            private void AllowAccessToAccount(String phone, String password) {
                if(ChkBoxRememberMe.isChecked()){

                    Paper.book().write(Prevalent.UserPhoneKey,phone);
                    Paper.book().write(Prevalent.UserPasswordKey,password);
                }
                final DatabaseReference RootRef ;
                RootRef = FirebaseDatabase.getInstance().getReference().child("Users");

                RootRef.orderByChild("phone").equalTo(phone).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (snapshot.exists()) {
                                Toast.makeText(getApplicationContext(), "kokokokoko", Toast.LENGTH_LONG).show();
                                //Users usersData = snapshot.child(parentDbName).child(phone).getValue(Users.class);
                                Map<String, Object> map = (Map<String, Object>)   snapshot.getValue();
                                Log.d("AnyTagYouWant", map.toString());

                                //Toast.makeText(getApplicationContext(), (String) map.get("phone"), Toast.LENGTH_LONG).show();
                                Map<String, Object> nestedmap = (Map)map.get("phone");
                                Users usersData = new Users((String) nestedmap.get("name"),(String)nestedmap.get("phone"),(String)nestedmap.get("password"));
                                Log.d("TagYouWant", usersData.toString());

                                if(usersData.getPhone().equals(phone)){
                                    if(usersData.getPassword().equals(password)){
                                        Toast.makeText(getApplicationContext(), "Logged in successfully", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                        Intent intent = new Intent(LoginActivity2.this, HomeActivity.class);
                                        startActivity(intent);

                                    }
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "the account with this phone number"+phone+"does not exist", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }
}