package com.example.teamup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teamup.utilities.FirestoreUtils;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class DiscoverActivity extends AppCompatActivity {

    private FirestoreUtils firestoreUtils;

    private SearchView mSearchView;
    private ListView mProjectsListView;
    private List<String> mProjectsList;
    private ArrayAdapter<String> listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);

        firestoreUtils = new FirestoreUtils(FirebaseFirestore.getInstance());

        mSearchView = (SearchView) findViewById(R.id.searchView);
        mProjectsListView = (ListView) findViewById(R.id.projects_listView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setTitle("Discover");

        Query query = firestoreUtils.getFirestoreInstance().collection(FirestoreUtils.KEY_PROJECTS);
        query.get().addOnCompleteListener(task -> {
           if (task.isSuccessful()) {
               mProjectsList = new ArrayList<>();
               for (QueryDocumentSnapshot snapshot : task.getResult())
                   mProjectsList.add(snapshot.getData().get(FirestoreUtils.KEY_TITLE).toString());

               listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mProjectsList);
               mProjectsListView.setAdapter(listAdapter);
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
            Intent projectIntent = new Intent(this, ProjectActivity.class);
            projectIntent.putExtra(FirestoreUtils.KEY_TITLE, projectTitle);
            startActivity(projectIntent);
        });
    }
}