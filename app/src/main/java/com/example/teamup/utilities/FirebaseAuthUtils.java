package com.example.teamup.utilities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.teamup.activity.LoginActivity;
import com.example.teamup.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;

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

    public void isAlreadyLoggedIn(Runnable onUserVerified) {
        Log.d(TAG, "alreadyLoggedIn");

        if (firebaseAuth.getCurrentUser() != null) {
            if (isEmailVerified()) {

                obtainToken();

                onUserVerified.run();
            } else {
                verifyUser(onUserVerified);
            }
        }
    }

    private void obtainToken() {
        //  Ottiene un token che permette a inviare notifiche ad altri dispositivi
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
            if (task.isSuccessful())
                firestoreUtils.updateUserData(firebaseAuth.getCurrentUser().getDisplayName(), "token",
                        task.getResult().getToken());
        });
    }

    public FirebaseUser getCurrentUser() {
        Log.d(TAG, "getCurrentUser");

        return firebaseAuth.getCurrentUser();
    }

    public void createAccount(final String displayName, final String email, final String pass, final List<String> skills, Runnable onSignupSuccessful, Runnable onSignupFailed) {
        firebaseAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(activity, task -> {
            if (task.isSuccessful()) {
                updateUserProfile(displayName, email, pass, skills, onSignupSuccessful);
            } else {
                Log.e(TAG, "Create FirebaseUser unsuccessful");

                Toast.makeText(activity, "Error: Unable to create a user", Toast.LENGTH_LONG).show();
                onSignupFailed.run();
            }
        });
    }

    private void updateUserProfile(final String displayName, final String email, final String pass, final List<String> skills, Runnable onSignupSuccessful) {
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

                    user.updateProfile(profileUpdate)
                            .addOnCompleteListener(task1 -> firestoreUtils.storeUserData(getCurrentUser(), skills));

                    signIn(email, pass, onSignupSuccessful);
                }
            });
        }
    }

    public void signIn(String email, String pass, Runnable onLoginSuccessful) {
        Log.d(TAG, "signIn");

        firebaseAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(activity, task -> {
            if (task.isSuccessful()) {

                obtainToken();

                // On complete call either onLoginSuccess or onLoginFailed
                new android.os.Handler().post(
                        onLoginSuccessful::run);
            } else {
                Log.e(TAG, "Login Failed");

                Toast.makeText(activity, "Error: Unable to login", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void logout() {
        Log.d(TAG, "logout");

        //  Rimuove il Token associato all'utente corrente
        firestoreUtils.updateUserData(firebaseAuth.getCurrentUser().getDisplayName(), "token", FieldValue.delete());

        FirebaseAuth.getInstance().signOut();
        Intent logoutIntent = new Intent(activity, LoginActivity.class);
        activity.startActivity(logoutIntent);
        activity.finish();
    }

    public void verifyUser(Runnable onUserVerified) {
        Log.d(TAG, "verifyUser");

        AlertDialog.Builder verifyDialogBuilder = new AlertDialog.Builder(activity);
        verifyDialogBuilder.setTitle(R.string.verify_account_title);
        verifyDialogBuilder.setMessage(R.string.verify_account_message);
        verifyDialogBuilder.setPositiveButton("OK", (dialog, which) -> {
            if (firebaseAuth.getCurrentUser() != null) {
                firebaseAuth.getCurrentUser().reload().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (isEmailVerified()) {

                            obtainToken();
                            onUserVerified.run();

                        } else {
                            //  If the user isn't verified, show again this dialog
                            Toast.makeText(activity,
                                    "Please verify your account before proceeding",
                                    Toast.LENGTH_SHORT).show();
                            dialog.dismiss();

                            verifyUser(onUserVerified);
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

    private boolean isEmailVerified() {
        Log.d(TAG, "isEmailVerified");

        return Objects.requireNonNull(firebaseAuth.getCurrentUser()).isEmailVerified();
    }
}
