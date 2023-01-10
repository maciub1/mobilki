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
import android.widget.TextView;
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

public class DietAddActivity extends AppCompatActivity {
    private ImageButton mDietSelectImage;
    private EditText mDietName;
    private EditText mDietDesc;
    private Button mAddMealToDietBtn;
    private Button mNextDayBtn;
    private TextView mCaloriesField;
    private TextView mCarbohydratesField;
    private TextView mFatField;
    private TextView mProteinsField;
    private TextView mAddedMealsField;
    private Uri mImageUri = null;
    private StorageReference mStorageImage;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseDiets;
    private DatabaseReference mMeals;
    private ProgressDialog mProgress;
    private  double mCalories=0;
    private  double mCarbohydrates=0;
    private  double mFat=0;
    private  double mProteins=0;
    private  boolean mGlutenFree=true;
    private  boolean mVegan=true;
    private  boolean mVegetarian=true;
    private int day=1;
    final List<String> meals_ids = new ArrayList<>();
    private Button mAcceptBtn;
    private String mMealsStr;

    private static final int GALLERY_REQUEST = 1;
    private static final int MEAL_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diet_add);

        mDietSelectImage = findViewById(R.id.dietImageSelectBtn);
        mDietName = findViewById(R.id.DietName);
        mStorageImage = FirebaseStorage.getInstance().getReference().child("Diet_images");
        mDatabaseDiets = FirebaseDatabase.getInstance().getReference().child("Diets");
        DatabaseReference mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mProgress = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        mAddMealToDietBtn = findViewById(R.id.AddMealToDietBtn);
        mAcceptBtn = findViewById(R.id.DietAcceptBtn);
        mNextDayBtn = findViewById(R.id.NextDayAddBtn);
        mDietDesc = findViewById(R.id.DietDesc);
        mCaloriesField = findViewById(R.id.caloriesField);
        mFatField = findViewById(R.id.fatField);
        mProteinsField = findViewById(R.id.proteinsField);
        mCarbohydratesField = findViewById(R.id.carbohydratesField);
        mMealsStr="";
        mAddedMealsField = findViewById(R.id.AddedMeals);
        mDietSelectImage.setOnClickListener(view -> {
            Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
            galleryIntent.setType("image/*");
            startActivityForResult(galleryIntent,GALLERY_REQUEST);
        });
        mAddMealToDietBtn.setOnClickListener(v -> {
            Intent ProductSelectIntent = new Intent(DietAddActivity.this,MealSelectActivity.class);
            startActivityForResult(ProductSelectIntent,MEAL_REQUEST);
        });
        mAcceptBtn.setOnClickListener(v -> addDiet());
        mNextDayBtn.setOnClickListener(v -> mMeals=addDay(mMeals));
    }

    private void addDiet() {
        mProgress.setMessage("Adding diet ...");
        mProgress.show();

        if(day==1)
        {
            String name_val = mDietName.getText().toString().trim();
            String desc_val = mDietDesc.getText().toString();
            if(!TextUtils.isEmpty(name_val) && !TextUtils.isEmpty(desc_val))
            {

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
                        DatabaseReference newDiet = mDatabaseDiets.push();

                        newDiet.child("name").setValue(name_val);
                        newDiet.child("desc").setValue(desc_val);
                        newDiet.child("rating").setValue("0");
                        newDiet.child("image").setValue(downloadUri);
                        newDiet.child("uid").setValue(mAuth.getCurrentUser().getUid());
                        newDiet.child("tags").child("vegetarian").setValue("1");
                        newDiet.child("tags").child("vegan").setValue("1");
                        newDiet.child("tags").child("gluten free").setValue("1");
                        if (!mVegetarian)
                            newDiet.child("tags").child("vegetarian").setValue("0");
                        if (!mVegan)
                            newDiet.child("tags").child("vegan").setValue("0");
                        if (!mGlutenFree)
                            newDiet.child("tags").child("gluten free").setValue("0");
                        for (String id : meals_ids) {
                            newDiet.child("Meals").child(Integer.toString(day)).child(id).setValue("meal id");
                        }


                        mProgress.dismiss();
                        Intent mealIntent = new Intent(DietAddActivity.this, DietActivity.class);
                        mealIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(mealIntent);


                    }
                })).addOnFailureListener(e -> {
                    });
            } else{
                mProgress.dismiss();
                Toast.makeText(DietAddActivity.this, "Fill all fields", Toast.LENGTH_LONG).show();}
        } else{
            mProgress.dismiss();
            mMeals=addDay(mMeals);
            Intent mealIntent = new Intent(DietAddActivity.this, DietActivity.class);
            mealIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(mealIntent);
        }
    }

    private DatabaseReference addDay(DatabaseReference mMeals) {
        mProgress.setMessage("Adding day ...");
        mProgress.show();
        if(day==1) {
            String name_val = mDietName.getText().toString().trim();
            String desc_val = mDietDesc.getText().toString();
            if (!TextUtils.isEmpty(name_val) && !TextUtils.isEmpty(desc_val)) {
                DatabaseReference newDiet = mDatabaseDiets.push();
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

                        newDiet.child("name").setValue(name_val);
                        newDiet.child("rating").setValue("0");
                        newDiet.child("desc").setValue(desc_val);
                        newDiet.child("image").setValue(downloadUri);
                        newDiet.child("uid").setValue(mAuth.getCurrentUser().getUid());
                        newDiet.child("tags").child("vegetarian").setValue("1");
                        newDiet.child("tags").child("vegan").setValue("1");
                        newDiet.child("tags").child("gluten free").setValue("1");
                        if (!mVegetarian)
                            newDiet.child("tags").child("vegetarian").setValue("0");
                        if (!mVegan)
                            newDiet.child("tags").child("vegan").setValue("0");
                        if (!mGlutenFree)
                            newDiet.child("tags").child("gluten free").setValue("0");
                        for (String id : meals_ids) {
                            newDiet.child("Meals").child(Integer.toString(day)).child(id).setValue("meal id");
                        }

                        clean();
                        day++;
                        mProgress.dismiss();
                    }
                })).addOnFailureListener(e -> {
                    });
                return newDiet.child("Meals");
            } else{
                mProgress.dismiss();
                Toast.makeText(DietAddActivity.this, "Fill all fields", Toast.LENGTH_LONG).show();
            }
        } else
        {
            for (String id : meals_ids) {
                mMeals.child(Integer.toString(day)).child(id).setValue("meal id");
            }
            clean();
            day++;
            mProgress.dismiss();
            return mMeals;
        }
        return mMeals;
    }

    private void clean() {
        mDietSelectImage.setVisibility(View.GONE);
        mDietDesc.setVisibility(View.GONE);
        mDietName.setVisibility(View.GONE);
        mCaloriesField.setText("Calories: 0");
        mCarbohydratesField.setText("Carbohydrates: 0");
        mProteinsField.setText("Proteins: 0");
        mFatField.setText("Fat: 0");
        mCarbohydrates=0;
        mCalories=0;
        mProteins=0;
        mFat=0;
        meals_ids.clear();
        mMealsStr="";
        mAddMealToDietBtn.setText("Add meal");
        mAcceptBtn.setVisibility(View.GONE);
        mNextDayBtn.setVisibility(View.GONE);
        mAddedMealsField.setText(mMealsStr);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK){
            mImageUri = data.getData();

            mDietSelectImage.setImageURI(mImageUri);
        }
        if(requestCode==MEAL_REQUEST && resultCode == RESULT_OK)
        {
            String meal_id = data.getStringExtra("meal id");
            mMealsStr+=data.getStringExtra("name")+"\n";
            mCalories+=Double.parseDouble(data.getStringExtra("calories"));
            mCarbohydrates+=Double.parseDouble(data.getStringExtra("carbohydrates"));
            mFat+=Double.parseDouble(data.getStringExtra("fat"));
            mProteins+=Double.parseDouble(data.getStringExtra("proteins"));
            meals_ids.add(meal_id);
            if(data.getStringExtra("vegetarian").equals("0"))
                mVegetarian=false;
            if(data.getStringExtra("vegan").equals("0"))
                mVegan=false;
            if(data.getStringExtra("gluten free").equals("0"))
                mGlutenFree=false;
            mAcceptBtn.setVisibility(View.VISIBLE);
            mAddMealToDietBtn.setText("Dodaj kolejny posiłek");
            mNextDayBtn.setVisibility(View.VISIBLE);
            mCaloriesField.setText("Kalorie: "+ mCalories);
            mCarbohydratesField.setText("Węglowodany: "+ mCarbohydrates);
            mProteinsField.setText("Białko: "+ mProteins);
            mFatField.setText("Tłuszcz: "+ mFat);
            mAddedMealsField.setText(mMealsStr);



        }
    }
}