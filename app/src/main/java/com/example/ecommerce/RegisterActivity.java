package com.example.ecommerce;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.ecommerce.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private Button createAccountButton;
    private EditText inputNmae, inputPhoneNumber, inputPassword;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        createAccountButton = (Button) findViewById(R.id.register_btn);
        inputNmae = (EditText) findViewById(R.id.register_username_input);
        inputPassword = (EditText) findViewById(R.id.register_password_input);
        inputPhoneNumber = (EditText) findViewById(R.id.register_phone_number_input);
        loadingBar = new ProgressDialog(this);

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateAccount();
            }


        });
    }

    private void CreateAccount() {

        String name = inputNmae.getText().toString();
        String phone = inputPhoneNumber.getText().toString();
        String password = inputPassword.getText().toString();

        if(TextUtils.isEmpty(name)){

            Toast.makeText(this, "plese write your name", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(phone)){

            Toast.makeText(this, "plese write your phone number", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password)){

            Toast.makeText(this, "plese write your password", Toast.LENGTH_SHORT).show();
        }
        else{
            loadingBar.setTitle("create account");
            loadingBar.setMessage("Please wait while we are checking teh credentials");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            ValidatePhoneNumber(name, phone, password);
        }

    }

    private void ValidatePhoneNumber(String name, String phone, String password) {
        final DatabaseReference RootRef ;
        RootRef = FirebaseDatabase.getInstance().getReference();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message");

        myRef.setValue("Hello, World!");
        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //check if the user exists first using the below code
                //if(DataSnapshot.child("Users").child(phone).exists())}

                HashMap<String, Object> userDataMap = new HashMap<>();
                userDataMap.put("phone", phone);
                userDataMap.put("name", name);
                userDataMap.put("password", password);
                // Write a message to the database

                RootRef.child("Users").child("name").setValue(userDataMap);
                RootRef.child("Users").child("name").updateChildren(userDataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(RegisterActivity.this, "Your account has been created", Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity2.class);
                            startActivity(intent);
                        }
                        else{
                            Toast.makeText(RegisterActivity.this, "Ther was an erro", Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                            Toast.makeText(RegisterActivity.this,"network error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}