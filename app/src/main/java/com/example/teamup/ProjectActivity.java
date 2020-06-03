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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.teamup.utilities.FirebaseAuthUtils;
import com.example.teamup.utilities.FirestoreUtils;
import com.example.teamup.utilities.Progetto;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

//  TODO: aggiungere un pulsante o item di menu Home per tornare alla MainActivity

public class ProjectActivity extends AppCompatActivity {

    private static final String TAG = ProjectActivity.class.getSimpleName();

    private FirebaseAuthUtils firebaseAuthUtils;
    private FirestoreUtils firestoreUtils;
    private Progetto progetto;

    //  UI
    private ListView mObjectivesList;
    private ListView mTeammatesList;
    private TextView mDescriptionTextView;
    private TextView mProgressTextView;
    private FloatingActionButton mFab;

    private ArrayAdapter<String> mObjectivesAdapter;
    private ArrayAdapter<String> mTeammatesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);

        mObjectivesList = findViewById(R.id.objectives_listView);
        mTeammatesList = findViewById(R.id.team_listView);
        mDescriptionTextView = findViewById(R.id.description_textView);
        mProgressTextView = findViewById(R.id.progress_textView);
        mFab = findViewById(R.id.floatingActionButton);

        firestoreUtils = new FirestoreUtils(FirebaseFirestore.getInstance());
        firebaseAuthUtils = new FirebaseAuthUtils(FirebaseAuth.getInstance(), firestoreUtils.getFirestoreInstance(), this);

        if (firebaseAuthUtils.getCurrentUser() == null) mFab.hide();

        //  TODO: refactor stringa in una costante
        Intent intent = getIntent();
        progetto = readProjectData(intent.getStringExtra("title"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_home:
                //  Se l'utente è autenticato ritorna alla MainActivity, altrimenti ritorna ad AuthActivity
                if (firebaseAuthUtils.getCurrentUser() != null) {
                    Intent homeIntent = new Intent(this, MainActivity.class);
                    startActivity(homeIntent);
                } else {
                    Intent authIntent = new Intent(this, AuthActivity.class);
                    startActivity(authIntent);
                }
                this.finish();
                break;
            case R.id.action_details:
                onDetailsClick();
                break;

            default:
                Log.w(TAG, "onOptionsItemSelected: item non riconosciuto");
        }
        return super.onOptionsItemSelected(item);
    }

    private void onDetailsClick() {
        Log.d(TAG, "onDetailsClick");

        final Dialog detailsDialog = new Dialog(this);
        detailsDialog.setContentView(R.layout.project_details_dialog);
        TextView projectId = detailsDialog.findViewById(R.id.projectId_textView);
        ListView projectTags = detailsDialog.findViewById(R.id.tagsListView);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                progetto.getEtichette());
        projectTags.setAdapter(adapter);

        if (firebaseAuthUtils.getCurrentUser() != null && firebaseAuthUtils.getCurrentUser().getDisplayName().equals(progetto.getLeader())) {
            projectTags.setOnItemClickListener((parent, view, position, id) -> {
                        //  TODO: refactoring || optimizing
                        String tag = projectTags.getItemAtPosition(position).toString();
                        AlertDialog.Builder editTagDialogBuilder = new AlertDialog.Builder(this);
                        editTagDialogBuilder.setTitle("Edit Tag");

                        EditText tagEditText = new EditText(this);
                        tagEditText.setText(tag);
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);
                        tagEditText.setLayoutParams(layoutParams);

                        editTagDialogBuilder.setView(tagEditText);
                        editTagDialogBuilder.setPositiveButton("OK", (dialog, which) -> {
                            if (tagEditText.getText().toString().equals("")) {
                                progetto.removeEtichetta(tag);
                            } else if (!progetto.getEtichette().contains(tagEditText.getText().toString())) {
                                progetto.addEtichetta(tagEditText.getText().toString());
                            } else {
                                Log.w(TAG, "onDetailsClick: Non è stato possibile modificare l'etichetta");
                            }
                            firestoreUtils.updateProjectData(progetto.getId(), "tags", progetto.getEtichette());
                            adapter.notifyDataSetChanged();
                        });
                    AlertDialog editTagDialog = editTagDialogBuilder.create();
                    editTagDialog.show();
            });
        }

        Button closeButton = detailsDialog.findViewById(R.id.closeDialogButton);
        projectId.setText("Project ID: " + progetto.getId());
        closeButton.setOnClickListener(v -> {
            detailsDialog.hide();
        });
        detailsDialog.show();
    }

    public void onFabProjectClick(View view) {
        Log.d(TAG, "onFabProjectClick");

        //  TODO: sistemare il layout
        //  TODO: raffinare questo controllo

        if (Objects.equals(firebaseAuthUtils.getCurrentUser().getDisplayName(), progetto.getLeader()) ||
            progetto.getTeammates().contains(firebaseAuthUtils.getCurrentUser().getDisplayName())) {
            AlertDialog.Builder addObjectiveBuilder = new AlertDialog.Builder(this);
            addObjectiveBuilder.setTitle("New Objective");

            EditText objectiveEditText = new EditText(this);
            objectiveEditText.setHint("Objective");
            objectiveEditText.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            addObjectiveBuilder.setView(objectiveEditText);
            addObjectiveBuilder.setPositiveButton("OK", (dialog, which) -> {

                String objectiveText = objectiveEditText.getText().toString();
                if (!objectiveText.equals("") || !progetto.getObiettivi().containsKey(objectiveText)) {
                    progetto.addObiettivoDaRaggiungere(objectiveText);
                    firestoreUtils.updateProjectData(
                            progetto.getId(),
                            "objectives",
                            progetto.getObiettivi());
                    mObjectivesAdapter.notifyDataSetChanged();
                }
            });

            addObjectiveBuilder.setNegativeButton("Cancel", (dialog, which) -> {
                dialog.cancel();
                dialog.dismiss();
            });

            AlertDialog addObjectiveDialog = addObjectiveBuilder.create();
            addObjectiveDialog.show();
        } else {
            AlertDialog.Builder teammateRequestDialogBuilder = new AlertDialog.Builder(this);
            teammateRequestDialogBuilder.setTitle("Become a Teammate!");
            TextView requestTextView = new TextView(this);
            requestTextView.setText("Would you like to send a request to become a teammate for this project?");
            teammateRequestDialogBuilder.setView(requestTextView);
            teammateRequestDialogBuilder.setPositiveButton("OK", (dialog, which) -> {
                //  TODO: se l'utente non è Teammate, usare il Fab per inviare una richiesta di diventare Teammate
            });
            teammateRequestDialogBuilder.setNegativeButton("Cancel", (dialog, which) -> {
                dialog.cancel();
                dialog.dismiss();
            });
            AlertDialog teammateRequestDialog = teammateRequestDialogBuilder.create();
            teammateRequestDialog.show();
        }
    }

    /**
     * Ottiene da Firestore i dati relativi al progetto indicato da title
     * e istanzia un oggetto Progetto con le informazioni lette
     *
     * @param title
     */
    public Progetto readProjectData(String title) {
        Log.d(TAG, "readProjectData: title: "+ title);
        Map<String, Object> data = new HashMap<>();
        Query query = firestoreUtils.getFirestoreInstance()
                .collection("projects")
                .whereEqualTo("title", title);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                data.putAll(task.getResult().getDocuments().get(0).getData());
                data.put("id", task.getResult().getDocuments().get(0).getId());

                progetto = new Progetto(
                        data.get("id").toString(),
                        Objects.requireNonNull(data.get("leader")).toString(),
                        data.get("title").toString(),
                        data.get("description").toString(),
                        (List<String>) data.get("tags"),
                        (Map<String, Boolean>) data.get("objectives"));
                displayProject(progetto);

            } else {
                Log.e(TAG, "ERROR READING DATA...");
            }
        });

        return progetto;
    }

    /**
     * Visualizza le informazioni relative al progetto
     *
     * @param project
     */
    private void displayProject(Progetto project) {
        Log.d(TAG, "displayProject");

        this.setTitle(project.getTitolo());

        //  Imposta le ListView per visualizzare gli obiettivi e i teammates
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

        //  TODO: verificare se l'utente ha effettuato l'accesso a TeamUp - impedire qualsiasi modifica al progetto
        //  TODO: verificare se l'utente è Leader
        //  TODO: verificare se l'utente è Teammate - limitare le modifiche al progetto
        //  TODO: se l'utente è autenticato ma non è nè Leader nè Teammate visualizzare un pulsante per fare la richiesta di diventare Teammate

        //  TODO: raffinare questo controllo
        if (firebaseAuthUtils.getCurrentUser() != null && firebaseAuthUtils.getCurrentUser().getDisplayName().equals(progetto.getLeader())) {

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
                    }
                }
            });
        }

        updateProgress(project);
    }

    public void updateProgress(Progetto project) {
        Log.d(TAG, "updateProgress");

        mProgressTextView.setText("Progress: " + (int) ((project.obiettiviCompleti() / project.numeroObiettivi()) * 100) + '%');
    }
}