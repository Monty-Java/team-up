package com.example.teamup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.teamup.utilities.FirebaseAuthUtils;
import com.example.teamup.utilities.FirestoreUtils;
import com.example.teamup.utilities.Progetto;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

//  TODO IF-6: gestione del progetto
public class ProjectActivity extends AppCompatActivity {
    private static final String TAG = ProjectActivity.class.getSimpleName();

    private FirebaseAuthUtils firebaseAuthUtils;
    private FirestoreUtils firestoreUtils;
    private Progetto progetto;

    //  UI
    private TextView mObjectivesTextView;
    private TextView mTeammatesTextView;
    private ListView mObjectivesList;
    private ListView mTeammatesList;
    private TextView mDescriptionTextView;
    private ProgressBar mProgressBar;

    //private ArrayAdapter<Map.Entry<String, Boolean>> mObjectivesAdapter;
    private ArrayAdapter<String> mObjectivesAdapter;
    private ArrayAdapter<String> mTeammatesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);

        mObjectivesTextView = findViewById(R.id.objectives_textView);
        mTeammatesTextView = findViewById(R.id.teamTextView);
        mObjectivesList = findViewById(R.id.objectives_listView);
        mTeammatesList = findViewById(R.id.team_listView);
        mDescriptionTextView = findViewById(R.id.description_textView);
        mProgressBar = findViewById(R.id.progressBar);

        firestoreUtils = new FirestoreUtils(FirebaseFirestore.getInstance());

        //  TODO: refactor stringa in una costante
        Intent intent = getIntent();
        readProjectData(intent.getStringExtra("title"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_details:
                final Dialog detailsDialog = new Dialog(this);
                detailsDialog.setContentView(R.layout.project_details_dialog);
                TextView dialogTitle = detailsDialog.findViewById(R.id.dialog_title);
                TextView projectId = detailsDialog.findViewById(R.id.projectId_textView);
                TextView tagsTitle = detailsDialog.findViewById(R.id.tags_title);
                ListView projectTags = detailsDialog.findViewById(R.id.tagsListView);

                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                        progetto.getEtichette());
                projectTags.setAdapter(adapter);

                Button closeButton = detailsDialog.findViewById(R.id.closeDialogButton);
                projectId.setText("Project ID: " + progetto.getId());
                closeButton.setOnClickListener(v -> {
                    detailsDialog.hide();
                });
                detailsDialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

    //  Ottiene da Firestore i dati relativi al progetto indicato da
    //  title e istanzia un oggetto Progetto con le informazioni lette
    public void readProjectData(String title) {
        Map<String, Object> data = new HashMap<>();
        Query query = firestoreUtils.getFirestoreInstance().collection("projects")
                .whereEqualTo("title", title);
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                data.putAll(task.getResult().getDocuments().get(0).getData());
                data.put("id", task.getResult().getDocuments().get(0).getId());

                //  TODO: istanziare e usare qui un oggetto di tipo Progetto
                progetto = new Progetto(data.get("id").toString(),
                        data.get("leader").toString(), data.get("title").toString(),
                        data.get("description").toString(), (List<String>) data.get("tags"),
                        (Map<String, Boolean>) data.get("objectives"));
                displayProject(progetto);

            } else Log.d(TAG, "ERROR READING DATA...");
        });
    }

    //  Visualizza le informazioni relative al progetto
    private void displayProject(Progetto project) {
        this.setTitle(project.getTitolo());

        //  Imposta le ListView per visualizzare gli obiettivi e i teammates
        //List<Map.Entry<String, Boolean>> objectivesList = new ArrayList<>(project.getObiettivi().entrySet());
        List<String> objectivesList = new ArrayList<>(project.getObiettivi().keySet());
        mObjectivesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                objectivesList);
        List<String> team = new ArrayList<>();
        team.add(project.getLeader());
        if (project.hasTeammates())
            team.addAll(project.getTeammates());
        mTeammatesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                team);
        mObjectivesList.setAdapter(mObjectivesAdapter);
        mTeammatesList.setAdapter(mTeammatesAdapter);

        mDescriptionTextView.setText(project.getDescrizione());
    }
}