package com.example.teamup.utilities;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Utente {
    private static final String TAG = Utente.class.getSimpleName();
    private String displayName;
    private String email;
    private List<String> comptetenze;

    public Utente(String name, String mail, List<String> skills) {
        displayName = name;
        email = mail;
        comptetenze = new ArrayList<>();
        comptetenze.addAll(skills);
    }

    public String getDisplayName() { return displayName; }

    public String getEmail() { return email; }

    public List<String> getComptetenze() { return comptetenze; }

    public void removeSkill(String skillToRemove) { comptetenze.remove(skillToRemove); }
}
