package com.example.teamup.utilities;

import java.util.List;

public class Utente {
    private static final String TAG = Utente.class.getSimpleName();

    private final String nome;
    private final String cognome;
    private final String email;
    private final List<String> comptetenze;
    private final List<Progetto> projects;

    public Utente() {
        nome = UtenteHelper.getNome();
        cognome = UtenteHelper.getCognome();
        email = UtenteHelper.getEmail();
        comptetenze = UtenteHelper.getComptetenze();
        projects = UtenteHelper.getProgetti();
    }

    public String getNome() {
        return nome;
    }

    public String getCognome() {
        return cognome;
    }

    public String getEmail() {
        return email;
    }

    public List<String> getComptetenze() {
        return comptetenze;
    }

    public List<Progetto> getProjects() {
        return projects;
    }
}
