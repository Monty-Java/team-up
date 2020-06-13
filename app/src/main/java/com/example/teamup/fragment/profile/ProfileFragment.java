package com.example.teamup.fragment.profile;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.teamup.R;
import com.example.teamup.utilities.FirebaseAuthUtils;
import com.example.teamup.utilities.FirestoreUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

//  TODO: funzionalità per rendere l'e-mail e la foto profilo private

public class ProfileFragment extends Fragment {

    private FirestoreUtils firestoreUtils;
    private FirebaseAuthUtils firebaseAuthUtils;

    //  UI
    private ImageView mProfilePicImageView;
    private TextView mDisplayNameTextView;
    private TextView mEmailTextView;
    private Button mViewSkillsButton;

    private List<String> mSkills;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        View layout = root.findViewById(R.id.layout_profile);

        firestoreUtils = new FirestoreUtils(FirebaseFirestore.getInstance());
        firebaseAuthUtils = new FirebaseAuthUtils(FirebaseAuth.getInstance(), firestoreUtils.getFirestoreInstance(), this.getActivity());

        mProfilePicImageView = layout.findViewById(R.id.profilePic_imageView);
        mDisplayNameTextView = layout.findViewById(R.id.displayName_textView);
        mEmailTextView = layout.findViewById(R.id.email_textView);
        mViewSkillsButton = layout.findViewById(R.id.viewSkills_button);

        //  TODO: implementare funzionalità per mettere foto profilo
        mProfilePicImageView.setImageResource(R.drawable.ic_launcher_foreground);

        //  Ottiene i dati dell'utente da Firestore e li popola
        mDisplayNameTextView.setText(firebaseAuthUtils.getCurrentUser().getDisplayName());
        mEmailTextView.setText(firebaseAuthUtils.getCurrentUser().getEmail());

        //  TODO: modificare il dialog in modo da poter aggiungere, rimuovere o modificare le competenze
        mViewSkillsButton.setOnClickListener(view -> {
            Dialog skillsDialog = new Dialog(this.getContext());
            skillsDialog.setContentView(R.layout.profile_skills_dialog);
            ListView skillsListView = skillsDialog.findViewById(R.id.skillsListView);

            firestoreUtils.getFirestoreInstance().collection(FirestoreUtils.KEY_USERS).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot snapshot : task.getResult().getDocuments()) {
                        if (snapshot.getReference().getId().equals(firebaseAuthUtils.getCurrentUser().getEmail())) {
                            mSkills = (List<String>) snapshot.getData().get("skills");

                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                                    this.getContext(),
                                    android.R.layout.simple_list_item_1,
                                    mSkills
                            );

                            skillsListView.setAdapter(adapter);
                        }
                    }
                }
            });
            Button closeButton = skillsDialog.findViewById(R.id.closeDialogButton);
            closeButton.setOnClickListener(v -> {
                skillsDialog.hide();
            });
            skillsDialog.show();
        });

        /*
                final TextView textView = root.findViewById(R.id.missionStatementTextView);
        mViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
         */

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}
