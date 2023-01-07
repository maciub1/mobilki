package com.example.mobilki;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {
    private EditText mEmail;
    private EditText mPassword;
    private EditText mPassword2;

    private FirebaseAuth mAuth;


    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        mProgress = new ProgressDialog(this);

        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mPassword2 = findViewById(R.id.password2);

        Button mRegisterBtn = findViewById(R.id.registerBtn);

        mRegisterBtn.setOnClickListener(view -> startRegister());
    }

    private void startRegister() {

        String email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();
        String password2 = mPassword2.getText().toString().trim();


        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(password2)) {
            if (!password.equals(password2)) {
                Toast.makeText(RegisterActivity.this, "Hasła nie są takie same", Toast.LENGTH_LONG).show();
            } else if (password.length() < 6) {
                Toast.makeText(RegisterActivity.this, "Hasło musi mieć przynajmniej 6 znaków",
                        Toast.LENGTH_SHORT).show();
            } else {
                mProgress.setMessage("Rejestracja ...");
                mProgress.show();

                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mProgress.dismiss();
                        Intent mainIntent = new Intent(RegisterActivity.this, SetupActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(mainIntent);

                    } else {
                        mProgress.dismiss();
                        Toast.makeText(RegisterActivity.this, "Podany adres email jest już przypisany do innego konta",
                                Toast.LENGTH_SHORT).show();

                    }

                }

                );
            }

        } else {
            Toast.makeText(RegisterActivity.this, "Wypenij wszystkie pola", Toast.LENGTH_LONG).show();
        }
    }


}