package com.example.teamup.fragment.discover;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teamup.R;
import com.example.teamup.utilities.DiscoveryProjectsAdapter;
import com.example.teamup.utilities.FirestoreUtils;
import com.example.teamup.utilities.Progetto;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DiscoverFragment extends Fragment {
    public static final String TAG = DiscoverFragment.class.getSimpleName();

    private FirestoreUtils mFirestoreUtils;

    private SearchView mSearchView;
    private RecyclerView mProjectsListView;
    private DiscoveryProjectsAdapter mListAdapter;

    private List<Progetto> mProjects;

    private Activity mParentActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_discover, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG, "onViewCreated");

        mParentActivity = requireActivity();

        mFirestoreUtils = new FirestoreUtils(FirebaseFirestore.getInstance());

        mSearchView = mParentActivity.findViewById(R.id.searchView);
        mProjectsListView = mParentActivity.findViewById(R.id.projects_listView);
    }

    @Override
    public void onStart() {
        super.onStart();

        Log.d(TAG, "onStart");

        mProjects = new ArrayList<>();

        displayProjects();

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                mListAdapter.getFilter().filter(s);
                return false;
            }
        });

    }

    private void displayProjects() {
        //  Ottiene i riferimenti ai progetti memorizzati e li visualizza nella ListView
        Query query = mFirestoreUtils.getFirestoreInstance().collection(FirestoreUtils.KEY_PROJECTS);
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot snapshot : Objects.requireNonNull(task.getResult())) {

                    @SuppressWarnings(value = "unchecked") List<String> tags = (List<String>) snapshot.getData().get(FirestoreUtils.KEY_TAGS);
                    @SuppressWarnings(value = "unchecked") List<String> teammates = (List<String>) snapshot.getData().get(FirestoreUtils.KEY_TEAMMATES);
                    @SuppressWarnings(value = "unchecked") Map<String, Boolean> objectives = (Map<String, Boolean>) snapshot.getData().get(FirestoreUtils.KEY_OBJ);

                    //   Crea una lista di Progetti corrispondenti alla ListView dei titoli di progetto
                    mProjects.add(new Progetto(snapshot.getId(),
                            (String) snapshot.getData().get(FirestoreUtils.KEY_LEADER),
                            (String) snapshot.getData().get(FirestoreUtils.KEY_TITLE),
                            (String) snapshot.getData().get(FirestoreUtils.KEY_DESC),
                            tags,
                            teammates,
                            objectives,
                            (Boolean) snapshot.getData().putIfAbsent(FirestoreUtils.KEY_SPONSORED, false)));

                    putSponsoredFirst();
                }

                LinearLayoutManager layoutManager = new LinearLayoutManager(mParentActivity, RecyclerView.VERTICAL, false);
                mProjectsListView.setLayoutManager(layoutManager);

                mListAdapter = new DiscoveryProjectsAdapter(mParentActivity, mProjects);
                mProjectsListView.setAdapter(mListAdapter);
                mListAdapter.notifyDataSetChanged();
            }
        });
    }

    private void putSponsoredFirst() {
        //  Riordina la lista ponendo i progetti sponsorizzati in testa
        int j = 0;
        for (int i = 0; i < mProjects.size(); i++) {
            if (mProjects.get(i).isSponsored()) {
                Collections.swap(mProjects, i, j);
                j++;
            }
        }
    }
}