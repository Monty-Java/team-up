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

    private EditText _nameText;
    private EditText _surnameText;
    private EditText _emailText;
    private EditText _passwordText;
    private EditText _skillsText;
    private Button _signupButton;

    private FirebaseAuthUtils firebaseAuthUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        Log.d(TAG, "onCreate");

        _nameText = findViewById(R.id.input_name);
        _surnameText = findViewById(R.id.input_surname);
        _emailText = findViewById(R.id.input_email);
        _passwordText = findViewById(R.id.input_password);
        _skillsText = findViewById(R.id.input_skills);
        _signupButton = findViewById(R.id.btn_signup);

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firebaseAuthUtils = new FirebaseAuthUtils(FirebaseAuth.getInstance(), firestore, this);
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

        _signupButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(SignupActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        String name = _nameText.getText().toString();
        String surname = _surnameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();
        String skills = _skillsText.getText().toString();

        String displayName = name + ' ' + surname;
        String[] skillsArray = skills.split("\\W+");
        List<String> skillList = new ArrayList<>(Arrays.asList(skillsArray));

        firebaseAuthUtils.createAccount(displayName, email, password, skillList, () -> {
            onSignupSuccess();
            progressDialog.dismiss();
        }, () -> {
            onSignupFailed();
            progressDialog.dismiss();
        });
    }

    public void onSignupSuccess() {
        _signupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        finish();
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "Signup failed", Toast.LENGTH_LONG).show();

        _signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String name = _nameText.getText().toString();
        String surname = _surnameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();
        String skills = _skillsText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            _nameText.setError("At least 3 characters");
            valid = false;
        } else {
            _nameText.setError(null);
        }

        if (surname.isEmpty() || surname.length() < 3) {
            _surnameText.setError("At least 3 characters");
            valid = false;
        } else {
            _surnameText.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("Enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("At least 6 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        if (skills.isEmpty()) {
            _skillsText.setError("At least 1 skill");
            valid = false;
        } else {
            _skillsText.setError(null);
        }

        return valid;
    }
}