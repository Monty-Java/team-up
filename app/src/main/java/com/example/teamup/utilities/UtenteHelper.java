package com.example.teamup.utilities;

import java.util.ArrayList;
import java.util.List;

abstract class UtenteHelper {

    private static final String TAG = UtenteHelper.class.getSimpleName();

    static String getNome() {
        String name = "";
        // TODO: implementare recupero dati da database
        return name;
    }

    static String getCognome() {
        String cognome = "";
        // TODO: implementare recupero dati da database
        return cognome;
    }

    static String getEmail() {
        String email = "";
        // TODO: implementare recupero dati da database
        return email;
    }

    static List<String> getComptetenze() {
        List<String> comptetenze = new ArrayList<>();
        // TODO: implementare recupero dati da database
        return comptetenze;
    }

    static List<Progetto> getProgetti() {
        List<Progetto> progetti = new ArrayList<>();
        // TODO: implementare recupero dati da database
        return progetti;
    }
}
