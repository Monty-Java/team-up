package com.example.teamup;

import android.os.Bundle;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teamup.utilities.DiscoveryProjectsAdapter;
import com.example.teamup.utilities.FirestoreUtils;
import com.example.teamup.utilities.Progetto;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DiscoverActivity extends AppCompatActivity {
    public static final String TAG = DiscoverActivity.class.getSimpleName();

    private FirestoreUtils firestoreUtils;

    private SearchView mSearchView;
    private RecyclerView mProjectsListView;
    private DiscoveryProjectsAdapter listAdapter;

    private List<Progetto> mProjects;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);

        firestoreUtils = new FirestoreUtils(FirebaseFirestore.getInstance());

        mSearchView = (SearchView) findViewById(R.id.searchView);
        mProjectsListView = findViewById(R.id.projects_listView);

        mProjects = new ArrayList<>();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setTitle("Discover");

        //  Ottiene i riferimenti ai progetti memorizzati e li visualizza nella ListView
        Query query = firestoreUtils.getFirestoreInstance().collection(FirestoreUtils.KEY_PROJECTS);
        query.get().addOnCompleteListener(task -> {
           if (task.isSuccessful()) {
               for (QueryDocumentSnapshot snapshot : task.getResult()) {
                   //   Crea una lista di Progetti corrispondenti alla ListView dei titoli di progetto
                   mProjects.add(new Progetto(snapshot.getId(),
                           (String) snapshot.getData().get(FirestoreUtils.KEY_LEADER).toString(),
                           (String) snapshot.getData().get(FirestoreUtils.KEY_TITLE).toString(),
                           (String) snapshot.getData().get(FirestoreUtils.KEY_DESC).toString(),
                           (List<String>) snapshot.getData().get(FirestoreUtils.KEY_TAGS),
                           (List<String>) snapshot.getData().get(FirestoreUtils.KEY_TEAMMATES),
                           (Map<String, Boolean>) snapshot.getData().get(FirestoreUtils.KEY_OBJ)));
               }

               LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
               mProjectsListView.setLayoutManager(layoutManager);

               listAdapter = new DiscoveryProjectsAdapter(this, mProjects);
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