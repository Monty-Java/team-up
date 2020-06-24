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
import com.example.teamup.utilities.NotificationUtils;
import com.example.teamup.utilities.Utente;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NotificationViewActivity extends AppCompatActivity {
    private static final String TAG = NotificationViewActivity.class.getSimpleName();

    FirestoreUtils firestoreUtils;
    FirebaseAuthUtils firebaseAuthUtils;

    private ListView mSkillsListView;

    private Utente mSender;

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

        Log.d(TAG, "onCreate");

        firestoreUtils = new FirestoreUtils(FirebaseFirestore.getInstance());
        firebaseAuthUtils = new FirebaseAuthUtils(FirebaseAuth.getInstance(), firestoreUtils.getFirestoreInstance(), this);

        onNotificationOpened();

        //  UI
        ImageView mProfileImageView = findViewById(R.id.profile_imageView);
        TextView mNameTextView = findViewById(R.id.nameTextView);
        TextView mSkillTitleTextView = findViewById(R.id.skillsTitle);
        mSkillsListView = findViewById(R.id.skills_ListView);

        NotificationType notificationType = NotificationType.valueOf(getIntent().getStringExtra(NotificationUtils.TYPE));
        String sendResponseTo = getIntent().getStringExtra(NotificationUtils.SENDER);
        String project = getIntent().getStringExtra(NotificationUtils.PROJECT);
        String senderUid = getIntent().getStringExtra(NotificationUtils.UID);

        firestoreUtils.getProfilePic(senderUid, mProfileImageView);

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

                            List<DocumentSnapshot> documents = Objects.requireNonNull(task.getResult()).getDocuments();
                            @SuppressWarnings(value = "unchecked")
                            List<String> skills = (List<String>) Objects.requireNonNull(documents.get(0).getData()).get(FirestoreUtils.KEY_SKILLS);

                            mSender = new Utente(null,
                                    sendResponseTo,
                                    documents.get(0).getReference().getId(),
                                    skills);

                            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                    this.getApplicationContext(),
                                    android.R.layout.simple_list_item_1,
                                    mSender.getComptetenze()
                            );

                            mSkillsListView.setAdapter(adapter);
                        }
            });

            negativeButton.setVisibility(View.VISIBLE);
            mSkillTitleTextView.setVisibility(View.VISIBLE);

            positiveButton.setText(R.string.accept_text);
            negativeButton.setText(R.string.reject_text);

            positiveButton.setOnClickListener(view -> {
                firestoreUtils.getFirestoreInstance().collection(FirestoreUtils.KEY_PROJECTS).whereEqualTo(FirestoreUtils.KEY_TITLE, project)
                        .get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot snapshot = Objects.requireNonNull(task.getResult()).getDocuments().get(0);
                                String id = snapshot.getId();
                                @SuppressWarnings(value = "unchecked") List<String> teammates = (List<String>) snapshot.get(FirestoreUtils.KEY_TEAMMATES);

                                List<String> team = new ArrayList<>();
                                if (teammates != null)
                                    team.addAll(teammates);

                                //  Verifica che l'utente non sia già teammate del progetto per evitare di inserirlo più di una volta
                                if (!team.contains(sendResponseTo)) {
                                    team.add(sendResponseTo);
                                    firestoreUtils.updateProjectData(id, FirestoreUtils.KEY_TEAMMATES, team);
                                    firestoreUtils.storeNotification(project, sendResponseTo, firebaseAuthUtils.getCurrentUser().getDisplayName(), firebaseAuthUtils.getCurrentUser().getUid(), NotificationType.LEADER_ACCEPT);
                                } else Toast.makeText(this, sendResponseTo + " is already a teammate on this project.", Toast.LENGTH_LONG).show();
                            } else Log.d(TAG, "Error responding or updating project");
                });

                onNotificationAcknowledged(notificationType, project);
            });

            negativeButton.setOnClickListener(view -> {
                firestoreUtils.storeNotification(project, sendResponseTo, firebaseAuthUtils.getCurrentUser().getDisplayName(), firebaseAuthUtils.getCurrentUser().getUid(), NotificationType.LEADER_REJECT);
                onNotificationAcknowledged(notificationType, project);
            });

        } else if (notificationType.equals(NotificationType.LEADER_ACCEPT)) {
            negativeButton.setVisibility(View.INVISIBLE);
            mSkillTitleTextView.setVisibility(View.INVISIBLE);


            positiveButton.setText(R.string.ok_text);
            //  Intent che apre ProjectActivity col progetto per il quale si è fatta la richiesta
            positiveButton.setOnClickListener(view -> onNotificationAcknowledged(notificationType, project));
        } else if (notificationType.equals(NotificationType.LEADER_REJECT)) {
            negativeButton.setVisibility(View.INVISIBLE);
            mSkillTitleTextView.setVisibility(View.INVISIBLE);

            positiveButton.setText(R.string.ok_text);
            positiveButton.setOnClickListener(view -> onNotificationAcknowledged(notificationType, project));
        }
    }

    private void onNotificationOpened() {
        //  Rimuove il documento relativo alla notifica corrente da Firestore
        firestoreUtils.getFirestoreInstance().collection(FirestoreUtils.KEY_USERS)
                .whereEqualTo(FirestoreUtils.KEY_NAME, getIntent().getStringExtra(NotificationUtils.RECIPIENT))
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot snapshot = Objects.requireNonNull(task.getResult()).getDocuments().get(0);
                snapshot.getReference().collection(FirestoreUtils.KEY_NOTIFICATIONS)
                        .whereEqualTo(NotificationUtils.PROJECT, getIntent().getStringExtra(NotificationUtils.PROJECT))
                        .get().addOnCompleteListener(notificationTask -> {
                    if (notificationTask.isSuccessful()) {
                        DocumentSnapshot notificationSnapshot = Objects.requireNonNull(notificationTask.getResult()).getDocuments().get(0);
                        notificationSnapshot.getReference().delete().addOnCompleteListener(removeTask -> {
                            if (removeTask.isSuccessful()) Log.d(TAG, "Notification received and removed from Firestore");
                        });
                    }
                });
            }
        });
    }

    //  Se la notifica equivale a LEADER_ACCEPT, porta l'utente
    //  alla schermata del progetto per il quale è appena diventato teammate,
    //  altrimenti riporta l'utente alla schermata principale.
    private void onNotificationAcknowledged(NotificationType response, String project) {
        if (response == NotificationType.TEAMMATE_REQUEST || response == NotificationType.LEADER_REJECT) {
            Intent homeIntent = new Intent(this, MainActivity.class);
            startActivity(homeIntent);
        } else if (response == NotificationType.LEADER_ACCEPT) {
            Intent viewProjectIntent = new Intent(this, ProjectActivity.class);
            viewProjectIntent.putExtra(FirestoreUtils.KEY_TITLE, project);
            startActivity(viewProjectIntent);
        }
        finish();
    }
}