package com.example.mobilki;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class DietActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth mAuth;
    private RecyclerView mDietList;
    private EditText mSearchField;
    private ProgressDialog mProgress;

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    View header;
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference mDatabaseDiets;
    private DatabaseReference mDatabaseMeals;
    private DatabaseReference mDatabaseProducts;
    private TextView usernameTv;
    private ImageView userImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diet);

        Button mAddBtn = findViewById(R.id.addDietBtn);
        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseDiets = FirebaseDatabase.getInstance().getReference().child("Diets");
        mDatabaseMeals = FirebaseDatabase.getInstance().getReference().child("Meals");
        mDatabaseProducts = FirebaseDatabase.getInstance().getReference().child("Products");
        mSearchField = findViewById(R.id.DietSearchField);
        ImageButton mSearchBtn = findViewById(R.id.DietSearchBtn);
        mDietList = findViewById(R.id.diet_list);
        mProgress = new ProgressDialog(this);
        mDietList.setHasFixedSize(true);
        mDietList.setLayoutManager(new LinearLayoutManager(this));
        Button mLikesBtn = findViewById(R.id.likesDietBtn);
        Button mVegetarianBtn = findViewById(R.id.vegetarianDietBtn);
        Button mGlutenFreeBtn = findViewById(R.id.glutenFreeDietBtn);
        Button mVeganBtn = findViewById(R.id.veganDietBtn);
        Button mAllBtn = findViewById(R.id.allDietBtn);
        if (mAuth.getCurrentUser().isAnonymous()) {
            mAddBtn.setVisibility(View.GONE);
            mLikesBtn.setVisibility(View.GONE);
        }
        mAddBtn.setOnClickListener(view -> {
            Intent AddDietIntent = new Intent(DietActivity.this,
                    DietAddActivity.class);
            startActivity(AddDietIntent);
        });

        mSearchBtn.setOnClickListener(v -> {
            String searchText = mSearchField.getText().toString();
            dietSearch(searchText);
        });

        mVegetarianBtn.setOnClickListener(v -> showVegetarian());

        mVeganBtn.setOnClickListener(v -> showVegan());

        mGlutenFreeBtn.setOnClickListener(v -> showGlutenFree());

        mAllBtn.setOnClickListener(v -> onStart());
        mLikesBtn.setOnClickListener(v -> showLikes());

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
        navigationView.setCheckedItem(R.id.nav_diets);
        if (!mAuth.getCurrentUser().isAnonymous()) {
            SetProfile();
        } else {
            userImg.setImageResource(0);
            guestLogin.setVisibility(View.VISIBLE);
            navigationView.getMenu().findItem(R.id.nav_logout).setTitle(R.string.exit);
            guestLogin.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(DietActivity.this, MainActivity.class));
            });
        }
    }

    private void showLikes() {
        mProgress.setMessage("Please wait ...");
        mProgress.show();

        FirebaseRecyclerAdapter<Diet, DietViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Diet, DietViewHolder>(
                Diet.class,
                R.layout.diet_row,
                DietViewHolder.class,
                mDatabaseDiets
        ) {
            @Override
            protected void populateViewHolder(DietViewHolder dietViewHolder, Diet diet, int i) {
                String diet_key = getRef(i).getKey();
                dietViewHolder.setName(diet.getName());
                dietViewHolder.setImage(diet.getImage());
                dietViewHolder.setRating(diet.getRating());
                dietViewHolder.mView.setOnClickListener(v -> {
                    Intent singleDietIntent = new Intent(DietActivity.this, DietSingleActivity.class);
                    singleDietIntent.putExtra("diet_id", diet_key);
                    startActivity(singleDietIntent);
                });
                mDatabaseDiets.child(diet_key).child("Meals").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot day : snapshot.getChildren()) {
                            mDatabaseDiets.child(diet_key).child("Meals").child(day.getKey()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot meal : snapshot.getChildren()) {
                                        mDatabaseMeals.child(meal.getKey()).child("Products").addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for (DataSnapshot product : snapshot.getChildren()) {
                                                    mDatabaseProducts.child(product.getKey()).child("dislikes").addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            if (snapshot.hasChild(mAuth.getCurrentUser().getUid())) {
                                                                dietViewHolder.mView.findViewById(R.id.dietCard).setVisibility(View.GONE);
                                                                RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) dietViewHolder.mView.getLayoutParams();
                                                                layoutParams.setMargins(0, 0, 0, 0);
                                                                dietViewHolder.mView.setLayoutParams(layoutParams);
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });

                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                        mProgress.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        };
        mDietList.setAdapter(firebaseRecyclerAdapter);
    }

    private void showGlutenFree() {
        Query glutenFreeQuery = mDatabaseDiets.orderByChild("tags/gluten free").equalTo("1");
        FirebaseRecyclerAdapter<Diet, DietViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Diet, DietViewHolder>(
                Diet.class,
                R.layout.diet_row,
                DietViewHolder.class,
                glutenFreeQuery
        ) {
            @Override
            protected void populateViewHolder(DietViewHolder dietViewHolder, Diet diet, int i) {
                String diet_key = getRef(i).getKey();
                dietViewHolder.setName(diet.getName());
                dietViewHolder.setImage(diet.getImage());
                dietViewHolder.setRating(diet.getRating());
                dietViewHolder.mView.setOnClickListener(v -> {
                    Intent singleDietIntent = new Intent(DietActivity.this, DietSingleActivity.class);
                    singleDietIntent.putExtra("diet_id", diet_key);
                    startActivity(singleDietIntent);
                });

            }
        };
        mDietList.setAdapter(firebaseRecyclerAdapter);
    }

    private void showVegan() {
        Query veganQuery = mDatabaseDiets.orderByChild("tags/vegan").equalTo("1");
        FirebaseRecyclerAdapter<Diet, DietViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Diet, DietViewHolder>(
                Diet.class,
                R.layout.diet_row,
                DietViewHolder.class,
                veganQuery
        ) {
            @Override
            protected void populateViewHolder(DietViewHolder dietViewHolder, Diet diet, int i) {
                String diet_key = getRef(i).getKey();
                dietViewHolder.setName(diet.getName());
                dietViewHolder.setImage(diet.getImage());
                dietViewHolder.setRating(diet.getRating());
                dietViewHolder.mView.setOnClickListener(v -> {
                    Intent singleDietIntent = new Intent(DietActivity.this, DietSingleActivity.class);
                    singleDietIntent.putExtra("diet_id", diet_key);
                    startActivity(singleDietIntent);
                });

            }
        };
        mDietList.setAdapter(firebaseRecyclerAdapter);
    }

    private void showVegetarian() {
        Query vegetarianQuery = mDatabaseDiets.orderByChild("tags/vegetarian").equalTo("1");
        FirebaseRecyclerAdapter<Diet, DietViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Diet, DietViewHolder>(
                Diet.class,
                R.layout.diet_row,
                DietViewHolder.class,
                vegetarianQuery
        ) {
            @Override
            protected void populateViewHolder(DietViewHolder dietViewHolder, Diet diet, int i) {
                String diet_key = getRef(i).getKey();
                dietViewHolder.setName(diet.getName());
                dietViewHolder.setImage(diet.getImage());
                dietViewHolder.setRating(diet.getRating());
                dietViewHolder.mView.setOnClickListener(v -> {
                    Intent singleDietIntent = new Intent(DietActivity.this, DietSingleActivity.class);
                    singleDietIntent.putExtra("diet_id", diet_key);
                    startActivity(singleDietIntent);
                });

            }
        };
        mDietList.setAdapter(firebaseRecyclerAdapter);
    }

    private void dietSearch(String searchText) {
        Query searchQuery = mDatabaseDiets.orderByChild("name").startAt(searchText).endAt(searchText + "\uf8ff");
        FirebaseRecyclerAdapter<Diet, DietViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Diet, DietViewHolder>(
                Diet.class,
                R.layout.diet_row,
                DietViewHolder.class,
                searchQuery
        ) {
            @Override
            protected void populateViewHolder(DietViewHolder dietViewHolder, Diet diet, int i) {
                String diet_key = getRef(i).getKey();
                dietViewHolder.setName(diet.getName());
                dietViewHolder.setImage(diet.getImage());
                dietViewHolder.setRating(diet.getRating());
                dietViewHolder.mView.setOnClickListener(v -> {
                    Intent singleDietIntent = new Intent(DietActivity.this, DietSingleActivity.class);
                    singleDietIntent.putExtra("diet_id", diet_key);
                    startActivity(singleDietIntent);
                });

            }
        };
        mDietList.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Diet, DietViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Diet, DietViewHolder>(
                Diet.class,
                R.layout.diet_row,
                DietViewHolder.class,
                mDatabaseDiets
        ) {
            @Override
            protected void populateViewHolder(DietViewHolder dietViewHolder, Diet diet, int i) {
                String diet_key = getRef(i).getKey();
                dietViewHolder.setName(diet.getName());
                dietViewHolder.setImage(diet.getImage());
                dietViewHolder.setRating(diet.getRating());
                dietViewHolder.mView.setOnClickListener(v -> {
                    Intent singleDietIntent = new Intent(DietActivity.this, DietSingleActivity.class);
                    singleDietIntent.putExtra("diet_id", diet_key);
                    startActivity(singleDietIntent);
                });

            }
        };
        mDietList.setAdapter(firebaseRecyclerAdapter);

    }

    public static class DietViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final FirebaseAuth mAuth;

        public DietViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            mAuth = FirebaseAuth.getInstance();
        }


        public void setName(String name) {
            TextView diet_name = mView.findViewById(R.id.diet_name);
            diet_name.setText(name);
        }

        public void setImage(String image) {
            ImageView diet_image = mView.findViewById(R.id.diet_image);
            Picasso.get().load(image).into(diet_image);
        }

        public void setRating(String rating) {
            RatingBar diet_rating = mView.findViewById(R.id.diet_rating);
            diet_rating.setRating(Float.parseFloat(rating));
        }

    }

    private void SetProfile() {
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
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_home:

                Intent intent = new Intent(DietActivity.this, MainScreenActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_diets:
                break;
            case R.id.nav_products:
                Intent productsintent = new Intent(DietActivity.this, ProductActivity.class);
                startActivity(productsintent);
                break;
            case R.id.nav_meals:
                Intent mealsIntent = new Intent(DietActivity.this, MealsActivity.class);
                startActivity(mealsIntent);
                break;
            case R.id.nav_settings:
                Intent settingsIntent = new Intent(DietActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
            case R.id.nav_logout:
                //Log out user
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(DietActivity.this, MainActivity.class));
        }

        drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            finish();
        }
    }

}