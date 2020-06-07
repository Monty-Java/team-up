package com.example.teamup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teamup.utilities.FirestoreUtils;
import com.example.teamup.utilities.Progetto;
import com.google.firebase.firestore.DocumentSnapshot;
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
    private ListView mProjectsListView;
    private List<String> mProjectsList;
    private ArrayAdapter<String> listAdapter;

    private List<Progetto> mProjects;

    private List<String> mTagsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);

        firestoreUtils = new FirestoreUtils(FirebaseFirestore.getInstance());

        mSearchView = (SearchView) findViewById(R.id.searchView);
        mProjectsListView = (ListView) findViewById(R.id.projects_listView);

        mTagsList = new ArrayList<>();

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
               mProjectsList = new ArrayList<>();
               for (QueryDocumentSnapshot snapshot : task.getResult()) {
                   mProjectsList.add(snapshot.getData().get(FirestoreUtils.KEY_TITLE).toString());

                   //   Crea una lista di Progetti corrispondenti alla ListView dei titoli di progetto
                   mProjects.add(new Progetto(snapshot.getId(),
                           (String) snapshot.getData().get(FirestoreUtils.KEY_LEADER).toString(),
                           (String) snapshot.getData().get(FirestoreUtils.KEY_TITLE).toString(),
                           (String) snapshot.getData().get(FirestoreUtils.KEY_DESC).toString(),
                           (List<String>) snapshot.getData().get(FirestoreUtils.KEY_TAGS),
                           (List<String>) snapshot.getData().get(FirestoreUtils.KEY_TEAMMATES),
                           (Map<String, Boolean>) snapshot.getData().get(FirestoreUtils.KEY_OBJ)));
               }

               listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mProjectsList);
               mProjectsListView.setAdapter(listAdapter);
           }
        });

        //  Popola mTagsList con tutte le etichette usate per descrivere i progetti memorizzati in Firestore da usare per la ricerca
        //  TODO: se le cose non funzionano si potrebbe dover spostare il QueryTextListener all'interno dell'if qui dentro per poter utilizzare i dati di mTagsList
        firestoreUtils.getFirestoreInstance().collection(FirestoreUtils.KEY_PROJECTS).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot snapshot : task.getResult()) {
                    mTagsList.addAll((List<String>) snapshot.getData().get(FirestoreUtils.KEY_TAGS));
                }

                Log.d(TAG, mTagsList.toString());
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

        //  Listener che rileva un click sugli item della ListView
        //  usata per visualizzare il progetto selezionato
        mProjectsListView.setOnItemClickListener((parent, view, position, id) -> {
            String projectTitle = mProjectsListView.getItemAtPosition(position).toString();

            Log.d(TAG, mProjects.get(position).getTitolo());

            Intent projectIntent = new Intent(this, ProjectActivity.class);
            projectIntent.putExtra(FirestoreUtils.KEY_TITLE, projectTitle);
            startActivity(projectIntent);
        });
    }
}