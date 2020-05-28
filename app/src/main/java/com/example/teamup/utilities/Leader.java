package com.example.teamup.utilities;
import com.google.firebase.auth.FirebaseUser;

import java.util.*;

public class Leader extends Utente {

    //  TODO: istanziare fornendo FirebaseAuthUtils.getCurrentUser() come parametro
    Leader(FirebaseUser user) {
        if (user != null) {
            this.nome = user.getDisplayName();
            this.cognome = user.getDisplayName();
            this.email = user.getEmail();
        }
    }

    public
    String getNome() { return nome; }
    String getCognome() { return cognome; }
    String getEmail() { return email; }
    List<String> getComptetenze() { return comptetenze; }
}
