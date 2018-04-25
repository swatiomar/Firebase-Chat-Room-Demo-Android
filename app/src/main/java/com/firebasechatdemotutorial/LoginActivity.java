package com.firebasechatdemotutorial;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebasechatdemotutorial.util.Constants;
import com.firebasechatdemotutorial.util.SharedPrefUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mEmailLoginEditText, mPasswordLoginEditText;
    private Button mLoginButton, mGoToCreateNewAccount;
    private ProgressDialog mDialog;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        init();
        initListeners();
    }

    private void initListeners() {
        mLoginButton.setOnClickListener(this);
        mGoToCreateNewAccount.setOnClickListener(this);

        mDialog = new ProgressDialog(this);
    }

    private void init() {
        //assign views
        mEmailLoginEditText = (EditText) findViewById(R.id.emailLogInEditText);
        mPasswordLoginEditText = (EditText) findViewById(R.id.passwordLogInEditText);
        mLoginButton = (Button) findViewById(R.id.logInButton);
        mGoToCreateNewAccount = (Button) findViewById(R.id.goToSignUpButton);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.logInButton:
                validation();
                break;

            case R.id.goToSignUpButton:
                goToCreateNewAccount();
                break;
        }
    }

    private void goToCreateNewAccount() {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    private void validation() {
        String email = mEmailLoginEditText.getText().toString().trim();
        String password = mPasswordLoginEditText.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(LoginActivity.this, "You must provide email", Toast.LENGTH_SHORT).show();
        } else if (password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "You must provide password", Toast.LENGTH_SHORT).show();
        } else {
            logInUsers(email, password);
        }
    }

    private void logInUsers(String email, String password) {
        mDialog.setMessage("Please wait...");
        mDialog.show();
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                mDialog.dismiss();
                if (!task.isSuccessful()) {
                    //error loging
                    mDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Error " + task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    goToChatUsersActivity(task.getResult().getUser().getUid());
                }
            }
        });
    }

    private void goToChatUsersActivity(String uid) {

        updateFirebaseToken(uid,
                new SharedPrefUtil(getApplicationContext()).getString(Constants.ARG_FIREBASE_TOKEN, null));

        mDialog.dismiss();
        Toast.makeText(LoginActivity.this, "Logged in successfully", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(LoginActivity.this, UserListingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }

    private void updateFirebaseToken(String uid, String token) {
        FirebaseDatabase.getInstance()
                .getReference()
                .child(Constants.ARG_USERS)
                .child(uid)
                .child(Constants.ARG_FIREBASE_TOKEN)
                .setValue(token);
    }

}
