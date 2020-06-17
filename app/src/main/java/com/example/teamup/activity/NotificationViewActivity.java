package com.example.teamup.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teamup.R;
import com.example.teamup.utilities.FirebaseAuthUtils;
import com.example.teamup.utilities.FirestoreUtils;
import com.example.teamup.utilities.NotificationType;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class NotificationViewActivity extends AppCompatActivity {
    private static final String TAG = NotificationViewActivity.class.getSimpleName();

    FirestoreUtils firestoreUtils;
    FirebaseAuthUtils firebaseAuthUtils;

    //  UI
    private ImageView mProfileImageView;
    private TextView mNameTextView;
    private ListView mSkillsListView;
    private List<String> mSkills;

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent");

        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_view);

        firestoreUtils = new FirestoreUtils(FirebaseFirestore.getInstance());
        firebaseAuthUtils = new FirebaseAuthUtils(FirebaseAuth.getInstance(), firestoreUtils.getFirestoreInstance(), this);

        //  Rimuove il documento relativo alla notifica corrente da Firestore
        firestoreUtils.getFirestoreInstance().collection(FirestoreUtils.KEY_USERS)
                .whereEqualTo(FirestoreUtils.KEY_NAME, getIntent().getStringExtra("recipient"))
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        task.getResult().getDocuments().get(0).getReference().collection(FirestoreUtils.KEY_NOTIFICATIONS)
                                .whereEqualTo("project", getIntent().getStringExtra("project"))
                                .get().addOnCompleteListener(t -> {
                                    if (t.isSuccessful()) {
                                        t.getResult().getDocuments().get(0).getReference().delete().addOnCompleteListener(remove -> {
                                            if (remove.isSuccessful()) Log.d(TAG, "Notification received and removed from Firestore");
                                        });
                                    }
                        });
                    }
        });

        mProfileImageView = findViewById(R.id.profile_imageView);
        mNameTextView = findViewById(R.id.nameTextView);
        mSkillsListView = findViewById(R.id.skills_ListView);

        Log.d(TAG, "Notification: " + getIntent().getStringExtra("type"));

        NotificationType notificationType = NotificationType.valueOf(getIntent().getStringExtra("type"));
        String sendResponseTo = getIntent().getStringExtra("sender");
        String project = getIntent().getStringExtra("project");

        mNameTextView.setText(sendResponseTo);

        Button positiveButton = findViewById(R.id.positiveButton);
        Button negativeButton = findViewById(R.id.negativeButton);

        if (notificationType.equals(NotificationType.TEAMMATE_REQUEST)) {
            //  Visualizza un'anteprima del profilo dell'utente che ha fatto la richiesta,
            //  due pulsanti per accettare o rifiutare
            //  Inviare la notifica appropriata
            firestoreUtils.getFirestoreInstance().collection(FirestoreUtils.KEY_USERS)
                    .whereEqualTo(FirestoreUtils.KEY_NAME, sendResponseTo)
                    .get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            mSkills = (List<String>) task.getResult().getDocuments().get(0).getData().get(FirestoreUtils.KEY_SKILLS);

                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                                    this.getApplicationContext(),
                                    android.R.layout.simple_list_item_1,
                                    mSkills
                            );

                            mSkillsListView.setAdapter(adapter);
                        }
            });

            negativeButton.setVisibility(View.VISIBLE);

            positiveButton.setText("Accept");
            negativeButton.setText("Reject");

            positiveButton.setOnClickListener(view -> {
                //  Uno schifo :(
                firestoreUtils.getFirestoreInstance().collection(FirestoreUtils.KEY_PROJECTS).whereEqualTo(FirestoreUtils.KEY_TITLE, project)
                        .get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                String id = task.getResult().getDocuments().get(0).getId();
                                List<String> team = new ArrayList<>();
                                if (task.getResult().getDocuments().get(0).get(FirestoreUtils.KEY_TEAMMATES) != null)
                                    team.addAll((List<String>) task.getResult().getDocuments().get(0).get(FirestoreUtils.KEY_TEAMMATES));

                                //  Verifica che l'utente non sia già teammate del progetto per evitare di inserirlo più di una volta
                                if (!team.contains(sendResponseTo)) {
                                    team.add(sendResponseTo);
                                    firestoreUtils.updateProjectData(id, FirestoreUtils.KEY_TEAMMATES, team);
                                    firestoreUtils.storeNotification(project, sendResponseTo, firebaseAuthUtils.getCurrentUser().getDisplayName(), NotificationType.LEADER_ACCEPT);
                                } else Toast.makeText(this, sendResponseTo + " is already a teammate on this project.", Toast.LENGTH_LONG).show();
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