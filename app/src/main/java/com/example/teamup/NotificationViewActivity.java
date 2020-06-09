package com.example.teamup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.teamup.utilities.FirebaseAuthUtils;
import com.example.teamup.utilities.FirestoreUtils;
import com.example.teamup.utilities.NotificationType;
import com.example.teamup.utilities.Progetto;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.List;

public class NotificationViewActivity extends AppCompatActivity {
    private static final String TAG = NotificationViewActivity.class.getSimpleName();

    FirestoreUtils firestoreUtils;
    FirebaseAuthUtils firebaseAuthUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_view);

        firestoreUtils = new FirestoreUtils(FirebaseFirestore.getInstance());
        firebaseAuthUtils = new FirebaseAuthUtils(FirebaseAuth.getInstance(), firestoreUtils.getFirestoreInstance(), this);

        NotificationType notificationType = NotificationType.valueOf(getIntent().getStringExtra("type"));
        String sendResponseTo = getIntent().getStringExtra("sender");
        String project = getIntent().getStringExtra("project");

        Log.d(TAG, sendResponseTo);
        Log.d(TAG, project);

        Button positiveButton = findViewById(R.id.positiveButton);

        Button negativeButton = findViewById(R.id.negativeButton);

        if (notificationType.equals(NotificationType.TEAMMATE_REQUEST)) {

            //  Visualizza un'anteprima del profilo dell'utente che ha fatto la richiesta,
            //  due pulsanti per accettare o rifiutare
            //  Inviare la notifica appropriata

            negativeButton.setVisibility(View.VISIBLE);


            positiveButton.setText("Accept");
            negativeButton.setText("Reject");

            positiveButton.setOnClickListener(view -> {
                Log.d(TAG, "DATA: " + project + ' ' + sendResponseTo + ' ' + firebaseAuthUtils.getCurrentUser().getDisplayName());

                //  Uno schifo :(
                firestoreUtils.getFirestoreInstance().collection(FirestoreUtils.KEY_PROJECTS).whereEqualTo(FirestoreUtils.KEY_TITLE, project)
                        .get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                String id = task.getResult().getDocuments().get(0).getId();
                                List<String> team = new ArrayList<>();
                                if (task.getResult().getDocuments().get(0).get(FirestoreUtils.KEY_TEAMMATES) != null)
                                    team.addAll((List<String>) task.getResult().getDocuments().get(0).get(FirestoreUtils.KEY_TEAMMATES));
                                team.add(sendResponseTo);
                                firestoreUtils.updateProjectData(id, FirestoreUtils.KEY_TEAMMATES, team);

                                firestoreUtils.storeNotification(project, sendResponseTo, firebaseAuthUtils.getCurrentUser().getDisplayName(), NotificationType.LEADER_ACCEPT);
                            } else Log.d(TAG, "Error responding or updating project");
                });

                Intent homeIntent = new Intent(this, MainActivity.class);
                startActivity(homeIntent);
                finish();
            });

            negativeButton.setOnClickListener(view -> {
                firestoreUtils.storeNotification(project, sendResponseTo, firebaseAuthUtils.getCurrentUser().getDisplayName(), NotificationType.LEADER_REJECT);

                //  TODO: REFACTOR
                Intent homeIntent = new Intent(this, MainActivity.class);
                startActivity(homeIntent);
                finish();
            });

        } else if (notificationType.equals(NotificationType.LEADER_ACCEPT)) {
            negativeButton.setVisibility(View.INVISIBLE);

            //  Intent che apre ProjectActivity col progetto per il quale si è fatta la richiesta
            positiveButton.setOnClickListener(view -> {
                Intent viewProjectIntent = new Intent(this, ProjectActivity.class);
                startActivity(viewProjectIntent);
            });
            Log.d(TAG, "Request Accepted");
        } else if (notificationType.equals(NotificationType.LEADER_REJECT)) {
            negativeButton.setVisibility(View.INVISIBLE);
            //  Messaggio per indicare che il leader ha rifiutato la richiesta
            Log.d(TAG, "Request Rejected");

            positiveButton.setOnClickListener(view -> {
                Intent homeIntent = new Intent(this, MainActivity.class);
                startActivity(homeIntent);
            });
        }
    }
}