package com.example.teamup.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ProfileViewModel extends ViewModel {
    private MutableLiveData<String> mText;

    //  Conterrà le viste per la lista delle competenze dell'utente
    //  e per la lista dei progetti in cui è coinvolto

    public ProfileViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is the user profile fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
