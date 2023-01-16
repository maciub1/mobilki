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

public class MealAddActivity extends AppCompatActivity {
    private ImageButton mMealSelectImage;
    private EditText mMealName;
    private Button mAddProductToMealBtn;
    private Uri mImageUri = null;

    private StorageReference mStorageImage;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseMeals;
    private ProgressDialog mProgress;
    private double mCalories = 0;
    private double mCarbohydrates = 0;
    private double mFat = 0;
    private double mProteins = 0;
    private boolean mGlutenFree = true;
    private boolean mVegan = true;
    private boolean mVegetarian = true;
    final List<String> product_ids = new ArrayList<>();
    private Button mAcceptBtn;

    private static final int GALLERY_REQUEST = 1;
    private static final int PRODUCT_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_add);

        mMealSelectImage = findViewById(R.id.mealImageSelectBtn);
        mMealName = findViewById(R.id.MealName);
        mStorageImage = FirebaseStorage.getInstance().getReference().child("Meal_images");
        mDatabaseMeals = FirebaseDatabase.getInstance().getReference().child("Meals");
        DatabaseReference mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mProgress = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        mAddProductToMealBtn = findViewById(R.id.AddProductToMealBtn);
        mAcceptBtn = findViewById(R.id.acceptBtn);
        mMealSelectImage.setOnClickListener(view -> {
            Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
            galleryIntent.setType("image/*");
            startActivityForResult(galleryIntent, GALLERY_REQUEST);
        });
        mAddProductToMealBtn.setOnClickListener(v -> {
            Intent ProductSelectIntent = new Intent(MealAddActivity.this, ProductSelectActivity.class);
            startActivityForResult(ProductSelectIntent, PRODUCT_REQUEST);
        });
        mAcceptBtn.setOnClickListener(v -> addMeal());
    }

    private void addMeal() {
        String name_val = mMealName.getText().toString().trim();
        if (!TextUtils.isEmpty(name_val)) {
            mProgress.setMessage("Adding meal ...");
            mProgress.show();
            StorageReference filepath = mStorageImage.child(mImageUri.getLastPathSegment());
            final UploadTask uploadTask = filepath.putFile(mImageUri);
            uploadTask.addOnSuccessListener(taskSnapshot -> uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();

                }
                // Continue with the task to get the download URL
                return filepath.getDownloadUrl();

            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String downloadUri = task.getResult().toString();
                    DatabaseReference newMeal = mDatabaseMeals.push();

                    newMeal.child("name").setValue(name_val);
                    newMeal.child("image").setValue(downloadUri);
                    newMeal.child("calories").setValue(Double.toString(mCalories));
                    newMeal.child("carbohydrates").setValue(Double.toString(mCarbohydrates));
                    newMeal.child("proteins").setValue(Double.toString(mProteins));
                    newMeal.child("fat").setValue(Double.toString(mFat));
                    newMeal.child("uid").setValue(mAuth.getCurrentUser().getUid());
                    newMeal.child("tags").child("vegetarian").setValue("1");
                    newMeal.child("tags").child("vegan").setValue("1");
                    newMeal.child("tags").child("gluten free").setValue("1");
                    if (!mVegetarian)
                        newMeal.child("tags").child("vegetarian").setValue("0");
                    if (!mVegan)
                        newMeal.child("tags").child("vegan").setValue("0");
                    if (!mGlutenFree)
                        newMeal.child("tags").child("gluten free").setValue("0");
                    for (String id : product_ids) {
                        newMeal.child("Products").child(id).setValue("product id");
                    }


                    mProgress.dismiss();

                    Intent mealIntent = new Intent(MealAddActivity.this, MealsActivity.class);
                    mealIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mealIntent);


                }
            })).addOnFailureListener(e -> {
            });

        } else
            Toast.makeText(MealAddActivity.this, "Fill all fields", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            mImageUri = data.getData();

            mMealSelectImage.setImageURI(mImageUri);
        }
        if (requestCode == PRODUCT_REQUEST && resultCode == RESULT_OK) {
            String product_id = data.getStringExtra("product id");
            Toast.makeText(MealAddActivity.this, "Product " + data.getStringExtra("name") + " added to meal",
                    Toast.LENGTH_SHORT).show();
            mCalories += Double.parseDouble(data.getStringExtra("calories"));
            mCarbohydrates += Double.parseDouble(data.getStringExtra("carbohydrates"));
            mFat += Double.parseDouble(data.getStringExtra("fat"));
            mProteins += Double.parseDouble(data.getStringExtra("proteins"));
            product_ids.add(product_id);
            if (data.getStringExtra("vegetarian").equals("0"))
                mVegetarian = false;
            if (data.getStringExtra("vegan").equals("0"))
                mVegan = false;
            if (data.getStringExtra("gluten free").equals("0"))
                mGlutenFree = false;
            mAcceptBtn.setVisibility(View.VISIBLE);
            mAddProductToMealBtn.setText("Add another product");


        }

    }
}