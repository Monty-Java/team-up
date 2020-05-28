package com.example.teamup.utilities;
import java.util.*;

public abstract class Utente {
    FirebaseAuthUtils userInfo;
    String nome;
    String cognome;
    String email;
    List<String> comptetenze;

    public
    abstract String getNome();
    abstract String getCognome();
    abstract String getEmail();
    abstract List<String> getComptetenze();

    void setCompetenze(String skills) {
        //  TODO: ottenere la lista delle competenze dalle SharedPreferences all'interno dell'Activity
        //  TODO: suddivedere le competenze usando substring() e inserendo le singole stringhe in una List<String>
    }
}
