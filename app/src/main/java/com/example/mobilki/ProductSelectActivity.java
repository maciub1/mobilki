package com.example.mobilki;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProductSelectActivity extends AppCompatActivity {
    private RecyclerView mProductList;
    private EditText mSearchField;

    private FirebaseAuth mAuth;

    private DatabaseReference mDatabaseProducts;
    private AlertDialog dialog;
    private EditText gramsField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_select);

        mSearchField = findViewById(R.id.productSearch);
        ImageButton mSearchBtn = findViewById(R.id.productSearchBt);
        mProductList = findViewById(R.id.productList);
        mProductList.setHasFixedSize(true);
        mProductList.setLayoutManager(new LinearLayoutManager(this));
        Button mLikesBtn = findViewById(R.id.likesBt);
        Button mDislikesBtn = findViewById(R.id.dislikesBt);
        Button mVegetarianBtn = findViewById(R.id.vegetarianBt);
        Button mGlutenFreeBtn = findViewById(R.id.glutenFreeBt);
        Button mVeganBtn = findViewById(R.id.veganBt);
        Button mAllBtn = findViewById(R.id.allBt);

        mAuth = FirebaseAuth.getInstance();

        DatabaseReference mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseProducts = FirebaseDatabase.getInstance().getReference().child("Products");
        mLikesBtn.setOnClickListener(v -> showLikes());

        mDislikesBtn.setOnClickListener(v -> showDislikes());

        mVegetarianBtn.setOnClickListener(v -> showVegetarian());

        mVeganBtn.setOnClickListener(v -> showVegan());

        mGlutenFreeBtn.setOnClickListener(v -> showGlutenFree());

        mAllBtn.setOnClickListener(v -> onStart());

        mSearchBtn.setOnClickListener(v -> {
            String searchText = mSearchField.getText().toString();
            productSearch(searchText);
        });
    }

    private void productSearch(String searchText) {
        Query searchQuery = mDatabaseProducts.orderByChild("name").startAt(searchText).endAt(searchText+"\uf8ff");
        FirebaseRecyclerAdapter<Product, ProductViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Product, ProductViewHolder>(
                Product.class,
                R.layout.product_select_row,
                ProductViewHolder.class,
                searchQuery
        ) {
            @Override
            protected void populateViewHolder(ProductViewHolder productViewHolder, Product product, int i) {
                String product_key = getRef(i).getKey();
                productViewHolder.setName(product.getName());
                productViewHolder.setImage(product.getImage());
                productViewHolder.mView.setOnClickListener(v -> {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("product id",product_key);
                    createNewGramsDialog(returnIntent,product_key);
                });

            }
        };
        mProductList.setAdapter(firebaseRecyclerAdapter);
    }

    private void showGlutenFree() {
        Query glutenFree = mDatabaseProducts.orderByChild("tags/gluten free").equalTo("1");
        FirebaseRecyclerAdapter<Product, ProductViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Product, ProductViewHolder>(
                Product.class,
                R.layout.product_select_row,
                ProductViewHolder.class,
                glutenFree
        ) {
            @Override
            protected void populateViewHolder(ProductViewHolder productViewHolder, Product product, int i) {
                String product_key = getRef(i).getKey();
                productViewHolder.setName(product.getName());
                productViewHolder.setImage(product.getImage());
                productViewHolder.mView.setOnClickListener(v -> {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("product id",product_key);
                    createNewGramsDialog(returnIntent,product_key);
                });

            }
        };
        mProductList.setAdapter(firebaseRecyclerAdapter);
    }

    private void showVegan() {
        Query veganQuery = mDatabaseProducts.orderByChild("tags/vegan").equalTo("1");
        FirebaseRecyclerAdapter<Product, ProductViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Product, ProductViewHolder>(
                Product.class,
                R.layout.product_select_row,
                ProductViewHolder.class,
                veganQuery
        ) {
            @Override
            protected void populateViewHolder(ProductViewHolder productViewHolder, Product product, int i) {
                String product_key = getRef(i).getKey();
                productViewHolder.setName(product.getName());
                productViewHolder.setImage(product.getImage());
                productViewHolder.mView.setOnClickListener(v -> {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("product id",product_key);
                    createNewGramsDialog(returnIntent,product_key);
                });

            }
        };
        mProductList.setAdapter(firebaseRecyclerAdapter);
    }

    private void showVegetarian() {
        Query vegetarianQuery = mDatabaseProducts.orderByChild("tags/vegetarian").equalTo("1");
        FirebaseRecyclerAdapter<Product, ProductViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Product, ProductViewHolder>(
                Product.class,
                R.layout.product_select_row,
                ProductViewHolder.class,
                vegetarianQuery
        ) {
            @Override
            protected void populateViewHolder(ProductViewHolder productViewHolder, Product product, int i) {
                String product_key = getRef(i).getKey();
                productViewHolder.setName(product.getName());
                productViewHolder.setImage(product.getImage());
                productViewHolder.mView.setOnClickListener(v -> {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("product id",product_key);
                    createNewGramsDialog(returnIntent,product_key);
                });

            }
        };
        mProductList.setAdapter(firebaseRecyclerAdapter);
    }

    private void showDislikes() {
        FirebaseRecyclerAdapter<Product, ProductViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Product, ProductViewHolder>(
                Product.class,
                R.layout.product_select_row,
                ProductViewHolder.class,
                mDatabaseProducts
        ) {
            @Override
            protected void populateViewHolder(ProductViewHolder productViewHolder, Product product, int i) {
                String product_key = getRef(i).getKey();
                mDatabaseProducts.child(product_key).child("dislikes").get().addOnCompleteListener(task -> {
                    if(!task.getResult().hasChild(mAuth.getCurrentUser().getUid())){
                        productViewHolder.mView.findViewById(R.id.cardd).setVisibility(View.GONE);
                        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) productViewHolder.mView.getLayoutParams();
                        layoutParams.setMargins(0, 0, 0, 0);
                        productViewHolder.mView.setLayoutParams(layoutParams);
                    }else{
                        productViewHolder.mView.findViewById(R.id.cardd).setVisibility(View.VISIBLE);

                    }

                });
                productViewHolder.setName(product.getName());
                productViewHolder.setImage(product.getImage());
                productViewHolder.mView.setOnClickListener(v -> {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("product id",product_key);
                    createNewGramsDialog(returnIntent,product_key);
                });
            }
        };
        mProductList.setAdapter(firebaseRecyclerAdapter);

    }

    private void showLikes() {
        FirebaseRecyclerAdapter<Product, ProductViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Product, ProductViewHolder>(
                Product.class,
                R.layout.product_select_row,
                ProductViewHolder.class,
                mDatabaseProducts
        ) {
            @Override
            protected void populateViewHolder(ProductViewHolder productViewHolder, Product product, int i) {
                String product_key = getRef(i).getKey();
                mDatabaseProducts.child(product_key).child("likes").get().addOnCompleteListener(task -> {
                    if(!task.getResult().hasChild(mAuth.getCurrentUser().getUid())){
                        productViewHolder.mView.findViewById(R.id.cardd).setVisibility(View.GONE);
                        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) productViewHolder.mView.getLayoutParams();
                        layoutParams.setMargins(0, 0, 0, 0);
                        productViewHolder.mView.setLayoutParams(layoutParams);
                    }else{
                        productViewHolder.mView.findViewById(R.id.cardd).setVisibility(View.VISIBLE);

                    }

                });
                productViewHolder.setName(product.getName());
                productViewHolder.setImage(product.getImage());
                productViewHolder.mView.setOnClickListener(v -> {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("product id",product_key);
                    createNewGramsDialog(returnIntent,product_key);
                });
            }
        };
        mProductList.setAdapter(firebaseRecyclerAdapter);
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
                productViewHolder.mView.setOnClickListener(v -> {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("product id",product_key);
                    createNewGramsDialog(returnIntent,product_key);
                });

            }
        };
        mProductList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder{
        final View mView;
        final FirebaseAuth mAuth;

        DatabaseReference mDatabaseLike;
        DatabaseReference mDatabaseDislike;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            mAuth = FirebaseAuth.getInstance();

        }



        public void setName(String name){
            TextView product_name = mView.findViewById(R.id.productName);
            product_name.setText(name);
        }

        public void setImage(String image){
            ImageView product_image = mView.findViewById(R.id.productImage);
            Picasso.get().load(image).into(product_image);
        }

    }
    public void createNewGramsDialog(Intent returnIntent, String product_key){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final View gramsPopupView = getLayoutInflater().inflate(R.layout.popup,null);
        gramsField = gramsPopupView.findViewById(R.id.gramsField);
        Button saveBtn = gramsPopupView.findViewById(R.id.saveBtn);
        Button cancelBtn = gramsPopupView.findViewById(R.id.cancelBtn);
        dialogBuilder.setView(gramsPopupView);
        dialog= dialogBuilder.create();
        dialog.show();
        cancelBtn.setOnClickListener(v -> dialog.dismiss());
        saveBtn.setOnClickListener(v -> mDatabaseProducts.child(product_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double calories =Double.parseDouble((String) snapshot.child("calories").getValue())/100.0*Integer.parseInt(gramsField.getText().toString());
                calories *= 100;
                calories = Math.round(calories);
                calories /= 100;
                returnIntent.putExtra("calories",Double.toString(calories));
                double carbohydrates =Double.parseDouble((String) snapshot.child("carbohydrates").getValue())/100.0*Integer.parseInt(gramsField.getText().toString());
                carbohydrates *= 100;
                carbohydrates = Math.round(carbohydrates);
                carbohydrates /= 100;
                returnIntent.putExtra("carbohydrates",Double.toString(carbohydrates));
                double fat =Double.parseDouble((String) snapshot.child("fat").getValue())/100.0*Integer.parseInt(gramsField.getText().toString());
                fat *= 100;
                fat = Math.round(fat);
                fat /= 100;
                returnIntent.putExtra("fat",Double.toString(fat));
                double proteins =Double.parseDouble((String) snapshot.child("proteins").getValue())/100.0*Integer.parseInt(gramsField.getText().toString());
                proteins *= 100;
                proteins = Math.round(proteins);
                proteins /= 100;
                returnIntent.putExtra("proteins",Double.toString(proteins));
                returnIntent.putExtra("name", snapshot.child("name").getValue().toString());
                returnIntent.putExtra("vegetarian",snapshot.child("tags").child("vegetarian").getValue().toString());
                returnIntent.putExtra("vegan",snapshot.child("tags").child("vegan").getValue().toString());
                returnIntent.putExtra("gluten free",snapshot.child("tags").child("gluten free").getValue().toString());
                setResult(Activity.RESULT_OK,returnIntent);
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        }));
    }
}