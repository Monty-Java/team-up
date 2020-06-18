package com.example.teamup.fragment.profile;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.teamup.R;
import com.example.teamup.utilities.FirebaseAuthUtils;
import com.example.teamup.utilities.FirestoreUtils;
import com.example.teamup.utilities.Utente;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

//  TODO: funzionalità per rendere l'e-mail e la foto profilo private

public class ProfileFragment extends Fragment {
    public static final String TAG = ProfileFragment.class.getSimpleName();

    private FirestoreUtils firestoreUtils;
    private FirebaseAuthUtils firebaseAuthUtils;

    //  UI
    private ImageView mProfilePicImageView;
    private TextView mDisplayNameTextView;
    private TextView mEmailTextView;
    private Button mViewSkillsButton;

    Utente mUser;

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

        firestoreUtils.getFirestoreInstance().collection(FirestoreUtils.KEY_USERS)
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot snapshot : task.getResult()) {
                    if (snapshot.getReference().getId().equals(firebaseAuthUtils.getCurrentUser().getEmail())) {
                        mUser = new Utente(firebaseAuthUtils.getCurrentUser().getDisplayName(),
                                firebaseAuthUtils.getCurrentUser().getEmail(),
                                (List<String>) snapshot.getData().get(FirestoreUtils.KEY_SKILLS));
                        break;
                    }
                }

                //  TODO: implementare funzionalità per mettere foto profilo
                mProfilePicImageView.setImageResource(R.drawable.ic_launcher_foreground);

                //  Ottiene i dati dell'utente da Firestore e li popola
                mDisplayNameTextView.setText(mUser.getDisplayName());
                mEmailTextView.setText(mUser.getEmail());

                mViewSkillsButton.setOnClickListener(view -> {
                    Dialog skillsDialog = new Dialog(this.getContext());
                    skillsDialog.setContentView(R.layout.profile_skills_dialog);
                    ListView skillsListView = skillsDialog.findViewById(R.id.skillsListView);


                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                            this.getContext(),
                            android.R.layout.simple_list_item_1,
                            mUser.getComptetenze()
                    );

                    skillsListView.setAdapter(adapter);

                    Button addSkillButton = skillsDialog.findViewById(R.id.addSkillButton);
                    Button closeButton = skillsDialog.findViewById(R.id.closeDialogButton);

                    //  Click su una competenza crea un AlertDialog che permette all'utente di rimuovere la competenza dalla lista
                    skillsListView.setOnItemClickListener((parent, listview, position, id) -> {
                        String skill = skillsListView.getItemAtPosition(position).toString();

                        AlertDialog.Builder removeSkillDialogBuilder = new AlertDialog.Builder(this.getContext());
                        removeSkillDialogBuilder.setTitle("Remove Skill");
                        removeSkillDialogBuilder.setMessage("Are you sure you want to remove " + skill + " from your list of skills?");
                        removeSkillDialogBuilder.setPositiveButton("OK", ((dialog, which) -> {
                            mUser.removeSkill(skill);
                            firestoreUtils.updateUserData(mUser.getDisplayName(), FirestoreUtils.KEY_SKILLS, mUser.getComptetenze());
                            dialog.dismiss();
                        }));
                        removeSkillDialogBuilder.setNegativeButton("Cancel", ((dialog, which) -> { dialog.dismiss(); }));

                        AlertDialog removeSkillDialog = removeSkillDialogBuilder.create();
                        removeSkillDialog.show();
                    });

                    //  Crea un Dialog che permette di aggiungere una nuova competenza
                    addSkillButton.setOnClickListener(l -> {
                        Dialog addSkillDialog = new Dialog(this.getContext());
                        addSkillDialog.setContentView(R.layout.add_skill_dialog);
                        Button positiveButton = addSkillDialog.findViewById(R.id.add_skill_positiveButton);
                        Button negativeButton = addSkillDialog.findViewById(R.id.add_skill_negativeButton);
                        negativeButton.setOnClickListener(v -> addSkillDialog.dismiss());
                        positiveButton.setOnClickListener(v -> {
                            EditText newSkillEditText = addSkillDialog.findViewById(R.id.add_skill_editText);
                            if (!newSkillEditText.getText().toString().equals("")) {
                                mUser.getComptetenze().add(newSkillEditText.getText().toString());
                                firestoreUtils.updateUserData(mUser.getDisplayName(), FirestoreUtils.KEY_SKILLS, mUser.getComptetenze());
                                addSkillDialog.dismiss();
                            } else Toast.makeText(this.getContext(), "No skill specified", Toast.LENGTH_LONG).show();
                        });
                        addSkillDialog.show();
                    });

                    closeButton.setOnClickListener(v -> {
                        skillsDialog.hide();
                    });
                    skillsDialog.show();
                });


            }
        });

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}
