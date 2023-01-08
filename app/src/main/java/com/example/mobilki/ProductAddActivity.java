package com.example.mobilki;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

public class ProductAddActivity extends AppCompatActivity {
    private ImageButton mProductSelectImage;
    private EditText mProductName;
    private EditText mCalories;
    private EditText mCarbohydrates;
    private EditText mProteins;
    private EditText mFat;

    private final List<String> tags = new ArrayList<>();
    private Uri mImageUri = null;

    private StorageReference mStorageImage;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseProducts;
    private ProgressDialog mProgress;


    private static final int GALLERY_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_add);

        mProductSelectImage = findViewById(R.id.productImageSelectBtn);

        mProductName = findViewById(R.id.ProductName);
        mCalories = findViewById(R.id.calories);
        mCarbohydrates = findViewById(R.id.carbohydrates);
        mProteins = findViewById(R.id.proteins);
        mFat = findViewById(R.id.fat);

        mStorageImage = FirebaseStorage.getInstance().getReference().child("Product_images");
        mDatabaseProducts = FirebaseDatabase.getInstance().getReference().child("Products");
        DatabaseReference mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mProgress = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();


        Button mSubmitBtn = findViewById(R.id.addProductBtn);

        mProductSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GALLERY_REQUEST);
            }
        });

        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addProduct();
            }
        });

    }

    private void addProduct() {
        String name_val = mProductName.getText().toString().trim();
        String calories_val = mCalories.getText().toString().trim();
        String carbohydrates_val = mCarbohydrates.getText().toString().trim();
        String proteins_val = mProteins.getText().toString().trim();
        String fat_val = mFat.getText().toString().trim();

        if(!TextUtils.isEmpty(name_val) && !TextUtils.isEmpty(calories_val)
                && !TextUtils.isEmpty(carbohydrates_val) && !TextUtils.isEmpty(proteins_val)
                && !TextUtils.isEmpty(fat_val) && mImageUri != null){
            mProgress.setMessage("Dodawanie produktu ...");
            mProgress.show();

            StorageReference filepath = mStorageImage.child(mImageUri.getLastPathSegment());
            final UploadTask uploadTask = filepath.putFile(mImageUri);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();

                            }
                            // Continue with the task to get the download URL
                            return filepath.getDownloadUrl();

                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                String downloadUri = task.getResult().toString();
                                DatabaseReference newProduct = mDatabaseProducts.push();

                                newProduct.child("name").setValue(name_val);
                                newProduct.child("image").setValue(downloadUri);
                                newProduct.child("calories").setValue(calories_val);
                                newProduct.child("carbohydrates").setValue(carbohydrates_val);
                                newProduct.child("proteins").setValue(proteins_val);
                                newProduct.child("fat").setValue(fat_val);


                                newProduct.child("uid").setValue(mAuth.getCurrentUser().getUid());
                                newProduct.child("tags").child("vegetarian").setValue("0");
                                newProduct.child("tags").child("vegan").setValue("0");
                                newProduct.child("tags").child("gluten free").setValue("0");
                                if(!tags.isEmpty())
                                {
                                    if(tags.contains("vegetarian"))
                                        newProduct.child("tags").child("vegetarian").setValue("1");
                                    if(tags.contains("vegan"))
                                        newProduct.child("tags").child("vegan").setValue("1");
                                    if(tags.contains("gluten free"))
                                        newProduct.child("tags").child("gluten free").setValue("1");
                                }


                                mProgress.dismiss();

                                Intent productIntent = new Intent(ProductAddActivity.this, ProductActivity.class);
                                productIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(productIntent);


                            }
                        }
                    });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                }
            });

        } else{
            Toast.makeText(ProductAddActivity.this, "Wypełnij wszystkie pola", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK){
            mImageUri = data.getData();

            mProductSelectImage.setImageURI(mImageUri);
        }

    }

    public void onCheckboxClicked(View view) {
        boolean checked = ((CheckBox) view).isChecked();

        // Check which checkbox was clicked
        switch(view.getId()) {
            case R.id.vegetarian:
                if (checked)
                    tags.add("vegetarian");
                else
                    tags.remove("vegetarian");
                break;
            case R.id.vegan:
                if (checked)
                    tags.add("vegan");

                else
                    tags.remove("vegan");
                break;
            case R.id.glutenFree:
                if (checked)
                    tags.add("gluten free");
                else
                    tags.remove("gluten free");
                break;

        }

    }

}