package com.example.mobilki;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class MealSingleActivity extends AppCompatActivity {

    private String mMealKey = null;

    private ImageView mMealSelectImage;
    private TextView mMealName;
    private TextView mCalories;
    private TextView mCarbohydrates;
    private TextView mProteins;
    private TextView mFat;
    private Button mMealRemoveBtn;
    private TextView mVegetarianLabel;
    private TextView mVeganLabel;
    private TextView mGlutenFreeLabel;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseMeals;
    DatabaseReference mDatabaseProducts;

    private RecyclerView mProductList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_single);

        mMealKey = getIntent().getExtras().getString("meal_id");

        mMealSelectImage = findViewById(R.id.SingleMealImage);
        mMealName = findViewById(R.id.SingleMealName);
        mCalories = findViewById(R.id.SingleMealCalories);
        mCarbohydrates = findViewById(R.id.SingleMealCarbohydrates);
        mProteins = findViewById(R.id.SingleMealProteins);
        mFat = findViewById(R.id.SingleMealFat);
        mDatabaseMeals = FirebaseDatabase.getInstance().getReference().child("Meals");
        mDatabaseProducts = FirebaseDatabase.getInstance().getReference().child("Products");
        mMealRemoveBtn = findViewById(R.id.mealRemoveBtn);
        mVegetarianLabel = findViewById(R.id.vegetarianMealLabel);
        mVeganLabel = findViewById(R.id.veganMealLabel);
        mGlutenFreeLabel = findViewById(R.id.glutenFreeMealLabel);
        mAuth = FirebaseAuth.getInstance();
        mProductList = findViewById(R.id.productListM);
        mProductList.setNestedScrollingEnabled(false);
        mProductList.setHasFixedSize(false);
        mProductList.setLayoutManager(new LinearLayoutManager(this));
        mDatabaseMeals.child(mMealKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = (String) snapshot.child("name").getValue();
                String calories = (String) snapshot.child("calories").getValue();
                String carbohydrates = (String) snapshot.child("carbohydrates").getValue();
                String proteins = (String) snapshot.child("proteins").getValue();
                String fat = (String) snapshot.child("fat").getValue();
                String image = (String) snapshot.child("image").getValue();
                String meal_uid = (String) snapshot.child("uid").getValue();
                mMealName.setText(name);
                mCalories.setText(calories + "kcal");
                mCarbohydrates.setText("Węglowodany: " + carbohydrates);
                mProteins.setText("Białko: " + proteins);
                mFat.setText("Tłuszcz: " + fat);
                Picasso.get().load(image).into(mMealSelectImage);
                if (mAuth.getCurrentUser().getUid().equals(meal_uid)) {
                    mMealRemoveBtn.setVisibility(View.VISIBLE);
                }
                if (snapshot.child("tags").child("vegetarian").getValue().equals("1"))
                    mVegetarianLabel.setVisibility(View.VISIBLE);
                if (snapshot.child("tags").child("vegan").getValue().equals("1"))
                    mVeganLabel.setVisibility(View.VISIBLE);
                if (snapshot.child("tags").child("gluten free").getValue().equals("1"))
                    mGlutenFreeLabel.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        mMealRemoveBtn.setOnClickListener(v -> {
            mDatabaseMeals.child(mMealKey).removeValue();
            Intent ProductsIntent = new Intent(MealSingleActivity.this, MealsActivity.class);
            startActivity(ProductsIntent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Product, ProductViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Product, ProductViewHolder>(
                Product.class,
                R.layout.product_select_row,
                ProductViewHolder.class,
                mDatabaseProducts
        ) {
            @Override
            protected void populateViewHolder(ProductViewHolder productViewHolder, Product product, int i) {
                String product_key = getRef(i).getKey();
                productViewHolder.setName(product.getName());
                productViewHolder.setImage(product.getImage());
                mDatabaseMeals.child(mMealKey).child("Products").get().addOnCompleteListener(task -> {
                    if (!task.getResult().hasChild(product_key)) {
                        productViewHolder.mView.findViewById(R.id.cardd).setVisibility(View.GONE);
                        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) productViewHolder.mView.getLayoutParams();
                        layoutParams.setMargins(0, 0, 0, 0);
                        productViewHolder.mView.setLayoutParams(layoutParams);
                    }

                });

            }
        };
        mProductList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final FirebaseAuth mAuth;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            mAuth = FirebaseAuth.getInstance();


        }


        public void setName(String name) {
            TextView product_name = mView.findViewById(R.id.productName);
            product_name.setText(name);
        }

        public void setImage(String image) {
            ImageView product_image = mView.findViewById(R.id.productImage);
            Picasso.get().load(image).into(product_image);
        }


    }
}