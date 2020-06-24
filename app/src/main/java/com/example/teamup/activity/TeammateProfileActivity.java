package com.example.teamup.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teamup.R;
import com.example.teamup.utilities.FirestoreUtils;
import com.example.teamup.utilities.Utente;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Objects;

public class TeammateProfileActivity extends AppCompatActivity {
    public static final String TAG = TeammateProfileActivity.class.getSimpleName();

    //  UI
    ImageView mProfilePicImageView;
    TextView mEmailTextView;
    ListView mSkillsListView;
    FloatingActionButton mFab;

    Utente mTeammate;

    FirestoreUtils firestoreUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teammate_profile);
        String teammate = getIntent().getStringExtra("teammate");
        setTitle(teammate);

        mProfilePicImageView = findViewById(R.id.profilePicImageView);
        mEmailTextView = findViewById(R.id.emailTextView);
        mSkillsListView = findViewById(R.id.skillsListView2);
        mFab = findViewById(R.id.floatingActionButton2);

        firestoreUtils = new FirestoreUtils(FirebaseFirestore.getInstance());
        firestoreUtils.getFirestoreInstance().collection(FirestoreUtils.KEY_USERS)
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot snapshot : Objects.requireNonNull(task.getResult())) {
                    String username = (String) Objects.requireNonNull(snapshot.getData()).get(FirestoreUtils.KEY_NAME);
                    if (Objects.equals(username, teammate)) {

                        @SuppressWarnings(value = "unchecked")
                        List<String> skills = (List<String>) snapshot.getData().get(FirestoreUtils.KEY_SKILLS);

                        //  TODO: ottenere la foto dell'utente dallo storage
                        mTeammate = new Utente(
                                null,
                                teammate,
                                snapshot.getReference().getId(),
                                skills);
                        break;
                    }
                }

                mEmailTextView.setText(mTeammate.getEmail());

                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_list_item_1,
                        mTeammate.getComptetenze());
                mSkillsListView.setAdapter(adapter);
            }
        });
    }

    public void onTeammateFabClick(View view) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO,
                Uri.fromParts("mailto", mTeammate.getEmail(), null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "TeamUp");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Body");
        startActivity(Intent.createChooser(emailIntent, "Send e-mail to " + mTeammate.getDisplayName()));
    }
}