package com.example.teamup.utilities;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
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

    public FirestoreUtils(FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

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
            //  TODO: inserire i progetti

            DocumentReference userDocument = firestore.collection("users").document(Objects.requireNonNull(userAuth.getCurrentUser().getEmail()));
            userDocument.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot snapshot = task.getResult();
                        assert snapshot != null;
                        snapshot.getReference().set(userData);
                    }
                }
            });
        }
    }
}
