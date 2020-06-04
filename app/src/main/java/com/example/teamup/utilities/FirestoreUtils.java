package com.example.teamup.utilities;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FirestoreUtils {
    private static final String TAG = FirestoreUtils.class.getSimpleName();
    //  Costanti usate come chiavi per leggere e scrivere dati Firestore
    public static final String KEY_USERS = "users";
    private static final String KEY_NAME = "name";
    private static final String KEY_SURNAME = "surname";
    private static final String KEY_SKILLS = "skills";
    public static final String KEY_PROJECTS = "projects";
    public static final String KEY_TITLE = "title";
    public static final String KEY_LEADER = "leader";
    public static final String KEY_DESC = "description";
    public static final String KEY_OBJ = "objectives";
    public static final String KEY_TAGS = "tags";
    public static final String KEY_TEAMMATES = "teammates";

    private FirebaseFirestore firestore;

    public FirestoreUtils(FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    public FirebaseFirestore getFirestoreInstance() {
        return this.firestore;
    }

    public void storeUserData(FirebaseUser currentUser, List<String> skills) {
        if (currentUser != null) {
            //  Ottiene nome e cognome dell'utente dividendo il display name in due
            String[] displayName = currentUser.getDisplayName().split(" ");
            String name = displayName[0];
            String surname = displayName[1];

            Map<String, Object> userData = new HashMap<>();
            userData.put(KEY_NAME, name);
            userData.put(KEY_SURNAME, surname);
            userData.put(KEY_SKILLS, skills);

            DocumentReference userDocument = firestore.collection(KEY_USERS)
                    .document(Objects.requireNonNull(currentUser.getEmail()));
            userDocument.get().addOnCompleteListener(task -> writeToDocument(task, userData));
        }
    }

    public void storeNewProjectData(String title, String desc, String leader,
                                 Map<String, Boolean> objectives, List<String> tags) {
        Map<String, Object> projectData = new HashMap<>();
        projectData.put(KEY_TITLE, title);
        projectData.put(KEY_LEADER, leader);
        projectData.put(KEY_DESC, desc);
        projectData.put(KEY_OBJ, objectives);
        projectData.put(KEY_TAGS, tags);

        DocumentReference document = firestore.collection(KEY_PROJECTS).document();
        document.get().addOnCompleteListener(task -> writeToDocument(task, projectData));
    }

    public void updateProjectData(String id, String field, Object data) {
        firestore.collection(KEY_PROJECTS)
                .document(id)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        Map<String, Object> newData = new HashMap<>();
                        newData.put(field, data);

                        task.getResult().getReference().set(newData, SetOptions.merge()).addOnCompleteListener(t -> {
                            if (t.isSuccessful()) {
                                Log.d(TAG, "Document updated");
                            } else {
                                Log.e(TAG, "Error updating document");
                            }
                        });
                    }
        });
    }

    public void writeToDocument(Task<DocumentSnapshot> task, Map<String, Object> data) {
        if (task.isSuccessful()) {
            DocumentSnapshot snapshot = task.getResult();
            assert snapshot != null;
            snapshot.getReference().set(data);
        } else {
            Log.e(TAG, "Error updating document");
        }
    }
}
