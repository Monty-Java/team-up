package com.example.teamup;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teamup.utilities.FirebaseAuthUtils;
import com.google.firebase.auth.FirebaseAuth;

public class AuthActivity extends AppCompatActivity {

    private static final String TAG = AuthActivity.class.getSimpleName();

    private FirebaseAuthUtils firebaseAuthUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        firebaseAuthUtils = new FirebaseAuthUtils(FirebaseAuth.getInstance(), this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");

        //  TODO: verificare se l'utente è già autenticato e in tal caso chiamare teamUp()
        firebaseAuthUtils.checkCurrentUser();
    }

    public void onLoginClick(View view) {
        Log.d(TAG, "onLoginClick");

        login();
    }

    public void onRegisterClick(View view) {
        Log.d(TAG, "onRegisterClick");

        register();
    }

    public void onDiscoverClick(View view) {
        Log.d(TAG, "onDiscoverClick");

        Toast.makeText(this, "Porta l'utente a IUI-7", Toast.LENGTH_LONG).show();
    }

    private void login() {
        final Dialog loginDialog = new Dialog(AuthActivity.this);
        loginDialog.setContentView(R.layout.login_dialog);

        Button dialogLoginButton = loginDialog.findViewById(R.id.button_login);
        dialogLoginButton.setOnClickListener(v -> {
            //  Verifica che le EditText per l'indirizzo e-mail e la password non siano vuote
            EditText etEmail = loginDialog.findViewById(R.id.editText_email);
            EditText etPass = loginDialog.findViewById(R.id.editText_pass);
            String sEmail = etEmail.getText().toString();
            String sPass = etPass.getText().toString();

            if (!sEmail.equals("") && !sPass.equals("")) {
                firebaseAuthUtils.signIn(sEmail, sPass);
            }
        });

        loginDialog.show();
    }

    private void register() {
        final Dialog registerDialog = new Dialog(AuthActivity.this);
        registerDialog.setContentView(R.layout.register_dialog);

        Button dialogRegisterButton = registerDialog.findViewById(R.id.button_register);
        dialogRegisterButton.setOnClickListener(v -> {
            //  Verifica che l'EditText per l'indirizzo e-mail non sia vuota
            EditText etEmail = registerDialog.findViewById(R.id.editText_email);
            EditText etPass = registerDialog.findViewById(R.id.editText_pass);
            EditText etName = registerDialog.findViewById(R.id.editText_name);
            EditText etSurname = registerDialog.findViewById(R.id.editText_surname);

            String sEmail = etEmail.getText().toString();
            String sPass = etPass.getText().toString();
            String displayName = etName.getText().toString() + ' ' + etSurname.getText().toString();

            if (!sEmail.equals(""))
                firebaseAuthUtils.createAccount(displayName, sEmail, sPass);
        });

        registerDialog.show();
    }
}
