package com.firebasechatdemotutorial;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebasechatdemotutorial.model.User;
import com.firebasechatdemotutorial.util.Constants;
import com.firebasechatdemotutorial.util.SharedPrefUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by mobua01 on 25/4/18.
 */

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {


    private static final String TAG = RegisterActivity.class.getSimpleName();
    private EditText mNameSignUpEditText, mEmailSignUpEditText, mPasswordEditText;
    private Button mGoToLoginButton, mSignUpButton;
    private FirebaseAuth mAuth;
    private ProgressDialog mDialog;
    private DatabaseReference mUsersDBref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //firebase assign
        mAuth = FirebaseAuth.getInstance();

        init();
        initListeners();
    }

    private void initListeners() {
        mGoToLoginButton.setOnClickListener(this);
        mSignUpButton.setOnClickListener(this);

        mDialog = new ProgressDialog(this);

    }

    private void init() {
        //assign the views
        mNameSignUpEditText = (EditText) findViewById(R.id.nameSignUpEditText);
        mEmailSignUpEditText = (EditText) findViewById(R.id.emailSignUpEditText);
        mPasswordEditText = (EditText) findViewById(R.id.passwordSignUpEditText);
        mSignUpButton = (Button) findViewById(R.id.signUpButton);
        mGoToLoginButton = (Button) findViewById(R.id.goToLogIn);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.goToLogIn:
                goToLoginActivity();
                break;

            case R.id.signUpButton:
                validation();
                break;
        }
    }

    private void validation() {
        String name = mNameSignUpEditText.getText().toString();
        String email = mEmailSignUpEditText.getText().toString().trim();
        String password = mPasswordEditText.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "Name cannot be empty!", Toast.LENGTH_SHORT).show();
        } else if (name.length() < 5) {
            Toast.makeText(RegisterActivity.this, "Name field contains atleast 5 characters", Toast.LENGTH_SHORT).show();
        } else if (email.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "Email cannot be empty!", Toast.LENGTH_SHORT).show();
        } else if (password.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
        } else {
            signUpUserWithFirebase(name, email, password);
        }
    }

    private void signUpUserWithFirebase(final String name, String email, String password) {
        mDialog.setMessage("Please wait...");
        mDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    //there was an error
                    Toast.makeText(RegisterActivity.this, "Error " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    mDialog.dismiss();
                    mDialog.setMessage(getString(R.string.please_wait));
                    Log.e(TAG, "onRegistrationFailure: " + task.getException().getMessage());

                } else {

                    mDialog.setMessage("Adding user in database");
                    Toast.makeText(RegisterActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();

                    DatabaseReference database = FirebaseDatabase.getInstance().getReference();

                    final FirebaseUser firebaseUser = task.getResult().getUser();

                    User user = new User(firebaseUser.getUid(),
                            firebaseUser.getEmail(),
                            new SharedPrefUtil(RegisterActivity.this).getString(Constants.ARG_FIREBASE_TOKEN));
                    database.child(Constants.ARG_USERS)
                            .child(firebaseUser.getUid())
                            .setValue(user)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(RegisterActivity.this, "User Successfully added", Toast.LENGTH_SHORT).show();

                                        mDialog.dismiss();
                                        Intent intent = new Intent(RegisterActivity.this,UserListingActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);

                                    } else {
                                        mDialog.dismiss();
                                        Toast.makeText(RegisterActivity.this, "Unable to add user", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                }
            }
        });
    }


    private void goToLoginActivity() {
        startActivity(new Intent(this, LoginActivity.class));
    }


}
