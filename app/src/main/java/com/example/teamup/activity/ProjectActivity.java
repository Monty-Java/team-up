package com.example.teamup.activity;

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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teamup.R;
import com.example.teamup.utilities.FirebaseAuthUtils;
import com.example.teamup.utilities.FirestoreUtils;
import com.example.teamup.utilities.NotificationType;
import com.example.teamup.utilities.Progetto;
import com.example.teamup.utilities.ProjectListsAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ProjectActivity extends AppCompatActivity {
    private static final String TAG = ProjectActivity.class.getSimpleName();
    private static final String KEY_ID = "id";

    private FirebaseAuthUtils firebaseAuthUtils;
    private FirestoreUtils firestoreUtils;
    private Progetto progetto;

    //  UI
    private RecyclerView mObjectivesList;
    private RecyclerView mTeammatesList;
    private TextView mDescriptionTextView;
    private TextView mProgressTextView;

    private ProjectListsAdapter objectivesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);

        mObjectivesList = findViewById(R.id.objectives_listView);
        mTeammatesList = findViewById(R.id.team_listView);
        mDescriptionTextView = findViewById(R.id.description_textView);
        mProgressTextView = findViewById(R.id.progress_textView);

        firestoreUtils = new FirestoreUtils(FirebaseFirestore.getInstance());
        firebaseAuthUtils = new FirebaseAuthUtils(FirebaseAuth.getInstance(), firestoreUtils.getFirestoreInstance(), this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        readProjectData(intent.getStringExtra(FirestoreUtils.KEY_TITLE));

        mDescriptionTextView.setOnLongClickListener(v -> {
            editProjectDescription();
            return false;
        });
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
            case R.id.action_change_title:
                changeProjectTitle();
                break;
            case R.id.action_details:
                onDetailsClick();
                break;
            case R.id.action_sponsor:
                if (Objects.equals(firebaseAuthUtils.getCurrentUser().getDisplayName(), progetto.getLeader())) {
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
            case R.id.action_delete:
                deleteProject();
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

        String projectIdText = "Project ID: " + progetto.getId();
        projectId.setText(projectIdText);

        ArrayAdapter<String> tagsAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                progetto.getEtichette());
        projectTags.setAdapter(tagsAdapter);

        if (Objects.equals(firebaseAuthUtils.getCurrentUser().getDisplayName(), progetto.getLeader())) {
            projectTags.setOnItemClickListener((parent, view, position, id) -> {
                        String tag = projectTags.getItemAtPosition(position).toString();
                        removeTag(tag, tagsAdapter);
            });

            addTagButton.setOnClickListener(v -> addNewTag(tagsAdapter));
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
            } else addObjectiveEditText.setError("Objective field cannot be empty");
        });

        addObjectiveDialog.show();
    }

    private void sendTeammateRequest() {
        AlertDialog.Builder teammateRequestDialogBuilder = new AlertDialog.Builder(this);
        teammateRequestDialogBuilder.setTitle("Become a Teammate!");
        teammateRequestDialogBuilder.setMessage(R.string.teammte_request_text);

        //  Invia una notifica al Leader del progetto corrente
        teammateRequestDialogBuilder.setPositiveButton(R.string.ok_text, (dialog, which) ->
                firestoreUtils.storeNotification(progetto.getTitolo(),
                        progetto.getLeader(),
                        firebaseAuthUtils.getCurrentUser().getDisplayName(),
                        firebaseAuthUtils.getCurrentUser().getUid(),
                        NotificationType.TEAMMATE_REQUEST));

        teammateRequestDialogBuilder.setNegativeButton(R.string.cancel_text, (dialog, which) -> dialog.dismiss());
        AlertDialog teammateRequestDialog = teammateRequestDialogBuilder.create();
        teammateRequestDialog.show();
    }

    private void removeTag(String tagToRemove, ArrayAdapter<String> adapter) {
        AlertDialog.Builder removeTagDialogBuilder = new AlertDialog.Builder(this);
        removeTagDialogBuilder.setTitle("Remove Tag");
        removeTagDialogBuilder.setMessage("Are you sure you want to remove this tag?");
        removeTagDialogBuilder.setPositiveButton("OK", (dialog, which) -> {
            progetto.removeEtichetta(tagToRemove);
            firestoreUtils.updateProjectData(progetto.getId(), FirestoreUtils.KEY_TAGS, progetto.getEtichette());
            adapter.remove(tagToRemove);
            dialog.dismiss();
        });
        removeTagDialogBuilder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        AlertDialog removeTagDialog = removeTagDialogBuilder.create();
        removeTagDialog.show();
    }

    private void addNewTag(ArrayAdapter<String> adapter) {
        Dialog addTagDialog = new Dialog(this);
        addTagDialog.setContentView(R.layout.add_project_tag_dialog);

        EditText addTagEditText = addTagDialog.findViewById(R.id.add_tag_editText);
        Button addTagPositiveButton = addTagDialog.findViewById(R.id.add_tag_positiveButton);
        Button addTagNegativeButton = addTagDialog.findViewById(R.id.add_tag_negativeButton);

        addTagNegativeButton.setOnClickListener(v -> addTagDialog.dismiss());

        addTagPositiveButton.setOnClickListener(v -> {
            if (!addTagEditText.getText().toString().equals("")) {
                progetto.addEtichetta(addTagEditText.getText().toString());
                firestoreUtils.updateProjectData(progetto.getId(), FirestoreUtils.KEY_TAGS, progetto.getEtichette());
                adapter.notifyDataSetChanged();
                addTagDialog.dismiss();
            } else
                addTagEditText.setError("Tag field cannot be empty");
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
                mDescriptionTextView.setText(progetto.getDescrizione());
                editDescriptionDialog.dismiss();
            } else
                editDescriptionEditText.setError("Project needs a description");
        });
        editDescriptionDialog.show();
    }

    private void changeProjectTitle() {
        if (Objects.equals(firebaseAuthUtils.getCurrentUser().getDisplayName(), progetto.getLeader())) {
            Dialog changeTitleDialog = new Dialog(this);
            changeTitleDialog.setContentView(R.layout.change_project_title_dialog);
            EditText changeTitleEditText = changeTitleDialog.findViewById(R.id.change_title_editText);
            changeTitleEditText.setHint(progetto.getTitolo());
            Button changeTitlePositiveButton = changeTitleDialog.findViewById(R.id.change_title_positiveButton);
            Button changeTitleNegativeButton = changeTitleDialog.findViewById(R.id.change_title_negativeButton);
            changeTitleNegativeButton.setOnClickListener(v -> changeTitleDialog.dismiss());

            changeTitlePositiveButton.setOnClickListener(v -> {
                if (!changeTitleEditText.getText().toString().equals("") && !changeTitleEditText.getText().toString().equals(progetto.getTitolo())) {
                    AlertDialog.Builder confirmTitleChangeBuilder = new AlertDialog.Builder(this);
                    confirmTitleChangeBuilder.setTitle("Confirm Title Change");
                    confirmTitleChangeBuilder.setMessage("Change title from " + progetto.getTitolo() + " to " + changeTitleEditText.getText().toString() + "?");
                    confirmTitleChangeBuilder.setPositiveButton(R.string.ok_text, (dialog, which) -> {
                        progetto.setTitolo(changeTitleEditText.getText().toString());
                        firestoreUtils.updateProjectData(progetto.getId(), FirestoreUtils.KEY_TITLE, progetto.getTitolo());
                        setTitle(changeTitleEditText.getText().toString());
                        dialog.dismiss();
                        changeTitleDialog.dismiss();
                    });
                    confirmTitleChangeBuilder.setNegativeButton(R.string.cancel_text, (dialog, which) -> dialog.dismiss());
                    AlertDialog confirmTitleChangeDialog = confirmTitleChangeBuilder.create();
                    confirmTitleChangeDialog.show();
                } else changeTitleEditText.setError("Title field is empty or equal to previous project title");
            });
            changeTitleDialog.show();
        } else Toast.makeText(this, "Only the leader can change the project title", Toast.LENGTH_LONG).show();
    }

    private void viewTeammateProfile(String teammate) {
        if (!teammate.equals(firebaseAuthUtils.getCurrentUser().getDisplayName())) {
            Intent teammateProfileIntent = new Intent(this, TeammateProfileActivity.class);
            teammateProfileIntent.putExtra("teammate", teammate);
            startActivity(teammateProfileIntent);
        }
    }

    /**
     * Ottiene da Firestore i dati relativi al progetto indicato da title
     * e istanzia un oggetto Progetto con le informazioni lette
     *
     * @param title: titolo del progetto
     */
    public void readProjectData(String title) {
        Map<String, Object> data = new HashMap<>();

        Query query = firestoreUtils.getFirestoreInstance()
                .collection(FirestoreUtils.KEY_PROJECTS)
                .whereEqualTo(FirestoreUtils.KEY_TITLE, title);
        query.addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (queryDocumentSnapshots != null) {
                List<DocumentChange> documentChanges = queryDocumentSnapshots.getDocumentChanges();
                data.putAll(documentChanges.get(0).getDocument().getData());
                data.put(KEY_ID, documentChanges.get(0).getDocument().getId());
                data.putIfAbsent(FirestoreUtils.KEY_SPONSORED, false);
                data.putIfAbsent(FirestoreUtils.KEY_TEAMMATES, new ArrayList<String>());

                //  I cast sono giustificati in quanto gli elementi, se presenti nel database sono sempre dei tipi specificati
                @SuppressWarnings(value = "unchecked")
                List<String> team = new ArrayList<>((List<String>) Objects.requireNonNull(data.get(FirestoreUtils.KEY_TEAMMATES)));
                @SuppressWarnings(value = "unchecked")
                List<String> tags = new ArrayList<>((List<String>) Objects.requireNonNull(data.get(FirestoreUtils.KEY_TAGS)));
                @SuppressWarnings(value = "unchecked")
                Map<String, Boolean> objective = new HashMap<>((Map<String, Boolean>) Objects.requireNonNull(data.get(FirestoreUtils.KEY_OBJ)));

                progetto = new Progetto(
                        (String) data.get(KEY_ID),
                        (String) data.get(FirestoreUtils.KEY_LEADER),
                        (String) data.get(FirestoreUtils.KEY_TITLE),
                        (String) data.get(FirestoreUtils.KEY_DESC),
                        tags,
                        team,
                        objective,
                        (boolean) data.get(FirestoreUtils.KEY_SPONSORED));

                displayProject(progetto);
            }
        });
    }

    /**
     * Visualizza le informazioni relative al progetto
     *
     * @param project: progetto da visualizzare
     */
    private void displayProject(Progetto project) {
        Log.d(TAG, "displayProject");

        this.setTitle(project.getTitolo());

        //  Calcola il progresso totale del progetto prima di rimuovere la lista di obiettivi completi
        updateProgress(project);

        //  Rimuove gli obiettivi completi dall'istanza di Progetto (gli obiettivi rimangono memorizzati in Firestore)
        Set<Map.Entry<String, Boolean>> incompleteObjectives = project.getObiettivi().entrySet();
        for (Iterator<Map.Entry<String, Boolean>> entries = incompleteObjectives.iterator(); entries.hasNext();) {
            if (entries.next().getValue()) entries.remove();
        }

        //  Imposta la ListView per visualizzare gli obiettivi

        LinearLayoutManager objectivesLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        mObjectivesList.setLayoutManager(objectivesLayoutManager);

        List<String> objectives = new ArrayList<>(project.getObiettivi().keySet());

        //  Permette al leader di modificare il valore degli objectives
        //  Possono assumere false o true, true indica che sono completi.
        View.OnClickListener onObjectiveClick = v -> {
            if (project.getLeader().equals(firebaseAuthUtils.getCurrentUser().getDisplayName())) {
                String objective = ((TextView)v).getText().toString();

                Log.d(TAG, project.getObiettivi().toString());

                if (!Objects.requireNonNull(project.getObiettivi().get(objective))) {
                    project.setObiettivoRaggiunto(objective);
                    firestoreUtils.updateProjectData(project.getId(), FirestoreUtils.KEY_OBJ, project.getObiettivi());
                    mObjectivesList.setAdapter(objectivesAdapter);
                }
            } else {
                Toast.makeText(this, "Per cambiare lo stato dell'obiettivo devi essere il leader del progetto.", Toast.LENGTH_SHORT).show();
            }
        };

        objectivesAdapter = new ProjectListsAdapter(objectives, onObjectiveClick);
        mObjectivesList.setAdapter(objectivesAdapter);
        objectivesAdapter.notifyDataSetChanged();

        //  Imposta la ListView per visualizzare i teammates
        LinearLayoutManager teammatesLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        mTeammatesList.setLayoutManager(teammatesLayoutManager);

        //  Permette al leader di modificare il valore degli objectives
        //  Possono assumere false o true, true indica che sono completi.
        View.OnClickListener onTeammateClick = v -> {
            String teammate = ((TextView)v).getText().toString();
            viewTeammateProfile(teammate);
        };

        ProjectListsAdapter teammatesAdapter = new ProjectListsAdapter(project.getTeammates(), onTeammateClick);
        mTeammatesList.setAdapter(teammatesAdapter);
        teammatesAdapter.notifyDataSetChanged();

        mDescriptionTextView.setText(project.getDescrizione());
    }

    public void updateProgress(Progetto project) {
        String progressText = "Progress: " + (int) ((project.obiettiviCompleti() / project.numeroObiettivi()) * 100) + '%';
        mProgressTextView.setText(progressText);
    }

    private void leaveProject() {
        if (!Objects.equals(firebaseAuthUtils.getCurrentUser().getDisplayName(), progetto.getLeader()) &&
                progetto.getTeammates().contains(firebaseAuthUtils.getCurrentUser().getDisplayName())) {
            AlertDialog.Builder leaveProjectDialogBuilder = new AlertDialog.Builder(this);
            leaveProjectDialogBuilder.setTitle("Leave Project");
            leaveProjectDialogBuilder.setMessage("Are you sure you want to leave the team on project " +
                    progetto.getTitolo() + "? This action cannot be undone (although you can always request to rejoin later).");

            leaveProjectDialogBuilder.setPositiveButton(R.string.ok_text, ((dialog, which) -> {
                progetto.removeTeammate(firebaseAuthUtils.getCurrentUser().getDisplayName());
                firestoreUtils.updateProjectData(progetto.getId(), FirestoreUtils.KEY_TEAMMATES, progetto.getTeammates());
                dialog.dismiss();

                Intent leaveProjectIntent = new Intent(this, MainActivity.class);
                startActivity(leaveProjectIntent);
                finish();
            }));

            leaveProjectDialogBuilder.setNegativeButton(R.string.cancel_text, (((dialog, which) -> dialog.dismiss())));

            AlertDialog leaveTeamDialog = leaveProjectDialogBuilder.create();
            leaveTeamDialog.show();
        }
    }

    private void deleteProject() {
        if (Objects.equals(firebaseAuthUtils.getCurrentUser().getDisplayName(), progetto.getLeader())) {
            AlertDialog.Builder deleteProjectDialogBuilder = new AlertDialog.Builder(this);
            deleteProjectDialogBuilder.setTitle("Delete " + progetto.getTitolo());
            deleteProjectDialogBuilder.setMessage("Are you sure you want to delete this project? This action cannot be undone.");
            deleteProjectDialogBuilder.setPositiveButton("OK", (dialog, which) -> firestoreUtils.getFirestoreInstance().collection(FirestoreUtils.KEY_PROJECTS)
                    .document(progetto.getId()).delete()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Intent homeIntent = new Intent(this, MainActivity.class);
                            startActivity(homeIntent);
                            finish();
                        } else Toast.makeText(this, "Error deleting project", Toast.LENGTH_LONG).show();
                    }));
            deleteProjectDialogBuilder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

            AlertDialog deleteProjectDialog = deleteProjectDialogBuilder.create();
            deleteProjectDialog.show();
        } else Toast.makeText(this, "Only the project leader can delete the project", Toast.LENGTH_LONG).show();
    }
}