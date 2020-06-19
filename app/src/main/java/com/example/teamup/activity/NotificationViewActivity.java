package com.example.teamup.activity;

import android.content.Intent;
import android.net.Uri;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
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

        //  Rimuove il documento relativo alla notifica corrente da Firestore
        firestoreUtils.getFirestoreInstance().collection(FirestoreUtils.KEY_USERS)
                .whereEqualTo(FirestoreUtils.KEY_NAME, getIntent().getStringExtra(NotificationUtils.RECIPIENT))
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        task.getResult().getDocuments().get(0).getReference().collection(FirestoreUtils.KEY_NOTIFICATIONS)
                                .whereEqualTo(NotificationUtils.PROJECT, getIntent().getStringExtra(NotificationUtils.PROJECT))
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

        NotificationType notificationType = NotificationType.valueOf(getIntent().getStringExtra(NotificationUtils.TYPE));
        String sendResponseTo = getIntent().getStringExtra(NotificationUtils.SENDER);
        String project = getIntent().getStringExtra(NotificationUtils.PROJECT);
        String senderUid = getIntent().getStringExtra(NotificationUtils.UID);
        Log.d(TAG, "UID: " + senderUid);

        //  TODO: usare UID per ottenere un riferimento alla foto profilo dell'utente che ha inviato la notifica
        getProfilePic(senderUid, mProfileImageView);

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

                            mSender = new Utente(null, sendResponseTo,
                                    task.getResult().getDocuments().get(0).getReference().getId(),
                                    (List<String>) task.getResult().getDocuments().get(0).getData().get(FirestoreUtils.KEY_SKILLS));

                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                                    this.getApplicationContext(),
                                    android.R.layout.simple_list_item_1,
                                    mSender.getComptetenze()
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

            //  TODO: nascondi Skills TextView, visualizzare un messaggio che si congratula con l'utente per essere stato accettato

            //  Intent che apre ProjectActivity col progetto per il quale si è fatta la richiesta
            positiveButton.setOnClickListener(view -> {
                onNotificationAcknowledged(notificationType, project);
            });
            Log.d(TAG, "Request Accepted");
        } else if (notificationType.equals(NotificationType.LEADER_REJECT)) {
            negativeButton.setVisibility(View.INVISIBLE);
            //  Messaggio per indicare che il leader ha rifiutato la richiesta
            Log.d(TAG, "Request Rejected");

            //  TODO: nascondi Skills TextView, visualizzare un messaggio che  informa l'utente che la richiesta è stata rifiutata

            positiveButton.setOnClickListener(view -> {
                onNotificationAcknowledged(notificationType, project);
            });
        }
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

    //  TODO: duplicato del metodo in MainActivity -- spostarlo in una classe Util e renderlo public
    private void getProfilePic(String uid, ImageView imageView) {
        FirebaseStorage storage = FirebaseStorage.getInstance("gs://teamup-41bb3.appspot.com");
        StorageReference gsReference = storage
                .getReferenceFromUrl("gs://teamup-41bb3.appspot.com/profileImages")
                .child(uid + ".jpeg");
        try {
            File localProfilePic = File.createTempFile("profile_pic", "jpeg");
            gsReference.getFile(localProfilePic).addOnSuccessListener(taskSnapshot -> {
                imageView.setImageURI(Uri.fromFile(localProfilePic));
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}