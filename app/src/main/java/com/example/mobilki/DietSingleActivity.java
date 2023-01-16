package com.example.mobilki;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
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

public class DietSingleActivity extends AppCompatActivity {

    private String mDietKey = null;
    private ImageView mDietSelectImage;
    private TextView mDietName;
    private TextView mDietDesc;
    private Button mDietRemoveBtn;
    private Button mNextDayBtn;
    private Button mPreviousDayBtn;
    private Button mObserveBtn;
    private TextView mVegetarianLabel;
    private TextView mVeganLabel;
    private TextView mGlutenFreeLabel;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseMeals;
    private DatabaseReference mDatabaseDiets;
    private DatabaseReference mDatabaseUsers;
    private RecyclerView mMealsList;
    private RecyclerView mRatingsList;
    private RatingBar mAddRatingBar;
    private RatingBar mRatingBar;
    private EditText mAddComment;
    private Button mAddRatingBtn;
    private TextView mDayField;
    private double ratings;
    private long day = 1;
    private long numberOfDays = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diet_single);

        mDietKey = getIntent().getExtras().getString("diet_id");
        mDietSelectImage = findViewById(R.id.SingleDietImage);
        mDietName = findViewById(R.id.SingleDietName);
        mDietDesc = findViewById(R.id.SingleDietDesc);
        mDietRemoveBtn = findViewById(R.id.DietRemoveBtn);
        mNextDayBtn = findViewById(R.id.nextDayBtn);
        mPreviousDayBtn = findViewById(R.id.previousDayBtn);
        mObserveBtn = findViewById(R.id.DietObserveBtn);
        mVegetarianLabel = findViewById(R.id.vegetarianDietLabel);
        mVeganLabel = findViewById(R.id.veganDietLabel);
        mGlutenFreeLabel = findViewById(R.id.glutenFreeDietLabel);
        mAuth = FirebaseAuth.getInstance();
        mDatabaseMeals = FirebaseDatabase.getInstance().getReference().child("Meals");
        mDatabaseDiets = FirebaseDatabase.getInstance().getReference().child("Diets");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mRatingsList = findViewById(R.id.ratingsList);
        mRatingsList.setNestedScrollingEnabled(false);
        mRatingsList.setHasFixedSize(false);
        mRatingsList.setLayoutManager(new LinearLayoutManager(this));
        mMealsList = findViewById(R.id.mealsList);
        mMealsList.setNestedScrollingEnabled(false);
        mMealsList.setHasFixedSize(false);
        mMealsList.setLayoutManager(new LinearLayoutManager(this));
        mDayField = findViewById(R.id.SingleDietDay);
        mAddRatingBtn = findViewById(R.id.addRatingBtn);
        mAddComment = findViewById(R.id.commentAdd);
        mAddRatingBar = findViewById(R.id.addRatingBar);
        mRatingBar = findViewById(R.id.RatingBar);
        mAddRatingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            mAddComment.setVisibility(View.VISIBLE);
            mAddRatingBtn.setVisibility(View.VISIBLE);
        });
        mDatabaseDiets.child(mDietKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = (String) snapshot.child("name").getValue();
                String image = (String) snapshot.child("image").getValue();
                String diet_uid = (String) snapshot.child("uid").getValue();
                String desc = (String) snapshot.child("desc").getValue();
                mRatingBar.setRating(Float.parseFloat(snapshot.child("rating").getValue().toString()));
                numberOfDays = snapshot.child("Meals").getChildrenCount();
                mDietName.setText(name);
                mDietDesc.setText(desc);
                Picasso.get().load(image).into(mDietSelectImage);
                if (mAuth.getCurrentUser().getUid().equals(diet_uid)) {
                    mDietRemoveBtn.setVisibility(View.VISIBLE);
                }
                if (snapshot.child("tags").child("vegetarian").getValue().equals("1"))
                    mVegetarianLabel.setVisibility(View.VISIBLE);
                if (snapshot.child("tags").child("vegan").getValue().equals("1"))
                    mVeganLabel.setVisibility(View.VISIBLE);
                if (snapshot.child("tags").child("gluten free").getValue().equals("1"))
                    mGlutenFreeLabel.setVisibility(View.VISIBLE);
                if (mAuth.getCurrentUser().isAnonymous()) {
                    mObserveBtn.setVisibility(View.GONE);
                }
                if (numberOfDays == 1)
                    mNextDayBtn.setVisibility(View.INVISIBLE);
                if (snapshot.child("Ratings").hasChild(mAuth.getCurrentUser().getUid()) || mAuth.getCurrentUser().isAnonymous())
                    mAddRatingBar.setVisibility(View.GONE);

                FirebaseRecyclerAdapter<Rating, RatingViewHolder> firebaseRecyclerAdapter2 = new FirebaseRecyclerAdapter<Rating, RatingViewHolder>(
                        Rating.class,
                        R.layout.rating_row,
                        RatingViewHolder.class,
                        mDatabaseDiets.child(mDietKey).child("Ratings")
                ) {
                    @Override
                    protected void populateViewHolder(RatingViewHolder ratingViewHolder, Rating rating, int i) {
                        ratingViewHolder.setRating(rating.getRating());
                        ratingViewHolder.setImage(rating.getImage());
                        ratingViewHolder.setName(rating.getName());
                        ratingViewHolder.setComment(rating.getComment());

                    }
                };
                mRatingsList.setAdapter(firebaseRecyclerAdapter2);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mDietRemoveBtn.setOnClickListener(v -> {
            mDatabaseMeals.child(mDietKey).removeValue();
            Intent DietsIntent = new Intent(DietSingleActivity.this, DietActivity.class);
            startActivity(DietsIntent);
        });


        mObserveBtn.setOnClickListener(v -> {
            mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("observed diet").setValue(mDietKey);
            mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("day").setValue("1");
        });

        mNextDayBtn.setOnClickListener(v -> {
            day++;
            mPreviousDayBtn.setVisibility(View.VISIBLE);
            mDayField.setText("Meals in day: " + day + ":");
            if (day == numberOfDays)
                mNextDayBtn.setVisibility(View.INVISIBLE);
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
                    mDatabaseDiets.child(mDietKey).child("Meals").child(Long.toString(day)).get().addOnCompleteListener(task -> {
                        if (!task.getResult().hasChild(meal_key)) {
                            mealViewHolder.mView.findViewById(R.id.cardd).setVisibility(View.GONE);
                            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) mealViewHolder.mView.getLayoutParams();
                            layoutParams.setMargins(0, 0, 0, 0);
                            mealViewHolder.mView.setLayoutParams(layoutParams);
                        }
                    });
                }
            };
            mMealsList.setAdapter(firebaseRecyclerAdapter);
        });

        mPreviousDayBtn.setOnClickListener(v -> {
            day--;
            mNextDayBtn.setVisibility(View.VISIBLE);
            mDayField.setText("Meals in day: " + day + ":");
            if (day == 1)
                mPreviousDayBtn.setVisibility(View.INVISIBLE);
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
                    mDatabaseDiets.child(mDietKey).child("Meals").child(Long.toString(day)).get().addOnCompleteListener(task -> {
                        if (!task.getResult().hasChild(meal_key)) {
                            mealViewHolder.mView.findViewById(R.id.cardd).setVisibility(View.GONE);
                            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) mealViewHolder.mView.getLayoutParams();
                            layoutParams.setMargins(0, 0, 0, 0);
                            mealViewHolder.mView.setLayoutParams(layoutParams);
                        }
                    });
                }
            };
            mMealsList.setAdapter(firebaseRecyclerAdapter);
        });

        mAddRatingBtn.setOnClickListener(v -> {
            ratings = 0;
            mDatabaseDiets.child(mDietKey).child("Ratings").child(mAuth.getCurrentUser().getUid()).child("rating").setValue(Integer.toString((int) mAddRatingBar.getRating()));
            mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).get().addOnCompleteListener(task -> {
                mDatabaseDiets.child(mDietKey).child("Ratings").child(mAuth.getCurrentUser().getUid()).child("name").setValue(task.getResult().child("name").getValue().toString());
                mDatabaseDiets.child(mDietKey).child("Ratings").child(mAuth.getCurrentUser().getUid()).child("image").setValue(task.getResult().child("image").getValue().toString());
            });
            if (!TextUtils.isEmpty(mAddComment.getText().toString()))
                mDatabaseDiets.child(mDietKey).child("Ratings").child(mAuth.getCurrentUser().getUid()).child("comment").setValue(mAddComment.getText().toString());
            mDatabaseDiets.child(mDietKey).child("Ratings").get().addOnCompleteListener(task -> {
                for (DataSnapshot rating : task.getResult().getChildren())
                    ratings += Double.parseDouble(rating.child("rating").getValue().toString());
                mDatabaseDiets.child(mDietKey).child("rating").setValue(Long.toString(Math.round(ratings / task.getResult().getChildrenCount())));
            });


            mAddRatingBtn.setVisibility(View.GONE);
            mAddComment.setVisibility(View.GONE);
            mAddRatingBar.setVisibility(View.GONE);

        });
    }

    @Override
    protected void onStart() {
        super.onStart();
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
                mDatabaseDiets.child(mDietKey).child("Meals").child(Long.toString(day)).get().addOnCompleteListener(task -> {
                    if (!task.getResult().hasChild(meal_key)) {
                        mealViewHolder.mView.findViewById(R.id.cardd).setVisibility(View.GONE);
                        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) mealViewHolder.mView.getLayoutParams();
                        layoutParams.setMargins(0, 0, 0, 0);
                        mealViewHolder.mView.setLayoutParams(layoutParams);
                    }
                });
            }
        };
        mMealsList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class MealViewHolder extends RecyclerView.ViewHolder {
        final View mView;

        public MealViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

        }

        public void setName(String name) {
            TextView product_name = (TextView) mView.findViewById(R.id.productName);
            product_name.setText(name);
        }

        public void setImage(String image) {
            ImageView product_image = (ImageView) mView.findViewById(R.id.productImage);
            Picasso.get().load(image).into(product_image);
        }
    }

    public static class RatingViewHolder extends RecyclerView.ViewHolder {
        final View mView;

        public RatingViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

        }

        public void setName(String name) {
            TextView rating_name = (TextView) mView.findViewById(R.id.rating_name);
            rating_name.setText(name);
        }

        public void setImage(String image) {
            ImageView rating_image = (ImageView) mView.findViewById(R.id.rating_image);
            Picasso.get().load(image).into(rating_image);
        }

        public void setRating(String rating) {
            RatingBar rating_bar = mView.findViewById(R.id.rating_bar);
            rating_bar.setRating(Float.parseFloat(rating));
        }

        public void setComment(String comment) {
            TextView rating_comment = mView.findViewById(R.id.rating_coment);
            rating_comment.setText(comment);
        }

    }
}