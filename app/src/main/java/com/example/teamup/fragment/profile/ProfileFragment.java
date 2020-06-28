package com.example.teamup.fragment.profile;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.teamup.R;
import com.example.teamup.utilities.FirebaseAuthUtils;
import com.example.teamup.utilities.FirestoreUtils;
import com.example.teamup.utilities.Utente;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {
    public static final String TAG = ProfileFragment.class.getSimpleName();

    private static final int TAKE_IMAGE_CODE = 10001;

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

        setHasOptionsMenu(true);

        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        firestoreUtils = new FirestoreUtils(FirebaseFirestore.getInstance());
        firebaseAuthUtils = new FirebaseAuthUtils(FirebaseAuth.getInstance(), firestoreUtils.getFirestoreInstance(), this.getActivity());

        mProfilePicImageView = root.findViewById(R.id.profilePic_imageView);
        mProfilePicImageView.setOnClickListener(this::onProfileImageClick);     //  Listener per creare una nuova foto profilo

        mDisplayNameTextView = root.findViewById(R.id.displayName_textView);
        mEmailTextView = root.findViewById(R.id.email_textView);
        mViewSkillsButton = root.findViewById(R.id.viewSkills_button);

        FirebaseUser firebaseUser = firebaseAuthUtils.getCurrentUser();
        getUserData(firebaseUser);

        return root;
    }

    private void onProfileImageClick(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivityForResult(intent, TAKE_IMAGE_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == TAKE_IMAGE_CODE) {
            if (resultCode == RESULT_OK) {
                assert data != null;
                Bitmap bitmap = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
                mProfilePicImageView.setImageBitmap(bitmap);
                if (bitmap != null) handleUpload(bitmap);
            }
        }
    }

    private void getUserData(FirebaseUser user) {
        //  Ottiene i dati relativi all'utente corrente da Firestore e li usa per istanziare un oggetto Utente
        firestoreUtils.getFirestoreInstance().collection(FirestoreUtils.KEY_USERS)
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot snapshot : Objects.requireNonNull(task.getResult())) {
                    if (snapshot.getReference().getId().equals(user.getEmail())) {

                        @SuppressWarnings(value = "unchecked")
                        List<String> skills = (List<String>) Objects.requireNonNull(snapshot.getData()).get(FirestoreUtils.KEY_SKILLS);

                        mUser = new Utente(
                                user.getPhotoUrl(),
                                user.getDisplayName(),
                                user.getEmail(),
                                skills);
                        break;
                    }
                }

                if (mUser.getProfileImageUri() != null) {
                    Glide.with(this.requireContext())
                            .load(mUser.getProfileImageUri())
                            .into(mProfilePicImageView);
                } else {
                    Glide.with(this.requireContext())
                            .load(R.drawable.default_profile_image)
                            .into(mProfilePicImageView);
                }

                String displayNameText = "Username: " + mUser.getDisplayName();
                String emailText = "E-mail Address: " + mUser.getEmail();
                mDisplayNameTextView.setText(displayNameText);
                mEmailTextView.setText(emailText);

                mViewSkillsButton.setOnClickListener(view -> viewUserSkills());
            }
        });
    }

    private void viewUserSkills() {
        Dialog skillsDialog = new Dialog(this.requireContext());
        skillsDialog.setContentView(R.layout.profile_skills_dialog);
        ListView skillsListView = skillsDialog.findViewById(R.id.skillsListView);
        Button addSkillButton = skillsDialog.findViewById(R.id.addSkillButton);
        Button closeButton = skillsDialog.findViewById(R.id.closeDialogButton);

        ArrayAdapter<String> skillsAdapter = new ArrayAdapter<>(
                this.requireContext(),
                android.R.layout.simple_list_item_1,
                mUser.getComptetenze()
        );
        skillsListView.setAdapter(skillsAdapter);

        //  Click su una competenza crea un AlertDialog che permette all'utente di rimuovere la competenza dalla lista
        skillsListView.setOnItemClickListener((parent, listview, position, id) -> removeSkill(parent, skillsAdapter, position));

        //  Crea un Dialog che permette di aggiungere una nuova competenza
        addSkillButton.setOnClickListener(l -> {
            addSkill(skillsAdapter);
            //skillsAdapter.notifyDataSetChanged();
        });

        closeButton.setOnClickListener(v -> skillsDialog.hide());
        skillsDialog.show();
    }

    private void removeSkill(AdapterView<?> parent, ArrayAdapter<String> adapter, int position) {
        String skill = parent.getItemAtPosition(position).toString();

        AlertDialog.Builder removeSkillDialogBuilder = new AlertDialog.Builder(this.requireContext());
        removeSkillDialogBuilder.setTitle("Remove Skill");
        removeSkillDialogBuilder.setMessage("Are you sure you want to remove " + skill + " from your list of skills?");

        removeSkillDialogBuilder.setPositiveButton("OK", ((dialog, which) -> {
            mUser.removeSkill(skill);
            firestoreUtils.updateUserData(mUser.getDisplayName(), FirestoreUtils.KEY_SKILLS, mUser.getComptetenze());
            adapter.notifyDataSetChanged();
            dialog.dismiss();
        }));
        removeSkillDialogBuilder.setNegativeButton("Cancel", ((dialog, which) -> dialog.dismiss()));

        AlertDialog removeSkillDialog = removeSkillDialogBuilder.create();
        removeSkillDialog.show();
    }

    private void addSkill(ArrayAdapter<String> adapter) {
        Dialog addSkillDialog = new Dialog(this.requireContext());
        addSkillDialog.setContentView(R.layout.add_skill_dialog);
        Button positiveButton = addSkillDialog.findViewById(R.id.add_skill_positiveButton);
        Button negativeButton = addSkillDialog.findViewById(R.id.add_skill_negativeButton);

        negativeButton.setOnClickListener(v -> addSkillDialog.dismiss());
        positiveButton.setOnClickListener(v -> {
            EditText newSkillEditText = addSkillDialog.findViewById(R.id.add_skill_editText);
            if (!newSkillEditText.getText().toString().equals("")) {
                mUser.getComptetenze().add(newSkillEditText.getText().toString());
                firestoreUtils.updateUserData(mUser.getDisplayName(), FirestoreUtils.KEY_SKILLS, mUser.getComptetenze());
                adapter.notifyDataSetChanged();
                addSkillDialog.dismiss();
            } else newSkillEditText.setError("No skill specified");
        });

        addSkillDialog.show();
    }

    private void handleUpload(Bitmap bitmap) {
        Log.d(TAG, "handleUpload");

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);

        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        StorageReference reference = FirebaseStorage.getInstance().getReference()
                .child("profileImages")
                .child(uid + ".jpeg");
        reference.putBytes(byteArrayOutputStream.toByteArray())
                .addOnSuccessListener(taskSnapshot -> getDownloadUrl(reference))
                .addOnFailureListener(e -> Log.e(TAG, "onFailure: ", e.getCause()));
    }

    private void getDownloadUrl(StorageReference reference) {
        Log.d(TAG, "getDownloadUrl");

        reference.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    Log.d(TAG, "onSuccess: " + uri);
                        setUserProfileUrl(uri);
                });
    }

    private void setUserProfileUrl(Uri uri) {
        Log.d(TAG, "setUserProfileUrl");

        FirebaseUser user = firebaseAuthUtils.getCurrentUser();
        if (user != null) {
            UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                    .setPhotoUri(uri)
                    .build();

            user.updateProfile(request)
                    .addOnSuccessListener(aVoid -> Toast.makeText(requireActivity(), "Updated successfully", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(requireActivity(), "Profile image failed...", Toast.LENGTH_SHORT).show());
        } else Toast.makeText(this.requireContext(), "Error. Unable to obtain user data", Toast.LENGTH_LONG).show();
    }
}
