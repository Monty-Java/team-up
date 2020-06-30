package com.example.teamup.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teamup.R;
import com.example.teamup.utilities.FirebaseAuthUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SignupActivity extends AppCompatActivity {
    private static final String TAG = SignupActivity.class.getSimpleName();

    private EditText mNameEditText;
    private EditText mSurnameEditText;
    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private EditText mSkillsEditText;
    private Button mSignupButton;

    private FirebaseAuthUtils mFirebaseAuthUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        Log.d(TAG, "onCreate");

        mNameEditText = findViewById(R.id.input_name);
        mSurnameEditText = findViewById(R.id.input_surname);
        mEmailEditText = findViewById(R.id.input_email);
        mPasswordEditText = findViewById(R.id.input_password);
        mSkillsEditText = findViewById(R.id.input_skills);
        mSignupButton = findViewById(R.id.btn_signup);

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        mFirebaseAuthUtils = new FirebaseAuthUtils(FirebaseAuth.getInstance(), firestore, this);
    }

    public void onSignupClick(View view) {
        Log.d(TAG, "onSignupClick");

        signup();
    }

    public void onLoginClick(View view) {
        Log.d(TAG, "onLoginClick");

        // Finish the registration screen and return to the Login activity
        finish();
    }

    public void signup() {
        Log.d(TAG, "Signup");

        if (!validate()) {
            onSignupFailed();
            return;
        }

        mSignupButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(SignupActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        String name = mNameEditText.getText().toString();
        String surname = mSurnameEditText.getText().toString();
        String email = mEmailEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();
        String skills = mSkillsEditText.getText().toString();

        String displayName = name + ' ' + surname;
        String[] skillsArray = skills.split("\\W+");
        List<String> skillList = new ArrayList<>(Arrays.asList(skillsArray));

        mFirebaseAuthUtils.createAccount(displayName, email, password, skillList, () -> {
            onSignupSuccess();
            progressDialog.dismiss();
        }, () -> {
            onSignupFailed();
            progressDialog.dismiss();
        });
    }

    public void onSignupSuccess() {
        mSignupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        finish();
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "Signup failed", Toast.LENGTH_LONG).show();

        mSignupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String name = mNameEditText.getText().toString();
        String surname = mSurnameEditText.getText().toString();
        String email = mEmailEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();
        String skills = mSkillsEditText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            mNameEditText.setError("At least 3 characters");
            valid = false;
        } else {
            mNameEditText.setError(null);
        }

        if (surname.isEmpty() || surname.length() < 3) {
            mSurnameEditText.setError("At least 3 characters");
            valid = false;
        } else {
            mSurnameEditText.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmailEditText.setError("Enter a valid email address");
            valid = false;
        } else {
            mEmailEditText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            mPasswordEditText.setError("At least 6 alphanumeric characters");
            valid = false;
        } else {
            mPasswordEditText.setError(null);
        }

        if (skills.isEmpty()) {
            mSkillsEditText.setError("At least 1 skill");
            valid = false;
        } else {
            mSkillsEditText.setError(null);
        }

        return valid;
    }
}