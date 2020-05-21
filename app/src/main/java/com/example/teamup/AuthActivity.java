package com.example.teamup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class AuthActivity extends AppCompatActivity {
    private static final String TAG = "AuthActivity";

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        mAuth = FirebaseAuth.getInstance();

        //  Riferimenti agli elementi dell'UI
        ImageView image = findViewById(R.id.imageView_teamUp);
        TextView teamUpDescription = findViewById(R.id.textView_desc);
        Button mLoginButton = findViewById(R.id.button_login);
        Button mRegisterButton = findViewById(R.id.button_register);
        Button discoverButton = findViewById(R.id.button_discover);

        //  Registrazione di listener per i pulsanti
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });

        discoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  TODO: Intent per inziare l'Activity relativa a IUI-7
                Toast.makeText(AuthActivity.this, "Porta l'utente a IUI-7", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //  TODO: verificare se l'utente è già autenticato e in tal caso chiamare teamUp()
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) teamUp();
    }

    private void login() {
        final Dialog loginDialog = new Dialog(AuthActivity.this);
        loginDialog.setContentView(R.layout.login_dialog);

        Button dialogLoginButton = loginDialog.findViewById(R.id.button_login);
        dialogLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  Verifica che le EditText per l'indirizzo e-mail e la password non siano vuote
                EditText etEmail = loginDialog.findViewById(R.id.editText_email);
                EditText etPass = loginDialog.findViewById(R.id.editText_pass);
                String sEmail = etEmail.getText().toString();
                String sPass = etPass.getText().toString();

                if (!sEmail.equals("") && !sPass.equals("")) signIn(sEmail, sPass);
            }
        });

        loginDialog.show();
    }

    private void register() {
        final Dialog registerDialog = new Dialog(AuthActivity.this);
        registerDialog.setContentView(R.layout.register_dialog);

        Button dialogRegisterButton = registerDialog.findViewById(R.id.button_register);
        dialogRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  Verifica che l'EditText per l'indirizzo e-mail non sia vuota
                EditText etEmail = registerDialog.findViewById(R.id.editText_email);
                EditText etPass = registerDialog.findViewById(R.id.editText_pass);
                EditText etName = registerDialog.findViewById(R.id.editText_name);
                EditText etSurname = registerDialog.findViewById(R.id.editText_surname);

                String sEmail = etEmail.getText().toString();
                String sPass = etPass.getText().toString();
                String displayName = etName.getText().toString() + ' ' + etSurname.getText().toString();

                if (!sEmail.equals(""))
                    createAccount(displayName, sEmail, sPass);
            }
        });

        registerDialog.show();
    }

    //  TODO: modificare il nome di questa funzione
    private void teamUp() {
        if (mAuth.getCurrentUser() != null) {
            if (mAuth.getCurrentUser().isEmailVerified()) {
                Intent teamUpIntent = new Intent(this, MainActivity.class);
                startActivity(teamUpIntent);
            } else verifyUser();
        }
    }

    //========================= Firebase Authentication =========================//

    private void createAccount(final String displayName, final String email, final String pass) {
        mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    //  TODO: sostituire questo con un metodo apposito che crea un messaggio Toast per l'utente
                    Log.e(TAG, "Create FirebaseUser unsuccessful\n");
                } else {
                    //  Aggiorna il profilo dell'utente impostando la stringa displayName come nome utente
                    final FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder().setDisplayName(displayName).build();
                                    user.updateProfile(profileUpdate);

                                    signIn(email, pass);
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    private void signIn(String email, String pass) {
        mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    //  TODO: creare un Toast con un messaggio di errore significativo
                    Log.e(TAG, "Login Failed\n");
                } else teamUp();
            }
        });
    }

    private void verifyUser() {
        AlertDialog.Builder verifyDialogBuilder = new AlertDialog.Builder(this);
        //  TODO: trasformare la stringa in una risorsa in strings.xml
        verifyDialogBuilder.setTitle("Verify Account");
        verifyDialogBuilder.setMessage("Please check your e-mail for the verification link to activate your account.");
        verifyDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mAuth.getCurrentUser() != null) {
                    mAuth.getCurrentUser().reload().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                if (mAuth.getCurrentUser().isEmailVerified()) teamUp();
                                else {
                                    //  If the user isn't verified, opens a chooser for the
                                    //  device's e-mail clients to allow the user to complete
                                    //  the process.
                                    Toast.makeText(AuthActivity.this,
                                            "Please verify your account before proceeding",
                                            Toast.LENGTH_SHORT).show();

                                    Intent emailClientIntent = new Intent(Intent.ACTION_MAIN);
                                    emailClientIntent.addCategory(Intent.CATEGORY_APP_EMAIL);
                                    try {
                                        startActivity(emailClientIntent);
                                    } catch (ActivityNotFoundException e) {
                                        //  TODO: creare un Toast con un messaggio di errore significativo
                                    }
                                }
                            } //else //TODO: creare un Toast con un messaggio di errore significativo
                        }
                    });
                }
            }
        });

        verifyDialogBuilder.setNeutralButton("Resend Link", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mAuth.getCurrentUser() != null)
                    mAuth.getCurrentUser().sendEmailVerification();
            }
        });

        AlertDialog verifyDialog = verifyDialogBuilder.create();
        verifyDialog.show();
    }
}
