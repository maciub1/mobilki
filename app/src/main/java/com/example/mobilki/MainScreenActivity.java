package com.example.mobilki;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;

public class MainScreenActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference mDatabaseDiets;

    private FirebaseAuth mAuth;
    private String uid;

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    View header;

    private double progressCalories = 0;
    private double progressCarbohydrates = 0;
    private double progressProteins = 0;
    private double progressFats = 0;

    private ProgressBar progressBarCalories;
    private ProgressBar progressBarCarbohydrates;
    private ProgressBar progressBarProteins;
    private ProgressBar progressBarFats;

    private TextView progressCaloriesTv;
    private TextView progressCarbohydratesTv;
    private TextView progressProteinsTv;
    private TextView progressFatsTv;
    private DatabaseReference mDatabaseMeals;
    private int day;

    boolean doubleBack = false;

    private TextView usernameTv;
    private ImageView userImg;
    private RecyclerView mEatenMeals;
    private RecyclerView mObservedMeals;
    private static final int MEAL_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        mAuth = FirebaseAuth.getInstance();
        FirebaseAuth.AuthStateListener mAuthListener = firebaseAuth -> {
            if (firebaseAuth.getCurrentUser() == null) {
                Intent loginIntent = new Intent(MainScreenActivity.this, MainActivity.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(loginIntent);
            }
        };

        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseMeals = FirebaseDatabase.getInstance().getReference().child("Meals");
        mDatabaseDiets = FirebaseDatabase.getInstance().getReference().child("Diets");

        mDatabaseUsers.keepSynced(true);
        uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        mEatenMeals = findViewById(R.id.EatenMeals);
        TextView mLoginMsg = findViewById(R.id.loginMsg);
        Button mAddMealToEatenBtn = findViewById(R.id.addMealtoEatenBtn);
        mEatenMeals.setNestedScrollingEnabled(false);
        mEatenMeals.setHasFixedSize(false);
        mEatenMeals.setLayoutManager(new LinearLayoutManager(this));
        mObservedMeals = findViewById(R.id.ObservedMeals);
        mObservedMeals.setNestedScrollingEnabled(false);
        mObservedMeals.setHasFixedSize(false);
        mObservedMeals.setLayoutManager(new LinearLayoutManager(this));
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
        navigationView.setCheckedItem(R.id.nav_home);


        /*decreaseBtn = findViewById(R.id.button_decrease);
        increaseBtn = findViewById(R.id.button_increase);*/

        progressBarCalories = findViewById(R.id.calories_progress_bar);
        progressBarCarbohydrates = findViewById(R.id.carbohydrate_progress_bar);
        progressBarProteins = findViewById(R.id.protein_progress_bar);
        progressBarFats = findViewById(R.id.fat_progress_bar);

        progressCaloriesTv = findViewById(R.id.calories_progress_tv);
        progressCarbohydratesTv = findViewById(R.id.carbohydrate_progress_tv);
        progressProteinsTv = findViewById(R.id.protein_progress_tv);
        progressFatsTv = findViewById(R.id.fat_progress_tv);


        checkUserExist();

        if (!mAuth.getCurrentUser().isAnonymous()) {
            SetProfile();
            mDatabaseUsers.child(uid).get().addOnCompleteListener(task -> {
                if (task.getResult().hasChild("day"))
                    day = Integer.parseInt(task.getResult().child("day").getValue().toString());
            });
        } else {
            userImg.setImageResource(0);
            guestLogin.setVisibility(View.VISIBLE);
            navigationView.getMenu().findItem(R.id.nav_logout).setTitle(R.string.exit);
            guestLogin.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainScreenActivity.this, MainActivity.class));
            });

        }

        if (mAuth.getCurrentUser().isAnonymous()) {
            mLoginMsg.setVisibility(View.VISIBLE);
            mAddMealToEatenBtn.setVisibility(View.GONE);
            mEatenMeals.setVisibility(View.GONE);
        } else {
            mDatabaseUsers.child(uid).get().addOnCompleteListener(task -> {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                if (!task.getResult().child("last logged").getValue().toString().equals(dateFormat.format(Calendar.getInstance().getTime()))) {
                    mDatabaseUsers.child(uid).child("last logged").setValue(dateFormat.format(Calendar.getInstance().getTime()));
                    mDatabaseUsers.child(uid).child("Eaten").removeValue();
                    if (task.getResult().hasChild("day")) {
                        day = Integer.parseInt(task.getResult().child("day").getValue().toString());
                        day++;
                        mDatabaseDiets.child(task.getResult().child("observed diet").getValue().toString()).get().addOnCompleteListener(task1 -> {
                            if (day > task1.getResult().child("Meals").getChildrenCount())
                                day = 1;
                            mDatabaseUsers.child(uid).child("day").setValue(Integer.toString(day));


                        });

                    }
                }
            });
            mDatabaseUsers.child(uid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    updateProgressBars();
                    FirebaseRecyclerAdapter<Meal, MealViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Meal, MealViewHolder>(
                            Meal.class,
                            R.layout.product_select_row,
                            MealViewHolder.class,
                            mDatabaseMeals
                    ) {
                        @Override
                        protected void populateViewHolder(MealViewHolder mealViewHolder, Meal meal, int i) {
                            String meal_key = getRef(i).getKey();
                            mealViewHolder.setName(meal.getName());
                            mealViewHolder.setImage(meal.getImage());
                            assert meal_key != null;
                            if (!snapshot.child("Eaten").hasChild(meal_key)) {
                                mealViewHolder.mView.findViewById(R.id.cardd).setVisibility(View.GONE);
                                RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) mealViewHolder.mView.getLayoutParams();
                                layoutParams.setMargins(0, 0, 0, 0);
                                mealViewHolder.mView.setLayoutParams(layoutParams);
                            }

                        }
                    };
                    mEatenMeals.setAdapter(firebaseRecyclerAdapter);
                    if (snapshot.hasChild("observed diet")) {
                        FirebaseRecyclerAdapter<Meal, MealViewHolder2> firebaseRecyclerAdapter1 = new FirebaseRecyclerAdapter<Meal, MealViewHolder2>(
                                Meal.class,
                                R.layout.observed_row,
                                MealViewHolder2.class,
                                mDatabaseMeals

                        ) {
                            @Override
                            protected void populateViewHolder(MealViewHolder2 mealViewHolder2, Meal meal2, int i) {
                                String meal_key = getRef(i).getKey();
                                mealViewHolder2.setName(meal2.getName());
                                mealViewHolder2.setImage(meal2.getImage());
                                mealViewHolder2.mEatBtn.setOnClickListener(v -> mDatabaseUsers.child(uid).child("Eaten").child(meal_key).setValue("meal id from observed diet"));
                                mDatabaseDiets.child(Objects.requireNonNull(snapshot.child("observed diet").getValue()).toString()).child("Meals").child(Integer.toString(day)).get().addOnCompleteListener(task -> {
                                    if (snapshot.child("Eaten").hasChild(meal_key) || !task.getResult().hasChild(meal_key)) {
                                        mealViewHolder2.mView.findViewById(R.id.observedCard).setVisibility(View.GONE);
                                        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) mealViewHolder2.mView.getLayoutParams();
                                        layoutParams.setMargins(0, 0, 0, 0);
                                        mealViewHolder2.mView.setLayoutParams(layoutParams);
                                    }
                                });

                            }
                        };
                        mObservedMeals.setAdapter(firebaseRecyclerAdapter1);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        /*mAddMealToEatenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ProductSelectIntent = new Intent(MainScreenActivity.this, MealSelectActivity.class);
                startActivityForResult(ProductSelectIntent, MEAL_REQUEST);
            }
        });*/


    }

    private void SetProfile() {
        mDatabaseUsers.child(Objects.requireNonNull(mAuth.getUid())).child("name").addValueEventListener(new ValueEventListener() {
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
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            if (doubleBack) {
                finishAffinity();
                super.onBackPressed();
            } else {
                doubleBack = true;
                Toast.makeText(MainScreenActivity.this, R.string.click_back_again_to_exit, Toast.LENGTH_SHORT).show();

                new Handler(Looper.getMainLooper()).postDelayed(() -> doubleBack = false, 2000);
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
    {
        switch (menuItem.getItemId())
        {
            case R.id.nav_home:
                //You are here
                break;
            /*case R.id.nav_diets:
                Intent dietsIntent = new Intent(MainScreenActivity.this, DietActivity.class);
                startActivity(dietsIntent);
                break;

            case R.id.nav_products:
                //Go to product activity
                Intent intent = new Intent(MainScreenActivity.this, ProductActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_meals:
                Intent mealsIntent = new Intent(MainScreenActivity.this, MealsActivity.class);
                startActivity(mealsIntent);
                break;
            case R.id.nav_settings:
                Intent settingsIntent = new Intent(MainScreenActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;*/
            case R.id.nav_logout:
                //Log out user
                //...
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainScreenActivity.this, MainActivity.class));
        }

        drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    private void updateProgressBars()
    {
        mDatabaseUsers.child(uid).child("Eaten").get().addOnCompleteListener(task -> {
            for(DataSnapshot meal : task.getResult().getChildren()){
                mDatabaseMeals.child(meal.getKey()).get().addOnCompleteListener(task1 -> {
                    progressCalories += Double.parseDouble(task1.getResult().child("calories").getValue().toString());
                    progressCarbohydrates += Double.parseDouble(task1.getResult().child("carbohydrates").getValue().toString());
                    progressFats += Double.parseDouble(task1.getResult().child("fat").getValue().toString());
                    progressProteins += Double.parseDouble(task1.getResult().child("proteins").getValue().toString());
                });
            }
            mDatabaseUsers.child(uid).get().addOnCompleteListener(task12 -> {
                progressCalories = (progressCalories/Integer.parseInt(task12.getResult().child("calories req").getValue().toString()))*100.0;
                progressCarbohydrates = (progressCarbohydrates/Integer.parseInt(task12.getResult().child("carbohydrates req").getValue().toString()))*100.0;
                progressProteins = (progressProteins/Integer.parseInt(task12.getResult().child("proteins req").getValue().toString()))*100.0;
                progressFats = (progressFats/Integer.parseInt(task12.getResult().child("fat req").getValue().toString()))*100.0;
                progressCalories = Math.round(progressCalories);
                progressCarbohydrates = Math.round(progressCarbohydrates);
                progressFats = Math.round(progressFats);
                progressProteins = Math.round(progressProteins);
                progressBarCalories.setProgress((int)progressCalories);
                progressBarCarbohydrates.setProgress((int) progressCarbohydrates);
                progressBarProteins.setProgress((int) progressProteins);
                progressBarFats.setProgress((int) progressFats);

                progressCaloriesTv.setText((int) progressCalories + "%");
                progressCarbohydratesTv.setText((int) progressCarbohydrates + "%");
                progressProteinsTv.setText((int) progressProteins + "%");
                progressFatsTv.setText((int) progressFats + "%");
            });

        });



        /*progressBar.setProgress(progress);
        progressBarTV.setText(Integer.toString(progress) + "%");*/
    }

    private void checkUserExist() {
        if (mAuth.getCurrentUser() != null) {
            String user_id = mAuth.getCurrentUser().getUid();

            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.hasChild(user_id) && !mAuth.getCurrentUser().isAnonymous()) {
                        Intent setupIntent = new Intent(MainScreenActivity.this, SetupActivity.class);
                        setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(setupIntent);

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==MEAL_REQUEST && resultCode == RESULT_OK){
            assert data != null;
            String meal_id = data.getStringExtra("meal id");
            mDatabaseUsers.child(uid).child("Eaten").child(meal_id).setValue("meal id");
        }
    }

    public static class MealViewHolder extends RecyclerView.ViewHolder{
        final View mView;
        public MealViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

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

    public static class MealViewHolder2 extends RecyclerView.ViewHolder{
        final View mView;
        final Button mEatBtn;
        public MealViewHolder2(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            mEatBtn = mView.findViewById(R.id.eatBtn);

        }
        public void setName(String name){
            TextView product_name = mView.findViewById(R.id.productName2);
            product_name.setText(name);
        }

        public void setImage(String image){
            ImageView product_image = mView.findViewById(R.id.productImage2);
            Picasso.get().load(image).into(product_image);
        }
    }

}