package com.example.teamup.utilities;

import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

public class Utente {
    private static final String TAG = Utente.class.getSimpleName();

    private Uri profileImageUri;
    private String displayName;
    private String email;
    private List<String> comptetenze;

    public Utente(Uri profileImageUri, String name, String mail, List<String> skills) {
        this.profileImageUri = profileImageUri;
        displayName = name;
        email = mail;
        comptetenze = new ArrayList<>();
        comptetenze.addAll(skills);
    }

    public Uri getProfileImageUri() {
        return profileImageUri;
    }

    public String getDisplayName() { return displayName; }

    public String getEmail() { return email; }

    public List<String> getComptetenze() { return comptetenze; }

    public void removeSkill(String skillToRemove) { comptetenze.remove(skillToRemove); }
}
