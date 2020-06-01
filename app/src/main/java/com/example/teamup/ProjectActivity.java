package com.example.teamup;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.teamup.utilities.FirebaseAuthUtils;
import com.example.teamup.utilities.FirestoreUtils;
import com.example.teamup.utilities.Progetto;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private TextView mProgressTextView;
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
        //mProgressBar = findViewById(R.id.progressBar);
        mProgressTextView = findViewById(R.id.progress_textView);

        firestoreUtils = new FirestoreUtils(FirebaseFirestore.getInstance());
        firebaseAuthUtils = new FirebaseAuthUtils(FirebaseAuth.getInstance(), firestoreUtils.getFirestoreInstance(), this);

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

                projectTags.setOnItemClickListener((parent, view, position, id) -> {
                    //  TODO: refactoring || optimizing
                    String tag = projectTags.getItemAtPosition(position).toString();
                    AlertDialog.Builder editTagDialogBuilder = new AlertDialog.Builder(this);
                    editTagDialogBuilder.setTitle("Edit Tag");

                    EditText tagEditText = new EditText(this);
                    tagEditText.setText(tag);
                    tagEditText.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    editTagDialogBuilder.setView(tagEditText);
                    editTagDialogBuilder.setPositiveButton("OK", (dialog, which) -> {
                       if (tagEditText.getText().toString().equals("")) {
                           progetto.removeEtichetta(tag);
                       } else {
                           progetto.addEtichetta(tagEditText.getText().toString());
                       }
                       firestoreUtils.updateProjectData(progetto.getId(), "tags", progetto.getEtichette());
                       adapter.notifyDataSetChanged();
                    });

                    AlertDialog editTagDialog = editTagDialogBuilder.create();
                    editTagDialog.show();
                });

                Button closeButton = detailsDialog.findViewById(R.id.closeDialogButton);
                projectId.setText("Project ID: " + progetto.getId());
                closeButton.setOnClickListener(v -> {
                    detailsDialog.hide();
                });
                detailsDialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

    public void onFabProjectClick(View view) {
        //  TODO: sistemare il layout
        AlertDialog.Builder addObjectiveBuilder = new AlertDialog.Builder(this);
        addObjectiveBuilder.setTitle("New Objective");

        EditText objectiveEditText = new EditText(this);
        objectiveEditText.setHint("Objective");
        objectiveEditText.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        addObjectiveBuilder.setView(objectiveEditText);
        addObjectiveBuilder.setPositiveButton("OK", (dialog, which) -> {
            if (!objectiveEditText.getText().toString().equals("")) {
                progetto.addObiettivoDaRaggiungere(objectiveEditText.getText().toString());
                firestoreUtils.updateProjectData(progetto.getId(), "objectives", progetto.getObiettivi());
                mObjectivesAdapter.notifyDataSetChanged();
            }
        });
        addObjectiveBuilder.setNegativeButton("Cancel", null);
        AlertDialog addObjectiveDialog = addObjectiveBuilder.create();
        addObjectiveDialog.show();
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

        //  Un long click permette di modificare la descrizione del progetto corrente
        mDescriptionTextView.setOnLongClickListener(view -> {
            //  TODO: sistemare layout
            AlertDialog.Builder editDescriptionDialogBuilder = new AlertDialog.Builder(this);
            editDescriptionDialogBuilder.setTitle("Edit Description");
            EditText descriptionEditText = new EditText(this);
            descriptionEditText.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            editDescriptionDialogBuilder.setView(descriptionEditText);
            editDescriptionDialogBuilder.setPositiveButton("OK", (dialog, which) -> {
                if (descriptionEditText.getText() != null) {
                    project.setDescrizione(descriptionEditText.getText().toString());
                    firestoreUtils.updateProjectData(project.getId(), "description", project.getDescrizione());
                }
            });
            AlertDialog editDescriptionDialog = editDescriptionDialogBuilder.create();
            editDescriptionDialog.show();
            return false;
        });

        //  Permette all'utente di modificare il valore degli objectives
        //  Possono assumere false o true, true indica che sono completi.
        mObjectivesList.setOnItemClickListener((parent, view, position, id) -> {
            if (progetto.getLeader().equals(firebaseAuthUtils.getCurrentUser().getDisplayName())) {
                String objective = mObjectivesList.getItemAtPosition(position).toString();
                if (!progetto.getObiettivi().get(objective)) {
                    progetto.setObiettivoRaggiunto(objective);
                    firestoreUtils.updateProjectData(progetto.getId(), "objectives", progetto.getObiettivi());

                    //  TODO: aggiornare layout con un feedback visuale corrispondente agli obiettivi completi

                    //view = (View) mObjectivesList.getItemAtPosition(position);
                    //TextView text = (TextView) view;
                    //text.setPaintFlags(text.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                }
            }
        });

        updateProgress(project);
    }

    public void updateProgress(Progetto project) {
        /*
        //  Update progress bar
        mProgressBar.setMax((int) project.numeroObiettivi());

        Log.d(TAG, "PERCENT: " + ((int) ((project.obiettiviCompleti() / mProgressBar.getMax()) * 100)));
        mProgressBar.setProgress((int) ((project.obiettiviCompleti() / mProgressBar.getMax()) * 100));

         */
        mProgressTextView.setText("Progress: " + (int) ((project.obiettiviCompleti() / project.numeroObiettivi()) * 100) + '%');
    }
}