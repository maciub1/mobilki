package com.example.mobilki;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class SettingsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DatabaseReference mDatabaseUsers;
    private FirebaseAuth mAuth;

    private ImageButton mSetupImageBtn;
    private EditText mNameField;
    private EditText mAgeField;
    private EditText mWeightField;
    private EditText mHeightField;
    private Uri mImageUri = null;

    private static final int GALERY_REQUEST = 1;
    private StorageReference mStorageImage;
    private ProgressDialog mProgress;
    private RadioGroup radioSexGroup;
    private RadioButton mBtn;
    private RadioButton fBtn;


    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    View header;

    private TextView usernameTv;
    private ImageView userImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");

        mProgress = new ProgressDialog(this);
        mSetupImageBtn = (ImageButton) findViewById(R.id.SettingsImageBtn);
        mNameField = (EditText) findViewById(R.id.SettingsNameField);
        mAgeField = (EditText) findViewById(R.id.SettingsAgeField);
        mHeightField = (EditText) findViewById(R.id.SettingsHeightField);
        mWeightField = (EditText) findViewById(R.id.SettingsWeightField);
        Button mSubmitBtn = (Button) findViewById(R.id.SettingsSubmitBtn);
        radioSexGroup = (RadioGroup) findViewById(R.id.SettingsradioGroup);
        TextView mInfo = findViewById(R.id.loginInfo);
        mStorageImage = FirebaseStorage.getInstance().getReference().child("Profile_images");
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        header = navigationView.getHeaderView(0);
        mBtn = findViewById(R.id.SettingsradioButton);
        fBtn = findViewById(R.id.SettingsradioButton2);

        usernameTv = header.findViewById(R.id.usernameTv);
        userImg = header.findViewById(R.id.profile_img);
        Button guestLogin = header.findViewById(R.id.guest_log_in);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_settings);
        if (!mAuth.getCurrentUser().isAnonymous()) {
            SetProfile();
            mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Picasso.get().load(snapshot.child("image").getValue().toString()).into(mSetupImageBtn);
                    mNameField.setText(snapshot.child("name").getValue().toString());
                    mAgeField.setText(snapshot.child("age").getValue().toString());
                    mHeightField.setText(snapshot.child("height").getValue().toString());
                    mWeightField.setText(snapshot.child("weight").getValue().toString());
                    if (snapshot.child("sex").getValue().toString().equals("m"))
                        mBtn.setChecked(true);
                    else
                        fBtn.setChecked(true);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            mSetupImageBtn.setOnClickListener(view -> {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALERY_REQUEST);
            });
            mSubmitBtn.setOnClickListener(view -> startSetupAccount());
        } else {
            mInfo.setVisibility(View.VISIBLE);
            mSetupImageBtn.setVisibility(View.GONE);
            mNameField.setVisibility(View.GONE);
            mAgeField.setVisibility(View.GONE);
            mHeightField.setVisibility(View.GONE);
            mWeightField.setVisibility(View.GONE);
            mSubmitBtn.setVisibility(View.GONE);
            radioSexGroup.setVisibility(View.GONE);
            userImg.setImageResource(0);
            guestLogin.setVisibility(View.VISIBLE);
            navigationView.getMenu().findItem(R.id.nav_logout).setTitle(R.string.exit);
            guestLogin.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(SettingsActivity.this, MainActivity.class));
            });
        }
    }

    private void startSetupAccount() {
        String name = mNameField.getText().toString().trim();
        String user_id = mAuth.getCurrentUser().getUid();
        String age = mAgeField.getText().toString().trim();
        String height = mHeightField.getText().toString().trim();
        String weight = mWeightField.getText().toString().trim();
        Boolean imageChanged = false;
        if (imageChanged) {

            if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(age) && !TextUtils.isEmpty(weight) && !TextUtils.isEmpty(height)) {
                mProgress.setMessage("Saving ...");
                mProgress.show();

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
                        mDatabaseUsers.child(user_id).child("name").setValue(name);
                        mDatabaseUsers.child(user_id).child("image").setValue(downloadUri);
                        mDatabaseUsers.child(user_id).child("age").setValue(age);
                        mDatabaseUsers.child(user_id).child("weight").setValue(weight);
                        mDatabaseUsers.child(user_id).child("height").setValue(height);
                        long calreq;
                        if (radioSexGroup.getCheckedRadioButtonId() == mBtn.getId()) {
                            calreq = Math.round(66 + (13.7 * Double.parseDouble(weight)) + (5 * Double.parseDouble(height)) - (6.8 * Double.parseDouble(age)));
                            mDatabaseUsers.child(user_id).child("sex").setValue("m");
                            mDatabaseUsers.child(user_id).child("calories req").setValue(Long.toString(calreq));
                        } else {
                            calreq = Math.round(655 + (9.6 * Double.parseDouble(weight)) + (1.8 * Double.parseDouble(height)) - (4.7 * Double.parseDouble(age)));
                            mDatabaseUsers.child(user_id).child("sex").setValue("f");
                            mDatabaseUsers.child(user_id).child("calories req").setValue(Long.toString(calreq));
                        }
                        long carbohydratesreq = Math.round((calreq * 0.55) / 4);
                        long proteinsreq = Math.round((calreq * 0.15) / 4);
                        long fatreq = Math.round((calreq * 0.30) / 9);
                        mDatabaseUsers.child(user_id).child("carbohydrates req").setValue(Long.toString(carbohydratesreq));
                        mDatabaseUsers.child(user_id).child("proteins req").setValue(Long.toString(proteinsreq));
                        mDatabaseUsers.child(user_id).child("fat req").setValue(Long.toString(fatreq));
                        mProgress.dismiss();
                        Toast.makeText(SettingsActivity.this, "Settings changed sucessfully", Toast.LENGTH_LONG).show();


                    }
                })).addOnFailureListener(e -> {
                });


            } else {
                Toast.makeText(SettingsActivity.this, "Fill all fields", Toast.LENGTH_LONG).show();
            }
        } else {
            if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(age) && !TextUtils.isEmpty(weight) && !TextUtils.isEmpty(height)) {
                mProgress.setMessage("Finishing configuration ...");
                mProgress.show();


                mDatabaseUsers.child(user_id).child("name").setValue(name);
                mDatabaseUsers.child(user_id).child("age").setValue(age);
                mDatabaseUsers.child(user_id).child("weight").setValue(weight);
                mDatabaseUsers.child(user_id).child("height").setValue(height);
                long calreq;
                if (radioSexGroup.getCheckedRadioButtonId() == mBtn.getId()) {
                    calreq = Math.round(66 + (13.7 * Double.parseDouble(weight)) + (5 * Double.parseDouble(height)) - (6.8 * Double.parseDouble(age)));
                    mDatabaseUsers.child(user_id).child("sex").setValue("m");
                    mDatabaseUsers.child(user_id).child("calories req").setValue(Long.toString(calreq));
                } else {
                    calreq = Math.round(655 + (9.6 * Double.parseDouble(weight)) + (1.8 * Double.parseDouble(height)) - (4.7 * Double.parseDouble(age)));
                    mDatabaseUsers.child(user_id).child("sex").setValue("f");
                    mDatabaseUsers.child(user_id).child("calories req").setValue(Long.toString(calreq));
                }
                long carbohydratesreq = Math.round((calreq * 0.55) / 4);
                long proteinsreq = Math.round((calreq * 0.15) / 4);
                long fatreq = Math.round((calreq * 0.30) / 9);
                mDatabaseUsers.child(user_id).child("carbohydrates req").setValue(Long.toString(carbohydratesreq));
                mDatabaseUsers.child(user_id).child("proteins req").setValue(Long.toString(proteinsreq));
                mDatabaseUsers.child(user_id).child("fat req").setValue(Long.toString(fatreq));
                mProgress.dismiss();
                Toast.makeText(SettingsActivity.this, "Account settings has been changed", Toast.LENGTH_LONG).show();


            } else {
                Toast.makeText(SettingsActivity.this, "Fill all fields", Toast.LENGTH_LONG).show();
            }
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALERY_REQUEST && resultCode == RESULT_OK) {
            mImageUri = data.getData();

            mSetupImageBtn.setImageURI(mImageUri);
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
                //Go to main screen
                Intent intent = new Intent(SettingsActivity.this, MainScreenActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_diets:
                Intent dietsIntent = new Intent(SettingsActivity.this, DietActivity.class);
                startActivity(dietsIntent);
                break;

            case R.id.nav_products:
                Intent productsntent = new Intent(SettingsActivity.this, ProductActivity.class);
                startActivity(productsntent);
                break;
            case R.id.nav_meals:
                Intent mealsIntent = new Intent(SettingsActivity.this, MealsActivity.class);
                startActivity(mealsIntent);
                break;
            case R.id.nav_settings:

                break;
            case R.id.nav_logout:
                //Log out user
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(SettingsActivity.this, MainActivity.class));
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