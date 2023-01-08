package com.example.mobilki;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProductActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private FirebaseAuth mAuth;
    private RecyclerView mProductList;
    private EditText mSearchField;

    private DatabaseReference mDatabaseUsers;
    private DatabaseReference mDatabaseProducts;


    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    View header;

    private TextView usernameTv;
    private ImageView userImg;

    private boolean mProcessLike = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        Button mAddBtn = findViewById(R.id.addBtn);
        mAuth = FirebaseAuth.getInstance();
        mSearchField = findViewById(R.id.productSearchField);
        ImageButton mSearchBtn = findViewById(R.id.productSearchBtn);
        mProductList = findViewById(R.id.product_list);
        mProductList.setHasFixedSize(true);
        mProductList.setLayoutManager(new LinearLayoutManager(this));
        Button mLikesBtn = findViewById(R.id.likesBtn);
        Button mDislikesBtn = findViewById(R.id.dislikesBtn);
        Button mVegetarianBtn = findViewById(R.id.vegetarianBtn);
        Button mGlutenFreeBtn = findViewById(R.id.glutenFreeBtn);
        Button mVeganBtn = findViewById(R.id.veganBtn);
        Button mAllBtn = findViewById(R.id.allBtn);


        if(mAuth.getCurrentUser().isAnonymous())
        {
            mAddBtn.setVisibility(View.GONE);
            mLikesBtn.setVisibility(View.GONE);
            mDislikesBtn.setVisibility(View.GONE);
        }

        mLikesBtn.setOnClickListener(v -> showLikes());

        mDislikesBtn.setOnClickListener(v -> showDislikes());

        mVegetarianBtn.setOnClickListener(v -> showVegetarian());

        mVeganBtn.setOnClickListener(v -> showVegan());

        mGlutenFreeBtn.setOnClickListener(v -> showGlutenFree());

        mAllBtn.setOnClickListener(v -> onStart());

        mAddBtn.setOnClickListener(view -> {
            Intent AddProductIntent=new Intent(ProductActivity.this,
                    ProductAddActivity.class);
            startActivity(AddProductIntent);
        });

        mSearchBtn.setOnClickListener(v -> {
            String searchText = mSearchField.getText().toString();
            productSearch(searchText);
        });

        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseProducts = FirebaseDatabase.getInstance().getReference().child("Products");


        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        header = navigationView.getHeaderView(0);

        usernameTv = header.findViewById(R.id.usernameTv);
        userImg = header.findViewById(R.id.profile_img);
        Button guestLogin = header.findViewById(R.id.guest_log_in);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        //You are here - check home item
        navigationView.setCheckedItem(R.id.nav_products);

        if(!mAuth.getCurrentUser().isAnonymous()) {
            SetProfile();
        }
        else
        {
            userImg.setImageResource(0);
            guestLogin.setVisibility(View.VISIBLE);
            navigationView.getMenu().findItem(R.id.nav_logout).setTitle(R.string.exit);
            guestLogin.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(ProductActivity.this, MainActivity.class));
            });
        }
    }

    private void showGlutenFree() {
        Query glutenFreeQuery = mDatabaseProducts.orderByChild("tags/gluten free").equalTo("1");

        FirebaseRecyclerAdapter<Product, ProductViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Product, ProductViewHolder>(
                Product.class,
                R.layout.product_row,
                ProductViewHolder.class,
                glutenFreeQuery
        ) {
            @Override
            protected void populateViewHolder(ProductViewHolder productViewHolder, Product product, int i) {
                String product_key = getRef(i).getKey();
                productViewHolder.setName(product.getName());
                productViewHolder.setCalories(product.getCalories());
                productViewHolder.setImage(product.getImage());

                productViewHolder.setLikeBtn(product_key);
                productViewHolder.setDislikeBtn(product_key);

                productViewHolder.mView.setOnClickListener(v -> {
                    Intent singleProductIntent = new Intent(ProductActivity.this,ProductSingleActivity.class);
                    singleProductIntent.putExtra("product_id",product_key);
                    startActivity(singleProductIntent);

                });
                productViewHolder.mLikeBtn.setOnClickListener(v -> {
                    mProcessLike = true;

                    mDatabaseProducts.child(product_key).child("likes").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(mProcessLike){
                                if(snapshot.hasChild(mAuth.getCurrentUser().getUid())){
                                    mDatabaseProducts.child(product_key).child("likes").child(mAuth.getCurrentUser().getUid()).removeValue();
                                    mProcessLike = false;

                                } else{
                                    mDatabaseProducts.child(product_key).child("dislikes").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                                            if(task.getResult().hasChild(mAuth.getCurrentUser().getUid()))
                                            {
                                                Toast.makeText(ProductActivity.this, "Produkt jest już na liście nielubianych", Toast.LENGTH_LONG).show();
                                                mProcessLike = false;
                                            } else{
                                                mDatabaseProducts.child(product_key).child("likes").child(mAuth.getCurrentUser().getUid()).setValue(mAuth.getCurrentUser().getEmail());
                                                mProcessLike = false;
                                            }
                                        }
                                    });
                                }
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


                });

                productViewHolder.mDislikeBtn.setOnClickListener(v -> {
                    mProcessLike = true;
                    mDatabaseProducts.child(product_key).child("dislikes").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(mProcessLike){
                                if(snapshot.hasChild(mAuth.getCurrentUser().getUid())){
                                    mDatabaseProducts.child(product_key).child("dislikes").child(mAuth.getCurrentUser().getUid()).removeValue();
                                    mProcessLike = false;
                                }else{
                                    mDatabaseProducts.child(product_key).child("likes").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                                            if(task.getResult().hasChild(mAuth.getCurrentUser().getUid()))
                                            {
                                                Toast.makeText(ProductActivity.this, "Produkt jest już na liście lubianych", Toast.LENGTH_LONG).show();
                                                mProcessLike = false;
                                            } else{
                                                mDatabaseProducts.child(product_key).child("dislikes").child(mAuth.getCurrentUser().getUid()).setValue(mAuth.getCurrentUser().getEmail());
                                                mProcessLike = false;
                                            }
                                        }
                                    });
                                }
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                });



            }
        };

        mProductList.setAdapter(firebaseRecyclerAdapter);
    }

    private void showVegan() {
        Query veganQuery = mDatabaseProducts.orderByChild("tags/vegan").equalTo("1");

        FirebaseRecyclerAdapter<Product, ProductViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Product, ProductViewHolder>(
                Product.class,
                R.layout.product_row,
                ProductViewHolder.class,
                veganQuery
        ) {
            @Override
            protected void populateViewHolder(ProductViewHolder productViewHolder, Product product, int i) {
                String product_key = getRef(i).getKey();
                productViewHolder.setName(product.getName());
                productViewHolder.setCalories(product.getCalories());
                productViewHolder.setImage(product.getImage());

                productViewHolder.setLikeBtn(product_key);
                productViewHolder.setDislikeBtn(product_key);

                productViewHolder.mView.setOnClickListener(v -> {
                    Intent singleProductIntent = new Intent(ProductActivity.this,ProductSingleActivity.class);
                    singleProductIntent.putExtra("product_id",product_key);
                    startActivity(singleProductIntent);

                });
                productViewHolder.mLikeBtn.setOnClickListener(v -> {
                    mProcessLike = true;

                    mDatabaseProducts.child(product_key).child("likes").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(mProcessLike){
                                if(snapshot.hasChild(mAuth.getCurrentUser().getUid())){
                                    mDatabaseProducts.child(product_key).child("likes").child(mAuth.getCurrentUser().getUid()).removeValue();
                                    mProcessLike = false;

                                } else{
                                    mDatabaseProducts.child(product_key).child("dislikes").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                                            if(task.getResult().hasChild(mAuth.getCurrentUser().getUid()))
                                            {
                                                Toast.makeText(ProductActivity.this, "Produkt jest już na liście nielubianych", Toast.LENGTH_LONG).show();
                                                mProcessLike = false;
                                            } else{
                                                mDatabaseProducts.child(product_key).child("likes").child(mAuth.getCurrentUser().getUid()).setValue(mAuth.getCurrentUser().getEmail());
                                                mProcessLike = false;
                                            }
                                        }
                                    });
                                }
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


                });

                productViewHolder.mDislikeBtn.setOnClickListener(v -> {
                    mProcessLike = true;
                    mDatabaseProducts.child(product_key).child("dislikes").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(mProcessLike){
                                if(snapshot.hasChild(mAuth.getCurrentUser().getUid())){
                                    mDatabaseProducts.child(product_key).child("dislikes").child(mAuth.getCurrentUser().getUid()).removeValue();
                                    mProcessLike = false;
                                }else{
                                    mDatabaseProducts.child(product_key).child("likes").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                                            if(task.getResult().hasChild(mAuth.getCurrentUser().getUid()))
                                            {
                                                Toast.makeText(ProductActivity.this, "Produkt jest już na liście lubianych", Toast.LENGTH_LONG).show();
                                                mProcessLike = false;
                                            } else{
                                                mDatabaseProducts.child(product_key).child("dislikes").child(mAuth.getCurrentUser().getUid()).setValue(mAuth.getCurrentUser().getEmail());
                                                mProcessLike = false;
                                            }
                                        }
                                    });
                                }
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                });


            }
        };

        mProductList.setAdapter(firebaseRecyclerAdapter);
    }

    private void showVegetarian() {
        Query vegetarianQuery = mDatabaseProducts.orderByChild("tags/vegetarian").equalTo("1");

        FirebaseRecyclerAdapter<Product, ProductViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Product, ProductViewHolder>(
                Product.class,
                R.layout.product_row,
                ProductViewHolder.class,
                vegetarianQuery
        ) {
            @Override
            protected void populateViewHolder(ProductViewHolder productViewHolder, Product product, int i) {
                String product_key = getRef(i).getKey();
                productViewHolder.setName(product.getName());
                productViewHolder.setCalories(product.getCalories());
                productViewHolder.setImage(product.getImage());

                productViewHolder.setLikeBtn(product_key);
                productViewHolder.setDislikeBtn(product_key);

                productViewHolder.mView.setOnClickListener(v -> {
                    Intent singleProductIntent = new Intent(ProductActivity.this,ProductSingleActivity.class);
                    singleProductIntent.putExtra("product_id",product_key);
                    startActivity(singleProductIntent);

                });
                productViewHolder.mLikeBtn.setOnClickListener(v -> {
                    mProcessLike = true;

                    mDatabaseProducts.child(product_key).child("likes").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(mProcessLike){
                                if(snapshot.hasChild(mAuth.getCurrentUser().getUid())){
                                    mDatabaseProducts.child(product_key).child("likes").child(mAuth.getCurrentUser().getUid()).removeValue();
                                    mProcessLike = false;

                                } else{
                                    mDatabaseProducts.child(product_key).child("dislikes").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                                            if(task.getResult().hasChild(mAuth.getCurrentUser().getUid()))
                                            {
                                                Toast.makeText(ProductActivity.this, "Produkt jest już na liście nielubianych", Toast.LENGTH_LONG).show();
                                                mProcessLike = false;
                                            } else{
                                                mDatabaseProducts.child(product_key).child("likes").child(mAuth.getCurrentUser().getUid()).setValue(mAuth.getCurrentUser().getEmail());
                                                mProcessLike = false;
                                            }
                                        }
                                    });
                                }
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


                });

                productViewHolder.mDislikeBtn.setOnClickListener(v -> {
                    mProcessLike = true;
                    mDatabaseProducts.child(product_key).child("dislikes").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(mProcessLike){
                                if(snapshot.hasChild(mAuth.getCurrentUser().getUid())){
                                    mDatabaseProducts.child(product_key).child("dislikes").child(mAuth.getCurrentUser().getUid()).removeValue();
                                    mProcessLike = false;
                                }else{
                                    mDatabaseProducts.child(product_key).child("likes").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                                            if(task.getResult().hasChild(mAuth.getCurrentUser().getUid()))
                                            {
                                                Toast.makeText(ProductActivity.this, "Produkt jest już na liście lubianych", Toast.LENGTH_LONG).show();
                                                mProcessLike = false;
                                            } else{
                                                mDatabaseProducts.child(product_key).child("dislikes").child(mAuth.getCurrentUser().getUid()).setValue(mAuth.getCurrentUser().getEmail());
                                                mProcessLike = false;
                                            }
                                        }
                                    });
                                }
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                });


            }
        };

        mProductList.setAdapter(firebaseRecyclerAdapter);

    }

    private void showDislikes() {


        FirebaseRecyclerAdapter<Product, ProductViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Product, ProductViewHolder>(
                Product.class,
                R.layout.product_row,
                ProductViewHolder.class,
                mDatabaseProducts
        ) {
            @Override
            protected void populateViewHolder(ProductViewHolder productViewHolder, Product product, int  i) {
                String product_key = getRef(i).getKey();
                mDatabaseProducts.child(product_key).child("dislikes").get().addOnCompleteListener(task -> {
                    if(!task.getResult().hasChild(mAuth.getCurrentUser().getUid())){
                        productViewHolder.mView.findViewById(R.id.card).setVisibility(View.GONE);
                        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) productViewHolder.mView.getLayoutParams();
                        layoutParams.setMargins(0, 0, 0, 0);
                        productViewHolder.mView.setLayoutParams(layoutParams);
                    }else{
                        productViewHolder.mView.findViewById(R.id.card).setVisibility(View.VISIBLE);

                    }

                });

                productViewHolder.setName(product.getName());
                productViewHolder.setCalories(product.getCalories());
                productViewHolder.setImage(product.getImage());

                productViewHolder.setLikeBtn(product_key);
                productViewHolder.setDislikeBtn(product_key);

                productViewHolder.mView.setOnClickListener(v -> {
                    Intent singleProductIntent = new Intent(ProductActivity.this,ProductSingleActivity.class);
                    singleProductIntent.putExtra("product_id",product_key);
                    startActivity(singleProductIntent);

                });
                productViewHolder.mLikeBtn.setOnClickListener(v -> {
                    mProcessLike = true;

                    mDatabaseProducts.child(product_key).child("likes").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(mProcessLike){
                                if(snapshot.hasChild(mAuth.getCurrentUser().getUid())){
                                    mDatabaseProducts.child(product_key).child("likes").child(mAuth.getCurrentUser().getUid()).removeValue();
                                    mProcessLike = false;

                                } else{
                                    mDatabaseProducts.child(product_key).child("dislikes").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                                            if(task.getResult().hasChild(mAuth.getCurrentUser().getUid()))
                                            {
                                                Toast.makeText(ProductActivity.this, "Produkt jest już na liście nielubianych", Toast.LENGTH_LONG).show();
                                                mProcessLike = false;
                                            } else{
                                                mDatabaseProducts.child(product_key).child("likes").child(mAuth.getCurrentUser().getUid()).setValue(mAuth.getCurrentUser().getEmail());
                                                mProcessLike = false;
                                            }
                                        }
                                    });
                                }
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


                });

                productViewHolder.mDislikeBtn.setOnClickListener(v -> {
                    mProcessLike = true;
                    mDatabaseProducts.child(product_key).child("dislikes").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(mProcessLike){
                                if(snapshot.hasChild(mAuth.getCurrentUser().getUid())){
                                    mDatabaseProducts.child(product_key).child("dislikes").child(mAuth.getCurrentUser().getUid()).removeValue();
                                    mProcessLike = false;
                                }else{
                                    mDatabaseProducts.child(product_key).child("likes").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                                            if(task.getResult().hasChild(mAuth.getCurrentUser().getUid()))
                                            {
                                                Toast.makeText(ProductActivity.this, "Produkt jest już na liście lubianych", Toast.LENGTH_LONG).show();
                                                mProcessLike = false;
                                            } else{
                                                mDatabaseProducts.child(product_key).child("dislikes").child(mAuth.getCurrentUser().getUid()).setValue(mAuth.getCurrentUser().getEmail());
                                                mProcessLike = false;
                                            }
                                        }
                                    });
                                }
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                });

            }
        };

        mProductList.setAdapter(firebaseRecyclerAdapter);
    }

    private void showLikes() {

        FirebaseRecyclerAdapter<Product, ProductViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Product, ProductViewHolder>(
                Product.class,
                R.layout.product_row,
                ProductViewHolder.class,
                mDatabaseProducts
        ) {
            @Override
            protected void populateViewHolder(ProductViewHolder productViewHolder, Product product, int  i) {
                String product_key = getRef(i).getKey();
                mDatabaseProducts.child(product_key).child("likes").get().addOnCompleteListener(task -> {
                    if(!task.getResult().hasChild(mAuth.getCurrentUser().getUid())){
                        productViewHolder.mView.findViewById(R.id.card).setVisibility(View.GONE);
                        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) productViewHolder.mView.getLayoutParams();
                        layoutParams.setMargins(0, 0, 0, 0);
                        productViewHolder.mView.setLayoutParams(layoutParams);
                    }else{
                        productViewHolder.mView.findViewById(R.id.card).setVisibility(View.VISIBLE);

                    }

                });

                productViewHolder.setName(product.getName());
                productViewHolder.setCalories(product.getCalories());
                productViewHolder.setImage(product.getImage());

                productViewHolder.setLikeBtn(product_key);
                productViewHolder.setDislikeBtn(product_key);

                productViewHolder.mView.setOnClickListener(v -> {
                    Intent singleProductIntent = new Intent(ProductActivity.this,ProductSingleActivity.class);
                    singleProductIntent.putExtra("product_id",product_key);
                    startActivity(singleProductIntent);

                });
                productViewHolder.mLikeBtn.setOnClickListener(v -> {
                    mProcessLike = true;

                    mDatabaseProducts.child(product_key).child("likes").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(mProcessLike){
                                if(snapshot.hasChild(mAuth.getCurrentUser().getUid())){
                                    mDatabaseProducts.child(product_key).child("likes").child(mAuth.getCurrentUser().getUid()).removeValue();
                                    mProcessLike = false;

                                } else{
                                    mDatabaseProducts.child(product_key).child("dislikes").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                                            if(task.getResult().hasChild(mAuth.getCurrentUser().getUid()))
                                            {
                                                Toast.makeText(ProductActivity.this, "Produkt jest już na liście nielubianych", Toast.LENGTH_LONG).show();
                                                mProcessLike = false;
                                            } else{
                                                mDatabaseProducts.child(product_key).child("likes").child(mAuth.getCurrentUser().getUid()).setValue(mAuth.getCurrentUser().getEmail());
                                                mProcessLike = false;
                                            }
                                        }
                                    });
                                }
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


                });

                productViewHolder.mDislikeBtn.setOnClickListener(v -> {
                    mProcessLike = true;
                    mDatabaseProducts.child(product_key).child("dislikes").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(mProcessLike){
                                if(snapshot.hasChild(mAuth.getCurrentUser().getUid())){
                                    mDatabaseProducts.child(product_key).child("dislikes").child(mAuth.getCurrentUser().getUid()).removeValue();
                                    mProcessLike = false;
                                }else{
                                    mDatabaseProducts.child(product_key).child("likes").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                                            if(task.getResult().hasChild(mAuth.getCurrentUser().getUid()))
                                            {
                                                Toast.makeText(ProductActivity.this, "Produkt jest już na liście lubianych", Toast.LENGTH_LONG).show();
                                                mProcessLike = false;
                                            } else{
                                                mDatabaseProducts.child(product_key).child("dislikes").child(mAuth.getCurrentUser().getUid()).setValue(mAuth.getCurrentUser().getEmail());
                                                mProcessLike = false;
                                            }
                                        }
                                    });
                                }
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                });

            }
        };

        mProductList.setAdapter(firebaseRecyclerAdapter);
    }

    private void productSearch(String searchText) {
        Query searchQuery = mDatabaseProducts.orderByChild("name").startAt(searchText).endAt(searchText+"\uf8ff");
        //Query searchQuery = mDatabaseProducts.orderByChild("tags/gluten free").equalTo("1");

        FirebaseRecyclerAdapter<Product, ProductViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Product, ProductViewHolder>(
                Product.class,
                R.layout.product_row,
                ProductViewHolder.class,
                searchQuery
        ) {
            @Override
            protected void populateViewHolder(ProductViewHolder productViewHolder, Product product, int i) {
                String product_key = getRef(i).getKey();
                productViewHolder.setName(product.getName());
                productViewHolder.setCalories(product.getCalories());
                productViewHolder.setImage(product.getImage());

                productViewHolder.setLikeBtn(product_key);
                productViewHolder.setDislikeBtn(product_key);

                productViewHolder.mView.setOnClickListener(v -> {
                    Intent singleProductIntent = new Intent(ProductActivity.this,ProductSingleActivity.class);
                    singleProductIntent.putExtra("product_id",product_key);
                    startActivity(singleProductIntent);

                });
                productViewHolder.mLikeBtn.setOnClickListener(v -> {
                    mProcessLike = true;

                    mDatabaseProducts.child(product_key).child("likes").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(mProcessLike){
                                if(snapshot.hasChild(mAuth.getCurrentUser().getUid())){
                                    mDatabaseProducts.child(product_key).child("likes")
                                            .child(mAuth.getCurrentUser().getUid()).removeValue();
                                    mProcessLike = false;

                                } else{
                                    mDatabaseProducts.child(product_key).child("dislikes").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                                            if(task.getResult().hasChild(mAuth.getCurrentUser().getUid()))
                                            {
                                                Toast.makeText(ProductActivity.this, "Produkt jest już na liście nielubianych",
                                                        Toast.LENGTH_LONG).show();
                                                mProcessLike = false;
                                            } else{
                                                mDatabaseProducts.child(product_key).child("likes")
                                                        .child(mAuth.getCurrentUser().getUid()).
                                                        setValue(mAuth.getCurrentUser().getEmail());
                                                mProcessLike = false;
                                            }
                                        }
                                    });
                                }
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


                });

                productViewHolder.mDislikeBtn.setOnClickListener(v -> {
                    mProcessLike = true;
                    mDatabaseProducts.child(product_key).child("dislikes").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(mProcessLike){
                                if(snapshot.hasChild(mAuth.getCurrentUser().getUid())){
                                    mDatabaseProducts.child(product_key).child("dislikes").child(mAuth.getCurrentUser().getUid()).removeValue();
                                    mProcessLike = false;
                                }else{
                                    mDatabaseProducts.child(product_key).child("likes").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                                            if(task.getResult().hasChild(mAuth.getCurrentUser().getUid()))
                                            {
                                                Toast.makeText(ProductActivity.this, "Produkt jest już na liście lubianych", Toast.LENGTH_LONG).show();
                                                mProcessLike = false;
                                            } else{
                                                mDatabaseProducts.child(product_key).child("dislikes").child(mAuth.getCurrentUser().getUid()).setValue(mAuth.getCurrentUser().getEmail());
                                                mProcessLike = false;
                                            }
                                        }
                                    });
                                }
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
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
                R.layout.product_row,
                ProductViewHolder.class,
                mDatabaseProducts
        ) {
            @Override
            protected void populateViewHolder(ProductViewHolder productViewHolder, Product product, int i) {
                String product_key = getRef(i).getKey();
                productViewHolder.setName(product.getName());
                productViewHolder.setCalories(product.getCalories());
                productViewHolder.setImage(product.getImage());

                productViewHolder.setLikeBtn(product_key);
                productViewHolder.setDislikeBtn(product_key);

                productViewHolder.mView.setOnClickListener(v -> {
                    Intent singleProductIntent = new Intent(ProductActivity.this,ProductSingleActivity.class);
                    singleProductIntent.putExtra("product_id",product_key);
                    startActivity(singleProductIntent);

                });

                productViewHolder.mLikeBtn.setOnClickListener(v -> {
                    mProcessLike = true;

                    mDatabaseProducts.child(product_key).child("likes").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(mProcessLike){
                                if(snapshot.hasChild(mAuth.getCurrentUser().getUid())){
                                    mDatabaseProducts.child(product_key).child("likes").child(mAuth.getCurrentUser().getUid()).removeValue();
                                    mProcessLike = false;

                                } else{
                                    mDatabaseProducts.child(product_key).child("dislikes").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                                            if(task.getResult().hasChild(mAuth.getCurrentUser().getUid()))
                                            {
                                                Toast.makeText(ProductActivity.this, "Produkt jest już na liście nielubianych", Toast.LENGTH_LONG).show();
                                                mProcessLike = false;
                                            } else{
                                                mDatabaseProducts.child(product_key).child("likes").child(mAuth.getCurrentUser().getUid()).setValue(mAuth.getCurrentUser().getEmail());
                                                mProcessLike = false;
                                            }
                                        }
                                    });


                                }
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


                });

                productViewHolder.mDislikeBtn.setOnClickListener(v -> {
                    mProcessLike = true;
                    mDatabaseProducts.child(product_key).child("dislikes").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(mProcessLike){
                                if(snapshot.hasChild(mAuth.getCurrentUser().getUid())){
                                    mDatabaseProducts.child(product_key).child("dislikes").child(mAuth.getCurrentUser().getUid()).removeValue();
                                    mProcessLike = false;
                                }else{
                                    mDatabaseProducts.child(product_key).child("likes").get().addOnCompleteListener(task -> {
                                        if(task.getResult().hasChild(mAuth.getCurrentUser().getUid()))
                                        {
                                            Toast.makeText(ProductActivity.this, "Produkt jest już na liście lubianych", Toast.LENGTH_LONG).show();
                                            mProcessLike = false;
                                        } else{
                                            mDatabaseProducts.child(product_key).child("dislikes").child(mAuth.getCurrentUser().getUid()).setValue(mAuth.getCurrentUser().getEmail());
                                            mProcessLike = false;
                                        }
                                    });


                                }
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                });

            }
        };

        mProductList.setAdapter(firebaseRecyclerAdapter);


    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder{
        final View mView;
        final FirebaseAuth mAuth;

        final ImageButton mLikeBtn;
        final ImageButton mDislikeBtn;

        DatabaseReference mDatabaseLike;
        DatabaseReference mDatabaseDislike;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            mLikeBtn = mView.findViewById(R.id.likeBtn);
            mDislikeBtn = mView.findViewById(R.id.dislikeBtn);
            mAuth = FirebaseAuth.getInstance();
            if(mAuth.getCurrentUser().isAnonymous()){
                mLikeBtn.setVisibility(View.GONE);
                mDislikeBtn.setVisibility(View.GONE);
            }

        }

        public void setLikeBtn(String product_key){
            mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Products").child(product_key).child("likes");
            mDatabaseLike.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.hasChild(mAuth.getCurrentUser().getUid())){
                        mLikeBtn.setImageResource(R.mipmap.baseline_thumb_up_alt_black_24);

                    }else{
                        mLikeBtn.setImageResource(R.mipmap.baseline_thumb_up_off_alt_black_24);

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


        }

        public void setDislikeBtn(String product_key){
            mDatabaseDislike = FirebaseDatabase.getInstance().getReference().child("Products").child(product_key).child("dislikes");
            mDatabaseDislike.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.hasChild(mAuth.getCurrentUser().getUid())){
                        mDislikeBtn.setImageResource(R.mipmap.baseline_thumb_down_alt_black_24);

                    }else{
                        mDislikeBtn.setImageResource(R.mipmap.baseline_thumb_down_off_alt_black_24);

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


        }

        public void setName(String name){
            TextView product_name = mView.findViewById(R.id.product_name);
            product_name.setText(name);
        }

        public void setCalories(String calories){
            TextView product_calories = mView.findViewById(R.id.product_calories);
            product_calories.setText(calories+"kcal");
        }


        public void setImage(String image){
            ImageView product_image = mView.findViewById(R.id.product_image);
            Picasso.get().load(image).into(product_image);
        }

    }

    private void SetProfile()
    {
        mDatabaseUsers.child(mAuth.getUid()).child("name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String username = snapshot.getValue(String.class);

                usernameTv.setText(username);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mDatabaseUsers.child(mAuth.getUid()).child("image").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String image = snapshot.getValue(String.class);

                Picasso.get().load(image).into(userImg);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
    {
        switch (menuItem.getItemId())
        {
            case R.id.nav_home:
                //Go to main screen
                Intent intent = new Intent(ProductActivity.this, MainScreenActivity.class);
                startActivity(intent);
                break;
            /*case R.id.nav_diets:
                Intent dietsIntent = new Intent(ProductActivity.this, DietActivity.class);
                startActivity(dietsIntent);
                break;
            case R.id.nav_followed:
                Intent dieticianintent = new Intent(ProductActivity.this, DieticiansActivity.class);
                startActivity(dieticianintent);
                break;
            case R.id.nav_products:
                //You are here
                break;
            case R.id.nav_meals:
                Intent mealsIntent = new Intent(ProductActivity.this, MealsActivity.class);
                startActivity(mealsIntent);
                break;
            case R.id.nav_settings:
                Intent settingsIntent = new Intent(ProductActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;*/
            case R.id.nav_logout:
                //Log out user
                //...
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(ProductActivity.this, MainActivity.class));
        }

        drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START))
        {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else
        {
            finish();
        }
    }

}