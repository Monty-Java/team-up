package com.example.teamup.utilities;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FirestoreUtils {
    private static final String TAG = FirestoreUtils.class.getSimpleName();

    private FirebaseFirestore firestore;

    public FirestoreUtils(FirebaseFirestore firestore) { this.firestore = firestore; }

    public FirebaseFirestore getFirestoreInstance() { return this.firestore; }

    public void storeUserData(FirebaseAuthUtils userAuth, List<String> skills) {
        if (userAuth.getCurrentUser() != null) {
            //  Ottiene nome e cognome dell'utente dividendo il display name in due
            String[] displayName = userAuth.getCurrentUser().getDisplayName().split(" ");
            String name = displayName[0];
            String surname = displayName[1];

            Map<String, Object> userData = new HashMap<>();
            userData.put("name", name);
            userData.put("surname", surname);
            userData.put("skills", skills);

            DocumentReference userDocument = firestore.collection("users")
                    .document(Objects.requireNonNull(userAuth.getCurrentUser().getEmail()));
            userDocument.get().addOnCompleteListener(task -> {
                writeToDocument(task, userData);
            });
        }
    }

    public void storeNewProjectData(String title, String desc, String leader,
                                 Map<String, Boolean> objectives, List<String> tags) {
        Map<String, Object> projectData = new HashMap<>();
        projectData.put("title", title);
        projectData.put("leader", leader);
        projectData.put("description", desc);
        projectData.put("objectives", objectives);
        projectData.put("tags", tags);

        DocumentReference document = firestore.collection("projects").document();
        document.get().addOnCompleteListener(task -> {
            writeToDocument(task, projectData);
        });
    }

    public void updateProjectData(String id, String field, Object data) {
        firestore.collection("projects").document(id)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        Map<String, Object> newData = new HashMap<>();
                        newData.put(field, data);

                        task.getResult().getReference().update(field, data).addOnCompleteListener(t -> {
                            if (t.isSuccessful()) Log.d(TAG, "Document updated");
                            else Log.d(TAG, "Error updating document");
                        });
                    }
        });
    }

    public void writeToDocument(Task<DocumentSnapshot> task, Map<String, Object> data) {
        if (task.isSuccessful()) {
            DocumentSnapshot snapshot = task.getResult();
            assert snapshot != null;
            snapshot.getReference().set(data);
        } // TODO: mettere una condizione else in caso di errori
    }
}
