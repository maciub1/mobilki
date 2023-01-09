package com.example.mobilki;

import androidx.annotation.NonNull;
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

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class MealSelectActivity extends AppCompatActivity {
    private DatabaseReference mDatabaseMeals;
    private RecyclerView mMealList;
    private EditText mSearchField;
    private ImageButton mSearchBtn;
    private Button mVegetarianBtn;
    private Button mGlutenFreeBtn;
    private Button mVeganBtn;
    private Button mAllBtn;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_select);

        mDatabaseMeals = FirebaseDatabase.getInstance().getReference().child("Meals");
        mSearchField = (EditText) findViewById(R.id.MealSearch);
        mAuth = FirebaseAuth.getInstance();
        mSearchBtn = (ImageButton) findViewById(R.id.mealSearchBt);
        mMealList = (RecyclerView) findViewById(R.id.mealList);
        mMealList.setHasFixedSize(true);
        mMealList.setLayoutManager(new LinearLayoutManager(this));
        mVegetarianBtn = (Button) findViewById(R.id.vegetarianBt);
        mGlutenFreeBtn = (Button) findViewById(R.id.glutenFreeBt);
        mVeganBtn = (Button) findViewById(R.id.veganBt);
        mAllBtn = (Button) findViewById(R.id.allBt);
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
    }

    private void mealSearch(String searchText) {
        Query searchQuery = mDatabaseMeals.orderByChild("name").startAt(searchText).endAt(searchText+"\uf8ff");
        FirebaseRecyclerAdapter<Meal, MealsActivity.MealViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Meal, MealsActivity.MealViewHolder>(
                Meal.class,
                R.layout.product_row,
                MealsActivity.MealViewHolder.class,
                searchQuery
        ) {
            @Override
            protected void populateViewHolder(MealsActivity.MealViewHolder mealViewHolder, Meal meal, int i) {
                String meal_key = getRef(i).getKey();
                mealViewHolder.setName(meal.getName());
                mealViewHolder.setCalories(meal.getCalories());
                mealViewHolder.setImage(meal.getImage());
                mealViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("meal id",meal_key);
                        mDatabaseMeals.child(meal_key).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                returnIntent.putExtra("calories",(String) snapshot.child("calories").getValue());
                                returnIntent.putExtra("carbohydrates",(String) snapshot.child("carbohydrates").getValue());
                                returnIntent.putExtra("fat",(String) snapshot.child("fat").getValue());
                                returnIntent.putExtra("proteins",(String) snapshot.child("proteins").getValue());
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
                        });

                    }
                });
            }
        };
        mMealList.setAdapter(firebaseRecyclerAdapter);

    }

    private void showGlutenFree() {
        Query glutenFreeQuery = mDatabaseMeals.orderByChild("tags/gluten free").equalTo("1");
        FirebaseRecyclerAdapter<Meal, MealsActivity.MealViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Meal, MealsActivity.MealViewHolder>(
                Meal.class,
                R.layout.product_row,
                MealsActivity.MealViewHolder.class,
                glutenFreeQuery
        ) {
            @Override
            protected void populateViewHolder(MealsActivity.MealViewHolder mealViewHolder, Meal meal, int i) {
                String meal_key = getRef(i).getKey();
                mealViewHolder.setName(meal.getName());
                mealViewHolder.setCalories(meal.getCalories());
                mealViewHolder.setImage(meal.getImage());
                mealViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("meal id",meal_key);
                        mDatabaseMeals.child(meal_key).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                returnIntent.putExtra("calories",(String) snapshot.child("calories").getValue());
                                returnIntent.putExtra("carbohydrates",(String) snapshot.child("carbohydrates").getValue());
                                returnIntent.putExtra("fat",(String) snapshot.child("fat").getValue());
                                returnIntent.putExtra("proteins",(String) snapshot.child("proteins").getValue());
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
                        });


                    }
                });


            }
        };
        mMealList.setAdapter(firebaseRecyclerAdapter);
    }

    private void showVegan() {
        Query veganQuery = mDatabaseMeals.orderByChild("tags/vegan").equalTo("1");
        FirebaseRecyclerAdapter<Meal, MealsActivity.MealViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Meal, MealsActivity.MealViewHolder>(
                Meal.class,
                R.layout.product_row,
                MealsActivity.MealViewHolder.class,
                veganQuery
        ) {
            @Override
            protected void populateViewHolder(MealsActivity.MealViewHolder mealViewHolder, Meal meal, int i) {
                String meal_key = getRef(i).getKey();
                mealViewHolder.setName(meal.getName());
                mealViewHolder.setCalories(meal.getCalories());
                mealViewHolder.setImage(meal.getImage());
                mealViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("meal id",meal_key);
                        mDatabaseMeals.child(meal_key).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                returnIntent.putExtra("calories",(String) snapshot.child("calories").getValue());
                                returnIntent.putExtra("carbohydrates",(String) snapshot.child("carbohydrates").getValue());
                                returnIntent.putExtra("fat",(String) snapshot.child("fat").getValue());
                                returnIntent.putExtra("proteins",(String) snapshot.child("proteins").getValue());
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
                        });

                    }
                });

            }
        };
        mMealList.setAdapter(firebaseRecyclerAdapter);
    }

    private void showVegetarian() {
        Query vegetarianQuery = mDatabaseMeals.orderByChild("tags/vegetarian").equalTo("1");
        FirebaseRecyclerAdapter<Meal, MealsActivity.MealViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Meal, MealsActivity.MealViewHolder>(
                Meal.class,
                R.layout.product_row,
                MealsActivity.MealViewHolder.class,
                vegetarianQuery
        ) {
            @Override
            protected void populateViewHolder(MealsActivity.MealViewHolder mealViewHolder, Meal meal, int i) {
                String meal_key = getRef(i).getKey();
                mealViewHolder.setName(meal.getName());
                mealViewHolder.setCalories(meal.getCalories());
                mealViewHolder.setImage(meal.getImage());
                mealViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("meal id",meal_key);
                        mDatabaseMeals.child(meal_key).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                returnIntent.putExtra("calories",(String) snapshot.child("calories").getValue());
                                returnIntent.putExtra("carbohydrates",(String) snapshot.child("carbohydrates").getValue());
                                returnIntent.putExtra("fat",(String) snapshot.child("fat").getValue());
                                returnIntent.putExtra("proteins",(String) snapshot.child("proteins").getValue());
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
                        });

                    }
                });

            }
        };
        mMealList.setAdapter(firebaseRecyclerAdapter);
    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Meal, MealsActivity.MealViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Meal, MealsActivity.MealViewHolder>(
                Meal.class,
                R.layout.product_row,
                MealsActivity.MealViewHolder.class,
                mDatabaseMeals
        ) {
            @Override
            protected void populateViewHolder(MealsActivity.MealViewHolder mealViewHolder, Meal meal, int i) {
                String meal_key = getRef(i).getKey();
                mealViewHolder.setName(meal.getName());
                mealViewHolder.setCalories(meal.getCalories());
                mealViewHolder.setImage(meal.getImage());
                mealViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("meal id",meal_key);
                        mDatabaseMeals.child(meal_key).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                returnIntent.putExtra("calories",(String) snapshot.child("calories").getValue());
                                returnIntent.putExtra("carbohydrates",(String) snapshot.child("carbohydrates").getValue());
                                returnIntent.putExtra("fat",(String) snapshot.child("fat").getValue());
                                returnIntent.putExtra("proteins",(String) snapshot.child("proteins").getValue());
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
                        });

                    }
                });

            }
        };
        mMealList.setAdapter(firebaseRecyclerAdapter);
    }
}