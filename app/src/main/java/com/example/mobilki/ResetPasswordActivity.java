package com.example.mobilki;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {
    private EditText mEmail;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        mEmail = findViewById(R.id.Email);

        Button mResetBtn = findViewById(R.id.resetBtn);
        Button mBackBtn = findViewById(R.id.backBtn);

        mAuth = FirebaseAuth.getInstance();

        mBackBtn.setOnClickListener(view -> {
            Intent mainIntent = new Intent(ResetPasswordActivity.this,MainActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(mainIntent);
        });

        mResetBtn.setOnClickListener(view -> {
            String email = mEmail.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(getApplication(), "Wprowadź email", Toast.LENGTH_SHORT).show();
            } else {
                mAuth.sendPasswordResetEmail(email)

                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ResetPasswordActivity.this,
                                            "Wysłano instrukcje do resetowania hasła na podany adres email",
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ResetPasswordActivity.this,
                                            "Brak konta powiązanego z podanym emailem",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }

                        });
            }

        });
    }
}