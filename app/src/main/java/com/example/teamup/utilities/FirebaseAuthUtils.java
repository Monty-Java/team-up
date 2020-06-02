package com.example.teamup.utilities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.teamup.AuthActivity;
import com.example.teamup.MainActivity;
import com.example.teamup.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Objects;

public class FirebaseAuthUtils {

    private static final String TAG = FirebaseAuthUtils.class.getSimpleName();

    private FirebaseAuth firebaseAuth;
    private final FirestoreUtils firestoreUtils;
    private Activity activity;


    public FirebaseAuthUtils(FirebaseAuth firebaseAuth, FirebaseFirestore firestore, Activity activity) {
        this.firebaseAuth = firebaseAuth;
        this.firestoreUtils = new FirestoreUtils(firestore);
        this.activity = activity;
    }

    public FirestoreUtils getFirestoreUtils() {
        return firestoreUtils;
    }

    // TODO: il metodo ha un nome ambiguo- si potrebbe voler verificare se l'utente è autenticato senza andare alla schermata principale
    public void checkCurrentUser() {
        Log.d(TAG, "checkCurrentUser");

        if (firebaseAuth.getCurrentUser() != null) {
            goToMainScreen();
        }
    }

    public FirebaseUser getCurrentUser() {
        Log.d(TAG, "getCurrentUser");

        return firebaseAuth.getCurrentUser();
    }

    public void createAccount(final String displayName, final String email, final String pass, final List<String> skills) {
        firebaseAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(activity, task -> {
            if (task.isSuccessful()) {
                updateUserProfile(displayName, email, pass, skills);
            } else {
                Log.e(TAG, "Create FirebaseUser unsuccessful");

                Toast.makeText(activity, "Error: Unable to create a user", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateUserProfile(final String displayName, final String email, final String pass, final List<String> skills) {
        Log.d(TAG, "updateUserProfile");

        //  Aggiorna il profilo dell'utente impostando la stringa displayName come nome utente
        final FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user != null) {
            user.sendEmailVerification().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    UserProfileChangeRequest profileUpdate =
                            new UserProfileChangeRequest.Builder()
                                    .setDisplayName(displayName)
                                    .build();

                    user.updateProfile(profileUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            firestoreUtils.storeUserData(getCurrentUser(), skills);
                        }
                    });

                    signIn(email, pass);
                }
            });
        }
    }

    public void signIn(String email, String pass) {
        Log.d(TAG, "signIn");

        firebaseAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(activity, task -> {
            if (task.isSuccessful()) {
                goToMainScreen();
            } else {
                Log.e(TAG, "Login Failed");

                Toast.makeText(activity, "Error: Unable to login", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void logout() {
        Log.d(TAG, "logout");

        FirebaseAuth.getInstance().signOut();
        Intent logoutIntent = new Intent(activity, AuthActivity.class);
        activity.startActivity(logoutIntent);
        activity.finish();
    }

    private void verifyUser() {
        Log.d(TAG, "verifyUser");

        AlertDialog.Builder verifyDialogBuilder = new AlertDialog.Builder(activity);
        verifyDialogBuilder.setTitle(R.string.verify_account_title);
        verifyDialogBuilder.setMessage(R.string.verify_account_message);
        verifyDialogBuilder.setPositiveButton("OK", (dialog, which) -> {
            if (firebaseAuth.getCurrentUser() != null) {
                firebaseAuth.getCurrentUser().reload().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (isEmailVerified()) {

                            goToMainScreen();

                        } else {
                            //  If the user isn't verified, show again this dialog
                            Toast.makeText(activity,
                                    "Please verify your account before proceeding",
                                    Toast.LENGTH_SHORT).show();
                            dialog.dismiss();

                            verifyUser();
                        }
                    } else {
                        Log.e(TAG, "Create FirebaseUser unsuccessful");

                        Toast.makeText(activity, "Error: Unable to verify the user", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        verifyDialogBuilder.setNeutralButton("Resend Link", (dialog, which) -> {
            if (firebaseAuth.getCurrentUser() != null)
                firebaseAuth.getCurrentUser().sendEmailVerification();
        });

        AlertDialog verifyDialog = verifyDialogBuilder.create();
        verifyDialog.show();
    }

    private void goToMainScreen() {
        Log.d(TAG, "teamUp");

        if (isEmailVerified()) {
            Intent teamUpIntent = new Intent(activity, MainActivity.class);
            activity.startActivity(teamUpIntent);
            activity.finish();
        } else {
            verifyUser();
        }
    }

    private boolean isEmailVerified() {
        Log.d(TAG, "isEmailVerified");

        return Objects.requireNonNull(firebaseAuth.getCurrentUser()).isEmailVerified();
    }
}
