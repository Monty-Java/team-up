package com.example.teamup.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.teamup.R;
import com.example.teamup.utilities.FirebaseAuthUtils;
import com.example.teamup.utilities.FirestoreUtils;
import com.example.teamup.utilities.NotificationType;
import com.example.teamup.utilities.Progetto;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ProjectActivity extends AppCompatActivity {
    private static final String TAG = ProjectActivity.class.getSimpleName();
    private static final String KEY_ID = "id";

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

        Intent intent = getIntent();
        readProjectData(intent.getStringExtra(FirestoreUtils.KEY_TITLE));
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
                Intent homeIntent = new Intent(this, MainActivity.class);
                startActivity(homeIntent);
                break;
            case R.id.action_details:
                onDetailsClick();
                break;
            case R.id.action_sponsor:
                if (firebaseAuthUtils.getCurrentUser().getDisplayName().equals(progetto.getLeader())) {
                    Intent sponsorIntent = new Intent(this, SponsorProjectActivity.class);
                    sponsorIntent.putExtra("project", progetto.getTitolo());
                    startActivity(sponsorIntent);
                } else {
                    Toast.makeText(this, "Only the Leader can sponsor the project.", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.action_leave:
                leaveProject();
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
        Button closeButton = detailsDialog.findViewById(R.id.closeDialogButton);
        Button addTagButton = detailsDialog.findViewById(R.id.addTagButton);

        projectId.setText("Project ID: " + progetto.getId());
        ArrayAdapter<String> tagsAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                progetto.getEtichette());
        projectTags.setAdapter(tagsAdapter);

        if (firebaseAuthUtils.getCurrentUser().getDisplayName().equals(progetto.getLeader())) {
            projectTags.setOnItemClickListener((parent, view, position, id) -> {
                        String tag = projectTags.getItemAtPosition(position).toString();
                        editProjectTags(tag);
                        tagsAdapter.notifyDataSetChanged();
            });

            addTagButton.setOnClickListener(v -> addNewTag());
        } else {
            addTagButton.setEnabled(false);
        }

        closeButton.setOnClickListener(v -> detailsDialog.dismiss());
        detailsDialog.show();
    }

    public void onFabProjectClick(View view) {
        Log.d(TAG, "onFabProjectClick");

        if (Objects.equals(firebaseAuthUtils.getCurrentUser().getDisplayName(), progetto.getLeader())) {
            addNewObjective();
        } else if (!progetto.getTeammates().contains(firebaseAuthUtils.getCurrentUser().getDisplayName())) {
            sendTeammateRequest();
        }
    }

    private void addNewObjective() {
        Dialog addObjectiveDialog = new Dialog(this);
        addObjectiveDialog.setContentView(R.layout.add_project_objective_dialog);
        EditText addObjectiveEditText = addObjectiveDialog.findViewById(R.id.add_objective_editText);
        Button addObjectivePositiveButton = addObjectiveDialog.findViewById(R.id.add_objective_positiveButton);
        Button addObjectiveNegativeButton = addObjectiveDialog.findViewById(R.id.add_objective_negativeButton);
        addObjectiveNegativeButton.setOnClickListener(v -> addObjectiveDialog.dismiss());

        addObjectivePositiveButton.setOnClickListener(v -> {
            if (!addObjectiveEditText.getText().toString().equals("")) {
                progetto.addObiettivoDaRaggiungere(addObjectiveEditText.getText().toString());
                firestoreUtils.updateProjectData(progetto.getId(), FirestoreUtils.KEY_OBJ, progetto.getObiettivi());
                addObjectiveDialog.dismiss();
            }
        });

        addObjectiveDialog.show();
    }

    private void sendTeammateRequest() {
        AlertDialog.Builder teammateRequestDialogBuilder = new AlertDialog.Builder(this);
        teammateRequestDialogBuilder.setTitle("Become a Teammate!");
        TextView requestTextView = new TextView(this);
        requestTextView.setText("Would you like to send a request to become a teammate for this project?");
        teammateRequestDialogBuilder.setView(requestTextView);
        teammateRequestDialogBuilder.setPositiveButton("OK", (dialog, which) -> {

            firestoreUtils.storeNotification(progetto.getTitolo(), progetto.getLeader(), firebaseAuthUtils.getCurrentUser().getDisplayName(), firebaseAuthUtils.getCurrentUser().getUid(), NotificationType.TEAMMATE_REQUEST);
        });
        teammateRequestDialogBuilder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.cancel();
            dialog.dismiss();
        });
        AlertDialog teammateRequestDialog = teammateRequestDialogBuilder.create();
        teammateRequestDialog.show();
    }

    private void editProjectTags(String tagToEdit) {
        Dialog editTagsDialog = new Dialog(this);
        editTagsDialog.setContentView(R.layout.edit_project_tag_dialog);

        EditText editTagEditText = editTagsDialog.findViewById(R.id.editTagDialog_editText);
        editTagEditText.setHint(tagToEdit);

        Button editTagRemoveButton = editTagsDialog.findViewById(R.id.editTagDialog_removeButton);
        Button editTagEditButton = editTagsDialog.findViewById(R.id.editTagDialog_editButton);
        Button editTagCancelButton = editTagsDialog.findViewById(R.id.editTagDialog_cancelButton);
        editTagCancelButton.setOnClickListener(v -> editTagsDialog.dismiss());

        //  Rimuove l'etichetta selezionata dal progetto corrente
        editTagRemoveButton.setOnClickListener(v -> {
            progetto.getEtichette().remove(tagToEdit);
            firestoreUtils.updateProjectData(progetto.getId(), FirestoreUtils.KEY_TAGS, progetto.getEtichette());
            editTagsDialog.dismiss();
        });

        //  Sostituisce l'etichetta nuova con quella attuale e aggiorna Firestore
        editTagEditButton.setOnClickListener(v -> {
            if (!editTagEditText.getText().toString().equals(tagToEdit)) {
                progetto.getEtichette().remove(tagToEdit);
                progetto.getEtichette().add(editTagEditText.getText().toString());
                firestoreUtils.updateProjectData(progetto.getId(), FirestoreUtils.KEY_TAGS, progetto.getEtichette());
                editTagsDialog.dismiss();
            }
        });

        editTagsDialog.show();
    }

    private void addNewTag() {
        Dialog addTagDialog = new Dialog(this);
        addTagDialog.setContentView(R.layout.add_project_tag_dialog);

        EditText addTagEditText = addTagDialog.findViewById(R.id.add_tag_editText);
        Button addTagPositiveButton = addTagDialog.findViewById(R.id.add_tag_positiveButton);
        Button addTagNegativeButton = addTagDialog.findViewById(R.id.add_tag_negativeButton);
        addTagNegativeButton.setOnClickListener(v -> addTagDialog.dismiss());

        addTagPositiveButton.setOnClickListener(v -> {
            if (!addTagEditText.getText().toString().equals("")) {
                progetto.getEtichette().add(addTagEditText.getText().toString());
                firestoreUtils.updateProjectData(progetto.getId(), FirestoreUtils.KEY_TAGS, progetto.getEtichette());
                addTagDialog.dismiss();
            } else {
                //  TODO: quella roba di errore nell'edit text che non so fare
            }
        });
        addTagDialog.show();
    }

    private void editProjectDescription() {
        Dialog editDescriptionDialog = new Dialog(this);
        editDescriptionDialog.setContentView(R.layout.edit_project_description_dialog);
        EditText editDescriptionEditText = editDescriptionDialog.findViewById(R.id.edit_description_editText);
        Button editDescriptionPositiveButton = editDescriptionDialog.findViewById(R.id.edit_description_positiveButton);
        Button editDescriptionNegativeButton = editDescriptionDialog.findViewById(R.id.edit_description_negativeButton);
        editDescriptionNegativeButton.setOnClickListener(v -> editDescriptionDialog.dismiss());

        editDescriptionPositiveButton.setOnClickListener(v -> {
            if (!editDescriptionEditText.getText().toString().equals("")) {
                progetto.setDescrizione(editDescriptionEditText.getText().toString());
                firestoreUtils.updateProjectData(progetto.getId(), FirestoreUtils.KEY_DESC, progetto.getDescrizione());
                editDescriptionDialog.dismiss();
            }
        });
        editDescriptionDialog.show();
    }

    private void viewTeammateProfile(String teammate) {
        if (!teammate.equals(firebaseAuthUtils.getCurrentUser().getDisplayName())) {
            Intent teammateProfileIntent = new Intent(this, TeammateProfileActivity.class);
            teammateProfileIntent.putExtra("teammate", teammate);
            startActivity(teammateProfileIntent);
        } else {
            //  TODO: possibilmente aprire il fragment del proprio profilo personale?
        }
    }

    /**
     * Ottiene da Firestore i dati relativi al progetto indicato da title
     * e istanzia un oggetto Progetto con le informazioni lette
     *
     * @param title
     */
    public void readProjectData(String title) {
        Map<String, Object> data = new HashMap<>();
        Query query = firestoreUtils.getFirestoreInstance()
                .collection(FirestoreUtils.KEY_PROJECTS)
                .whereEqualTo(FirestoreUtils.KEY_TITLE, title);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                data.putAll(task.getResult().getDocuments().get(0).getData());
                data.put(KEY_ID, task.getResult().getDocuments().get(0).getId());
                data.putIfAbsent(FirestoreUtils.KEY_SPONSORED, false);

                List<String> team = new ArrayList<>();
                if (data.get(FirestoreUtils.KEY_TEAMMATES) != null)
                    team.addAll((List<String>) data.get(FirestoreUtils.KEY_TEAMMATES));

                progetto = new Progetto(
                        data.get(KEY_ID).toString(),
                        Objects.requireNonNull(data.get(FirestoreUtils.KEY_LEADER)).toString(),
                        data.get(FirestoreUtils.KEY_TITLE).toString(),
                        data.get(FirestoreUtils.KEY_DESC).toString(),
                        (List<String>) data.get(FirestoreUtils.KEY_TAGS),
                        team,
                        (Map<String, Boolean>) data.get(FirestoreUtils.KEY_OBJ),
                        (boolean) data.get(FirestoreUtils.KEY_SPONSORED));

                displayProject(progetto);
            } else
                Log.e(TAG, "Error reading data");
        });
    }

    /**
     * Visualizza le informazioni relative al progetto
     *
     * @param project
     */
    private void displayProject(Progetto project) {
        Log.d(TAG, "displayProject");

        this.setTitle(project.getTitolo());

        if (!project.getLeader().equals(firebaseAuthUtils.getCurrentUser().getDisplayName()))
            mFab.hide();

        //  Calcola il progresso totale del progetto prima di rimuovere la lista di obiettivi completi
        updateProgress(project);

        //  Rimuove gli obiettivi completi dall'istanza di Progetto (gli obiettivi rimangono memorizzati in Firestore)
        for (Iterator<Map.Entry<String, Boolean>> entries = project.getObiettivi().entrySet().iterator(); entries.hasNext();) {
            if (entries.next().getValue()) entries.remove();
        }

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

        mObjectivesAdapter.notifyDataSetChanged();
        mTeammatesAdapter.notifyDataSetChanged();

        if (firebaseAuthUtils.getCurrentUser() != null) {
            //  Un long click permette di modificare la descrizione del progetto corrente
            mDescriptionTextView.setOnLongClickListener(view -> {
                editProjectDescription();
                return false;
            });

            //  Permette all'utente di modificare il valore degli objectives
            //  Possono assumere false o true, true indica che sono completi.
            mObjectivesList.setOnItemClickListener((parent, view, position, id) -> {
                if (progetto.getLeader().equals(firebaseAuthUtils.getCurrentUser().getDisplayName())) {
                    String objective = mObjectivesList.getItemAtPosition(position).toString();

                    if (!progetto.getObiettivi().get(objective)) {
                        progetto.setObiettivoRaggiunto(objective);
                        firestoreUtils.updateProjectData(progetto.getId(), FirestoreUtils.KEY_OBJ, progetto.getObiettivi());
                    }
                }
            });
            mObjectivesAdapter.notifyDataSetChanged();

            mTeammatesList.setOnItemClickListener(((parent, view, position, id) -> {
                String teammate = mTeammatesList.getItemAtPosition(position).toString();
                viewTeammateProfile(teammate);
            }));
            mTeammatesAdapter.notifyDataSetChanged();
        }
    }

    public void updateProgress(Progetto project) {
        Log.d(TAG, "updateProgress");

        mProgressTextView.setText("Progress: " + (int) ((project.obiettiviCompleti() / project.numeroObiettivi()) * 100) + '%');
    }

    private void leaveProject() {
        if (!firebaseAuthUtils.getCurrentUser().getDisplayName().equals(progetto.getLeader()) &&
                progetto.getTeammates().contains(firebaseAuthUtils.getCurrentUser().getDisplayName())) {
            AlertDialog.Builder leaveProjectDialogBuilder = new AlertDialog.Builder(this);
            leaveProjectDialogBuilder.setTitle("Leave Project");
            leaveProjectDialogBuilder.setMessage("Are you sure you want to leave the team on project " +
                    progetto.getTitolo() + "? This action cannot be undone (although you can always request to rejoin later).");

            leaveProjectDialogBuilder.setPositiveButton("OK", ((dialog, which) -> {
                progetto.removeTeammate(firebaseAuthUtils.getCurrentUser().getDisplayName());
                firestoreUtils.updateProjectData(progetto.getId(), FirestoreUtils.KEY_TEAMMATES, progetto.getTeammates());
                dialog.dismiss();

                Intent leaveProjectIntent = new Intent(this, MainActivity.class);
                startActivity(leaveProjectIntent);
                finish();
            }));

            leaveProjectDialogBuilder.setNegativeButton("Cancel", (((dialog, which) -> {
                dialog.dismiss();
            })));

            AlertDialog leaveTeamDialog = leaveProjectDialogBuilder.create();
            leaveTeamDialog.show();
        }
    }
}