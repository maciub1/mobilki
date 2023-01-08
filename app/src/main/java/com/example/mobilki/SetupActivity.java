package com.example.mobilki;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SetupActivity extends AppCompatActivity {
    private ImageButton mSetupImageBtn;
    private EditText mNameField;
    private EditText mAgeField;
    private EditText mWeightField;
    private EditText mHeightField;

    private FirebaseAuth mAuth;
    private Uri mImageUri = null;

    private static final int GALERY_REQUEST = 1;

    private DatabaseReference mDatabaseUsers;

    private StorageReference mStorageImage;
    private ProgressDialog mProgress;
    private RadioGroup radioSexGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mProgress = new ProgressDialog(this);

        mSetupImageBtn = findViewById(R.id.SetupImageBtn);
        mNameField = findViewById(R.id.setupNameField);
        mAgeField = findViewById(R.id.setupAgeField);
        mHeightField = findViewById(R.id.setupHeightField);
        mWeightField = findViewById(R.id.setupWeightField);
        Button mSubmitBtn = findViewById(R.id.setupSubmitBtn);

        Button mLogOutBtn = findViewById(R.id.logOutbtn);
        mAuth = FirebaseAuth.getInstance();
        radioSexGroup= findViewById(R.id.radioGroup);

        mStorageImage = FirebaseStorage.getInstance().getReference().child("Profile_images");

        mSubmitBtn.setOnClickListener(view -> startSetupAccount());

        mSetupImageBtn.setOnClickListener(view -> {
            Intent galleryIntent = new Intent();
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
            galleryIntent.setType("image/*");
            startActivityForResult(galleryIntent, GALERY_REQUEST);
        });

        mLogOutBtn.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(SetupActivity.this,MainActivity.class));
        });
    }

    private void startSetupAccount() {
        String name = mNameField.getText().toString().trim();
        String user_id = mAuth.getCurrentUser().getUid();
        String age = mAgeField.getText().toString().trim();
        String height = mHeightField.getText().toString().trim();
        String weight = mWeightField.getText().toString().trim();

        if(!TextUtils.isEmpty(name) && mImageUri != null && !TextUtils.isEmpty(age) && !TextUtils.isEmpty(weight) && !TextUtils.isEmpty(height)){
            mProgress.setMessage("Koczenie konfiguracji ...");
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
                    if(radioSexGroup.getCheckedRadioButtonId()==R.id.radioButton)
                    {
                        calreq = Math.round(66+(13.7*Double.parseDouble(weight))+(5*Double.parseDouble(height))-(6.8*Double.parseDouble(age)));
                        mDatabaseUsers.child(user_id).child("sex").setValue("m");
                        mDatabaseUsers.child(user_id).child("calories req").setValue(Long.toString(calreq));
                    }
                    else{
                        calreq = Math.round(655+(9.6*Double.parseDouble(weight))+(1.8*Double.parseDouble(height))-(4.7*Double.parseDouble(age)));
                        mDatabaseUsers.child(user_id).child("sex").setValue("f");
                        mDatabaseUsers.child(user_id).child("calories req").setValue(Long.toString(calreq));
                    }
                    long carbohydratesreq = Math.round((calreq*0.55)/4);
                    long proteinsreq = Math.round((calreq*0.15)/4);
                    long fatreq = Math.round((calreq*0.30)/9);
                    mDatabaseUsers.child(user_id).child("carbohydrates req").setValue(Long.toString(carbohydratesreq));
                    mDatabaseUsers.child(user_id).child("proteins req").setValue(Long.toString(proteinsreq));
                    mDatabaseUsers.child(user_id).child("fat req").setValue(Long.toString(fatreq));
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                    mDatabaseUsers.child(user_id).child("last logged").setValue(dateFormat.format(Calendar.getInstance().getTime()));


                    mProgress.dismiss();



                    Intent intent = new Intent(SetupActivity.this, MainScreenActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);


                }
            })).addOnFailureListener(e -> {
            });



        } else
        {
            Toast.makeText(SetupActivity.this, "Wypełnij wszystkie pola", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALERY_REQUEST && resultCode == RESULT_OK){
            mImageUri = data.getData();

            mSetupImageBtn.setImageURI(mImageUri);
        }

    }
}