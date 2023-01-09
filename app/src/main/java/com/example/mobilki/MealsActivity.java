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

public class MealsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private FirebaseAuth mAuth;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    View header;

    private TextView usernameTv;
    private ImageView userImg;
    private Button guestLogin;
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference mDatabaseMeals;
    private Button mAddBtn;
    private RecyclerView mMealList;
    private EditText mSearchField;
    private ImageButton mSearchBtn;
    private Button mVegetarianBtn;
    private Button mGlutenFreeBtn;
    private Button mVeganBtn;
    private Button mAllBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meals);

        mAddBtn = findViewById(R.id.addMealBtn);
        mAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent AddMealIntent=new Intent(MealsActivity.this,
                        MealAddActivity.class);
                startActivity(AddMealIntent);
            }
        });
        mAuth = FirebaseAuth.getInstance();
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        header = navigationView.getHeaderView(0);
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseMeals = FirebaseDatabase.getInstance().getReference().child("Meals");
        mSearchField = findViewById(R.id.mealSearchField);
        mSearchBtn = findViewById(R.id.mealSearchBtn);
        mMealList = findViewById(R.id.meal_list);
        mMealList.setHasFixedSize(true);
        mMealList.setLayoutManager(new LinearLayoutManager(this));
        mVegetarianBtn = findViewById(R.id.vegetarianB);
        mGlutenFreeBtn = findViewById(R.id.glutenFreeB);
        mVeganBtn = findViewById(R.id.veganB);
        mAllBtn = findViewById(R.id.allB);
        mVegetarianBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showVegetarian();
            }
        });

        mVeganBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showVegan();
            }
        });

        mGlutenFreeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGlutenFree();
            }
        });

        mAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStart();
            }
        });

        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchText = mSearchField.getText().toString();
                mealSearch(searchText);
            }
        });

        usernameTv = header.findViewById(R.id.usernameTv);
        userImg = header.findViewById(R.id.profile_img);
        guestLogin = header.findViewById(R.id.guest_log_in);
        if(mAuth.getCurrentUser().isAnonymous())
        {
            mAddBtn.setVisibility(View.GONE);
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        //You are here - check home item
        navigationView.setCheckedItem(R.id.nav_meals);

        if(!mAuth.getCurrentUser().isAnonymous()) {
            SetProfile();
        }
        else
        {
            userImg.setImageResource(0);
            guestLogin.setVisibility(View.VISIBLE);
            navigationView.getMenu().findItem(R.id.nav_logout).setTitle(R.string.exit);
            guestLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(MealsActivity.this, MainActivity.class));
                }
            });
        }
    }

    private void mealSearch(String searchText) {
        Query searchQuery = mDatabaseMeals.orderByChild("name").startAt(searchText).endAt(searchText+"\uf8ff");
        FirebaseRecyclerAdapter<Meal,MealViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Meal, MealViewHolder>(
                Meal.class,
                R.layout.product_row,
                MealViewHolder.class,
                searchQuery
        ) {
            @Override
            protected void populateViewHolder(MealViewHolder mealViewHolder, Meal meal, int i) {
                String meal_key = getRef(i).getKey();
                mealViewHolder.setName(meal.getName());
                mealViewHolder.setCalories(meal.getCalories());
                mealViewHolder.setImage(meal.getImage());
                mealViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent singleMealIntent = new Intent(MealsActivity.this,MealSingleActivity.class);
                        singleMealIntent.putExtra("meal_id",meal_key);
                        startActivity(singleMealIntent);

                    }
                });


            }
        };
        mMealList.setAdapter(firebaseRecyclerAdapter);

    }

    private void showGlutenFree() {
        Query glutenFreeQuery = mDatabaseMeals.orderByChild("tags/gluten free").equalTo("1");
        FirebaseRecyclerAdapter<Meal,MealViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Meal, MealViewHolder>(
                Meal.class,
                R.layout.product_row,
                MealViewHolder.class,
                glutenFreeQuery
        ) {
            @Override
            protected void populateViewHolder(MealViewHolder mealViewHolder, Meal meal, int i) {
                String meal_key = getRef(i).getKey();
                mealViewHolder.setName(meal.getName());
                mealViewHolder.setCalories(meal.getCalories());
                mealViewHolder.setImage(meal.getImage());
                mealViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent singleMealIntent = new Intent(MealsActivity.this,MealSingleActivity.class);
                        singleMealIntent.putExtra("meal_id",meal_key);
                        startActivity(singleMealIntent);

                    }
                });

            }
        };
        mMealList.setAdapter(firebaseRecyclerAdapter);
    }

    private void showVegan() {
        Query veganQuery = mDatabaseMeals.orderByChild("tags/vegan").equalTo("1");
        FirebaseRecyclerAdapter<Meal,MealViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Meal, MealViewHolder>(
                Meal.class,
                R.layout.product_row,
                MealViewHolder.class,
                veganQuery
        ) {
            @Override
            protected void populateViewHolder(MealViewHolder mealViewHolder, Meal meal, int i) {
                String meal_key = getRef(i).getKey();
                mealViewHolder.setName(meal.getName());
                mealViewHolder.setCalories(meal.getCalories());
                mealViewHolder.setImage(meal.getImage());
                mealViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent singleMealIntent = new Intent(MealsActivity.this,MealSingleActivity.class);
                        singleMealIntent.putExtra("meal_id",meal_key);
                        startActivity(singleMealIntent);

                    }
                });

            }
        };
        mMealList.setAdapter(firebaseRecyclerAdapter);
    }

    private void showVegetarian() {
        Query vegetarianQuery = mDatabaseMeals.orderByChild("tags/vegetarian").equalTo("1");
        FirebaseRecyclerAdapter<Meal,MealViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Meal, MealViewHolder>(
                Meal.class,
                R.layout.product_row,
                MealViewHolder.class,
                vegetarianQuery
        ) {
            @Override
            protected void populateViewHolder(MealViewHolder mealViewHolder, Meal meal, int i) {
                String meal_key = getRef(i).getKey();
                mealViewHolder.setName(meal.getName());
                mealViewHolder.setCalories(meal.getCalories());
                mealViewHolder.setImage(meal.getImage());
                mealViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent singleMealIntent = new Intent(MealsActivity.this,MealSingleActivity.class);
                        singleMealIntent.putExtra("meal_id",meal_key);
                        startActivity(singleMealIntent);

                    }
                });


            }
        };
        mMealList.setAdapter(firebaseRecyclerAdapter);
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
                Intent intent = new Intent(MealsActivity.this, MainScreenActivity.class);
                startActivity(intent);
                break;

            case R.id.nav_products:
                Intent productsIntent = new Intent(MealsActivity.this, ProductActivity.class);
                startActivity(productsIntent);
                break;
            case R.id.nav_meals:
                break;

            /*case R.id.nav_diets:
                Intent dietsIntent = new Intent(MealsActivity.this, DietActivity.class);
                startActivity(dietsIntent);
                break;


            case R.id.nav_settings:
                Intent settingsIntent = new Intent(MealsActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;*/
            case R.id.nav_logout:
                //Log out user
                //...
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MealsActivity.this, MainActivity.class));
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

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Meal,MealViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Meal, MealViewHolder>(
                Meal.class,
                R.layout.product_row,
                MealViewHolder.class,
                mDatabaseMeals
        ) {
            @Override
            protected void populateViewHolder(MealViewHolder mealViewHolder, Meal meal, int i) {
                String meal_key = getRef(i).getKey();
                mealViewHolder.setName(meal.getName());
                mealViewHolder.setCalories(meal.getCalories());
                mealViewHolder.setImage(meal.getImage());
                mealViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent singleMealIntent = new Intent(MealsActivity.this,MealSingleActivity.class);
                        singleMealIntent.putExtra("meal_id",meal_key);
                        startActivity(singleMealIntent);

                    }
                });

            }
        };
        mMealList.setAdapter(firebaseRecyclerAdapter);
    }
    public static class MealViewHolder extends RecyclerView.ViewHolder{
        View mView;
        ImageButton mLikeBtn;
        ImageButton mDislikeBtn;
        public MealViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            mLikeBtn = mView.findViewById(R.id.likeBtn);
            mDislikeBtn = mView.findViewById(R.id.dislikeBtn);
            mLikeBtn.setVisibility(View.GONE);
            mDislikeBtn.setVisibility(View.GONE);

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
}