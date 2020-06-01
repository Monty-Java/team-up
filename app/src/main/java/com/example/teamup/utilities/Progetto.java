package com.example.teamup.utilities;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Progetto {
    private static final String TAG = Progetto.class.getSimpleName();

    private String id;
    private String titolo;
    private String descrizione;
    private List<String> etichette;
    private String leader;
    private List<String> teammates;
    private Map<String, Boolean> obiettivi;

    public Progetto(String id, String leader, String titolo, String descrizione, List<String> etichette, Map<String, Boolean> goalsToAchieve) {
        Log.d(TAG, "Costruttore");

        this.id = id;
        this.leader = leader;
        this.titolo = titolo;
        this.descrizione = descrizione;
        this.etichette = etichette;
        this.obiettivi = new HashMap<>();
        this.teammates = new ArrayList<>();

        this.obiettivi.putAll(goalsToAchieve);

        /*for (String goal : goalsToAchieve) {
            this.obiettivi.put(goal, false);
        }*/
    }

    public void setTitolo(String title) {
        titolo = title;
    }

    public void setDescrizione(String description) {
        descrizione = description;
    }

    public void addEtichetta(String tag) {
        etichette.add(tag);
    }

    public void removeEtichetta(String tag) {
        List<String> result = new ArrayList<>();

        for (String oldTag : etichette) {
            if (!oldTag.equals(tag)) {
                result.add(oldTag);
            }
        }

        etichette = new ArrayList<>(result);
    }

    public void addTeammate(String teammate) {
        teammates.add(teammate);
    }

    public void removeTeammate(String teammate) {
        List<String> result = new ArrayList<>();

        for (String oldTeammate : teammates) {
            if (!oldTeammate.equals(teammate)) {
                result.add(oldTeammate);
            }
        }

        teammates = new ArrayList<>(result);
    }

    public void addObiettivoDaRaggiungere(String goal) {
        obiettivi.put(goal, false);
    }

    public void removeObiettivo(String goalToRemove) {
        obiettivi.remove(goalToRemove);
    }

    public void setObiettivoRaggiunto(String goalAchieved) {
        obiettivi.replace(goalAchieved, true);
    }

    public String getId() {
        return id;
    }

    public String getTitolo() {
        return titolo;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public List<String> getEtichette() {
        return etichette;
    }

    public String getLeader() {
        return leader;
    }

    public List<String> getTeammates() {
        return teammates;
    }

    public Map<String, Boolean> getObiettivi() {
        return obiettivi;
    }

    // Metodo di supporto per poter calcolare la percentuale di completezza del progetto
    public double obiettiviCompleti() {
        int n = 0;
        for (Map.Entry<String, Boolean> obj : getObiettivi().entrySet())
            if (obj.getValue()) n++;

        return n;
    }

    // Metodo di supporto per poter calcolare la percentuale di completezza del progetto
    public double numeroObiettivi() { return obiettivi.size(); }

    public boolean hasTeammates() { return teammates.isEmpty(); }
}
