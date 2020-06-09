package com.example.teamup.ui.discover;

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
import java.util.List;
import java.util.Map;

public class DiscoverFragment extends Fragment {

    public static final String TAG = DiscoverFragment.class.getSimpleName();

    private FirestoreUtils firestoreUtils;

    private SearchView mSearchView;
    private RecyclerView mProjectsListView;
    private DiscoveryProjectsAdapter listAdapter;

    private List<Progetto> mProjects;

    private Activity parentActivity;

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

        parentActivity = requireActivity();

        firestoreUtils = new FirestoreUtils(FirebaseFirestore.getInstance());

        mSearchView = parentActivity.findViewById(R.id.searchView);
        mProjectsListView = parentActivity.findViewById(R.id.projects_listView);
    }

    @Override
    public void onStart() {
        super.onStart();

        Log.d(TAG, "onStart");

        mProjects = new ArrayList<>();

        //  Ottiene i riferimenti ai progetti memorizzati e li visualizza nella ListView
        Query query = firestoreUtils.getFirestoreInstance().collection(FirestoreUtils.KEY_PROJECTS);
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot snapshot : task.getResult()) {
                    //   Crea una lista di Progetti corrispondenti alla ListView dei titoli di progetto
                    mProjects.add(new Progetto(snapshot.getId(),
                            snapshot.getData().get(FirestoreUtils.KEY_LEADER).toString(),
                            snapshot.getData().get(FirestoreUtils.KEY_TITLE).toString(),
                            snapshot.getData().get(FirestoreUtils.KEY_DESC).toString(),
                            (List<String>) snapshot.getData().get(FirestoreUtils.KEY_TAGS),
                            (List<String>) snapshot.getData().get(FirestoreUtils.KEY_TEAMMATES),
                            (Map<String, Boolean>) snapshot.getData().get(FirestoreUtils.KEY_OBJ)));
                }

                LinearLayoutManager layoutManager = new LinearLayoutManager(parentActivity, RecyclerView.VERTICAL, false);
                mProjectsListView.setLayoutManager(layoutManager);

                listAdapter = new DiscoveryProjectsAdapter(parentActivity, mProjects);
                mProjectsListView.setAdapter(listAdapter);
                listAdapter.notifyDataSetChanged();
            }
        });

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                listAdapter.getFilter().filter(s);
                return false;
            }
        });

    }
}