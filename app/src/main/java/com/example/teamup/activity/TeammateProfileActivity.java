package com.example.teamup.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.example.teamup.R;
import com.example.teamup.utilities.FirestoreUtils;
import com.example.teamup.utilities.Utente;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class TeammateProfileActivity extends AppCompatActivity {
    public static final String TAG = TeammateProfileActivity.class.getSimpleName();

    Utente mTeammate;

    FirestoreUtils firestoreUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teammate_profile);
        String teammate = getIntent().getStringExtra("teammate");

        firestoreUtils = new FirestoreUtils(FirebaseFirestore.getInstance());
        firestoreUtils.getFirestoreInstance().collection(FirestoreUtils.KEY_USERS)
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot snapshot : task.getResult()) {
                    if (snapshot.getData().get(FirestoreUtils.KEY_NAME).equals(teammate)) {
                        mTeammate = new Utente(
                                null,
                                teammate,
                                snapshot.getReference().getId(),
                                (List<String>) snapshot.getData().get(FirestoreUtils.KEY_SKILLS));
                        break;
                    }
                }

                Log.d(TAG, mTeammate.getDisplayName());
                Log.d(TAG, mTeammate.getEmail());
                Log.d(TAG, mTeammate.getComptetenze().toString());
            }
        });
    }
}