package com.example.teamup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teamup.utilities.FirestoreUtils;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class DiscoverActivity extends AppCompatActivity {

    private FirestoreUtils firestoreUtils;

    private ListView mProjectsListView;
    private List<String> mProjectsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);

        firestoreUtils = new FirestoreUtils(FirebaseFirestore.getInstance());

        mProjectsListView = findViewById(R.id.projects_listView);
    }

    @Override
    protected void onStart() {
        super.onStart();

        setTitle("Discover");

        Query query = firestoreUtils.getFirestoreInstance().collection("projects");
        query.get().addOnCompleteListener(task -> {
           if (task.isSuccessful()) {
               mProjectsList = new ArrayList<String>();
               for (QueryDocumentSnapshot snapshot : task.getResult())
                   mProjectsList.add(snapshot.getData().get("title").toString());

               ArrayAdapter<String> listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mProjectsList);
               mProjectsListView.setAdapter(listAdapter);
           }
        });

        mProjectsListView.setOnItemClickListener((parent, view, position, id) -> {
            String projectTitle = mProjectsListView.getItemAtPosition(position).toString();
            Intent projectIntent = new Intent(this, ProjectActivity.class);
            projectIntent.putExtra("title", projectTitle);
            startActivity(projectIntent);
        });
    }
}