package com.example.teamup;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        mAuth = FirebaseAuth.getInstance();

        //  Riferimenti agli elementi dell'UI
        ImageView image = findViewById(R.id.imageView_teamUp);
        TextView teamUpDescription = findViewById(R.id.textView_desc);
        final Button loginButton = findViewById(R.id.button_login);
        final Button registerButton = findViewById(R.id.button_register);
        Button discoverButton = findViewById(R.id.button_discover);

        //  Registrazione di listener per i pulsanti
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerButton.setEnabled(false);

                login();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginButton.setEnabled(false);

                register();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //  TODO: verificare se l'utente è già autenticato e in tal caso chiamare teamUp()
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    private void login() {
        final Dialog loginDialog = new Dialog(AuthActivity.this);
        loginDialog.setContentView(R.layout.login_dialog);

        Button loginButton = loginDialog.findViewById(R.id.button_login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  Verifica che le EditText per l'indirizzo e-mail e la password non siano vuote
                EditText etEmail = loginDialog.findViewById(R.id.editText_email);
                String sEmail = etEmail.getText().toString();


                if (!sEmail.equals("")) teamUp();
            }
        });

        loginDialog.show();
    }

    private void register() {
        final Dialog registerDialog = new Dialog(AuthActivity.this);
        registerDialog.setContentView(R.layout.register_dialog);

        Button bRegister = registerDialog.findViewById(R.id.button_register);
        bRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  Verifica che l'EditText per l'indirizzo e-mail non sia vuota
                EditText etEmail = registerDialog.findViewById(R.id.editText_email);
                String sEmail = etEmail.getText().toString();

                if (!sEmail.equals("")) teamUp();
            }
        });

        registerDialog.show();
    }

    //  TODO: modificare il nome di questa funzione
    private void teamUp() {
        Intent teamUpIntent = new Intent(this, MainActivity.class);
        startActivity(teamUpIntent);
    }
}
