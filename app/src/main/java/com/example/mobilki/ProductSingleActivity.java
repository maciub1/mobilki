package com.example.mobilki;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProductSingleActivity extends AppCompatActivity {
    private String mProductKey = null;

    private ImageView mProductSelectImage;
    private TextView mProductName;
    private TextView mCalories;
    private TextView mCarbohydrates;
    private TextView mProteins;
    private TextView mFat;
    private Button mProductRemoveBtn;
    private TextView mVegetarianLabel;
    private TextView mVeganLabel;
    private TextView mGlutenFreeLabel;


    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseProducts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_single);

        mProductKey = getIntent().getExtras().getString("product_id");

        mProductSelectImage = findViewById(R.id.image);

        mProductName = findViewById(R.id.SingleProductName);
        mCalories = findViewById(R.id.Singlecalories);
        mCarbohydrates = findViewById(R.id.Singlecarbohydrates);
        mProteins = findViewById(R.id.Singleproteins);
        mFat = findViewById(R.id.Singlefat);
        mDatabaseProducts = FirebaseDatabase.getInstance().getReference().child("Products");
        mProductRemoveBtn = findViewById(R.id.productRemoveBtn);
        mVegetarianLabel = findViewById(R.id.vegetarianLabel);
        mVeganLabel = findViewById(R.id.veganLabel);
        mGlutenFreeLabel = findViewById(R.id.glutenFreeLabel);
        mAuth=FirebaseAuth.getInstance();

        mDatabaseProducts.child(mProductKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = (String) snapshot.child("name").getValue();
                String calories = (String) snapshot.child("calories").getValue();
                String carbohydrates = (String) snapshot.child("carbohydrates").getValue();
                String proteins = (String) snapshot.child("proteins").getValue();
                String fat = (String) snapshot.child("fat").getValue();
                String image = (String) snapshot.child("image").getValue();
                String product_uid = (String) snapshot.child("uid").getValue();
                mProductName.setText(name);
                mCalories.setText(calories+"kcal");
                mCarbohydrates.setText("Węglowodany: "+carbohydrates);
                mProteins.setText("Białko: "+proteins);
                mFat.setText("Tłuszcz: "+fat);
                Picasso.get().load(image).into(mProductSelectImage);
                if(mAuth.getCurrentUser().getUid().equals(product_uid)){
                    mProductRemoveBtn.setVisibility(View.VISIBLE);
                }
                if(snapshot.child("tags").child("vegetarian").getValue().equals("1"))
                    mVegetarianLabel.setVisibility(View.VISIBLE);
                if(snapshot.child("tags").child("vegan").getValue().equals("1"))
                    mVeganLabel.setVisibility(View.VISIBLE);
                if(snapshot.child("tags").child("gluten free").getValue().equals("1"))
                    mGlutenFreeLabel.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mProductRemoveBtn.setOnClickListener(v -> {
            mDatabaseProducts.child(mProductKey).removeValue();
            Intent ProductsIntent = new Intent(ProductSingleActivity.this, ProductActivity.class);
            startActivity(ProductsIntent);
        });
    }
}