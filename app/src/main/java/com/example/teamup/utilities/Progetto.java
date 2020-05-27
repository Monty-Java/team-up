package com.example.teamup.utilities;
import android.util.Log;

import java.util.*;
import java.lang.reflect.*;

class Progetto {
    private static final String TAG = Progetto.class.getSimpleName();

    private String id;
    private String titolo;
    private String descrizione;
    private List<Tag> etichette;
    private Leader leader;
    private List<Teammate> personale;
    private List<String> obiettiviRaggiunti;
    private List<String> obiettiviDaRaggiungere;

    Progetto(Leader creator) {
        this.leader = creator;
        //  genera id
        UUID uuid = UUID.randomUUID();
        id = uuid.toString();
    }

    void setTitolo(Utente user, String title) {
        if(this.leader == user) {
            titolo = title;
        } else
            Log.d(TAG, "Error, you're not the leader of this project!");
    }

    void setDescrizione(Utente user,String description) {
        if(this.leader == user) {
            descrizione = description;
        } else
            Log.d(TAG, "Error, you're not the leader of this project!");
    }

    void setEtichette(Utente user,List<Tag> labels) {
        if(this.leader == user) {
            etichette = labels;
        } else
            Log.d(TAG, "Error, you're not the leader of this project!");
    }

    void setPersonale(Utente user, List<Teammate> mates) {
        if(this.leader == user) {
            personale = mates;
        } else
            Log.d(TAG, "Error, you're not the leader of this project!");
    }
    void setObiettiviRaggiunti(Utente user, List<String> reachedTarget) {
        if(this.leader == user) {
            obiettiviRaggiunti = reachedTarget;
        } else
            Log.d(TAG, "Error, you're not the leader of this project!");
    }

    void setObiettiviDaRaggiungere(Utente user, List<String> targetToReach) {
        if(this.leader == user) {
            obiettiviDaRaggiungere = targetToReach;
        } else
            Log.d(TAG, "Error, you're not the leader of this project!");
    }

    String getId() { return id; }
    String getTitolo() { return titolo; }
    String getDescrizione() { return descrizione; }
    List<Tag> getEtichette() { return etichette; }
    Leader getLeader() { return leader; }
    List<Teammate> getPersonale() { return personale; }
    List<String> getObiettiviRaggiunti() { return obiettiviRaggiunti; }
    List<String> getObiettiviDaRaggiungere() { return obiettiviDaRaggiungere; }
}
