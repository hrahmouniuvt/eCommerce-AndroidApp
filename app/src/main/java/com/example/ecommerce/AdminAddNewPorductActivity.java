package com.example.ecommerce;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.ecommerce.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class AdminAddNewPorductActivity extends AppCompatActivity {
    private String CategoryName, Description, Price, Pname, SaveCurrentDate, SaveCurrentTime, ProductRandomKey, DownloadImageUrl;
    private Button AddNewProductButton;
    private ImageView InputProductImage;
    private EditText InputProductName, InputProductDescription, InputProductPrice;
    private static final int GalleryPick = 1;
    ActivityResultLauncher<String> aPhoto;
    ActivityMainBinding binding;
    private Uri ImageUri;
    private StorageReference ProductImagesRef;
    private DatabaseReference ProductsRef;
    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_new_porduct);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        CategoryName = getIntent().getExtras().get("category").toString();
        ProductImagesRef = FirebaseStorage.getInstance().getReference().child("Product Images");
        ProductsRef= FirebaseDatabase.getInstance().getReference().child("Products");

        AddNewProductButton = (Button) findViewById(R.id.add_new_product);
        InputProductImage = (ImageView) findViewById(R.id.select_product_image);
        InputProductName = (EditText) findViewById(R.id.product_name);
        InputProductDescription = (EditText) findViewById(R.id.product_description);
        InputProductPrice = (EditText) findViewById(R.id.product_price);

        aPhoto = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                binding.appLogo.setImageURI(result);
                InputProductImage.setImageURI(result);
                ImageUri=result;
            }
        });
        loadingBar = new ProgressDialog(this);
        InputProductImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenGallery();
            }


        });
        AddNewProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ValidateProductData();
            }
        });
    }

    private void OpenGallery() {

        aPhoto.launch("image/*");
    }
    private void ValidateProductData(){

        Description = InputProductDescription.getText().toString();
        Price = InputProductPrice.getText().toString();
        Pname = InputProductName.getText().toString();
        storeProductInformation();

    }

    private void storeProductInformation() {
        loadingBar.setTitle("Adding new Product");
        loadingBar.setMessage("Please wait while we are adding the new product");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MM DD, YYYY");
        SaveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
        SaveCurrentTime = currentTime.format(calendar.getTime());
        ProductRandomKey = SaveCurrentDate+SaveCurrentTime;
        
        StorageReference filePath = ProductImagesRef.child(ImageUri.getLastPathSegment()+ProductRandomKey+ ".jpg");
        final UploadTask uploadTask = filePath.putFile(ImageUri);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                String message = e.toString();
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(getApplicationContext(), "Image uploaded succesfully", Toast.LENGTH_LONG).show();
                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if(!task.isSuccessful()){
                            throw task.getException();
                        }
                        DownloadImageUrl = filePath.getDownloadUrl().toString();
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(getApplicationContext(), "getting Image url succesfully", Toast.LENGTH_LONG).show();
                            saveProductInfoToDatabase();
                        }

                    }
                });
            }
        });
    }

    private void saveProductInfoToDatabase() {
        HashMap<String, Object> productMap = new HashMap<>();
        productMap.put("pid", ProductRandomKey);
        productMap.put("date", SaveCurrentDate);
        productMap.put("time", SaveCurrentTime);
        productMap.put("description", Description);
        productMap.put("image", DownloadImageUrl);
        productMap.put("category", CategoryName);
        productMap.put("price", Price);
        productMap.put("pname", Pname);

        ProductsRef.child(ProductRandomKey).updateChildren(productMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    DownloadImageUrl = task.getResult().toString();
                    Intent intent = new Intent(AdminAddNewPorductActivity.this, AdminCategoryActivity.class);
                    loadingBar.dismiss();

                    Toast.makeText(getApplicationContext(), "product is addedd sucessfully", Toast.LENGTH_LONG).show();
                }
                else{
                    loadingBar.dismiss();

                    String message = task.getException().toString();
                    Toast.makeText(getApplicationContext(), "Error: "+message, Toast.LENGTH_LONG).show();
                }

            }
        });
    }

}