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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
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
    private RecyclerView mObjectivesList;
    private RecyclerView mTeammatesList;
    private TextView mDescriptionTextView;
    private TextView mProgressTextView;

    private ProjectListsAdapter objectivesAdapter;
    private ProjectListsAdapter teammatesAdapter;

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
            default:
                Log.w(TAG, "onOptionsItemSelected: item non riconosciuto");
        }
        return super.onOptionsItemSelected(item);
    }

    private void onDetailsClick() {
        Log.d(TAG, "onDetailsClick");

        final Dialog detailsDialog = new Dialog(this);
        detailsDialog.setContentView(R.layout.project_details_dialog);
        MaterialTextView projectId = detailsDialog.findViewById(R.id.projectId_textView);
        ListView projectTags = detailsDialog.findViewById(R.id.tagsListView);
        MaterialButton closeButton = detailsDialog.findViewById(R.id.closeDialogButton);
        MaterialButton addTagButton = detailsDialog.findViewById(R.id.addTagButton);

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
                        editProjectTags(tag);
                        tagsAdapter.notifyDataSetChanged();
            });

            projectTags.setOnItemLongClickListener((parent, view, position, id) -> {
                String tag = projectTags.getItemAtPosition(position).toString();
                removeTag(tag);
                tagsAdapter.notifyDataSetChanged();
                return false;
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
        TextInputEditText addObjectiveEditText = addObjectiveDialog.findViewById(R.id.add_objective_editText);
        MaterialButton addObjectivePositiveButton = addObjectiveDialog.findViewById(R.id.add_objective_positiveButton);
        MaterialButton addObjectiveNegativeButton = addObjectiveDialog.findViewById(R.id.add_objective_negativeButton);
        addObjectiveNegativeButton.setOnClickListener(v -> addObjectiveDialog.dismiss());

        addObjectivePositiveButton.setOnClickListener(v -> {
            if (!Objects.requireNonNull(addObjectiveEditText.getText()).toString().equals("")) {
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

    private void editProjectTags(String tagToEdit) {
        Dialog editTagsDialog = new Dialog(this);
        editTagsDialog.setContentView(R.layout.edit_project_tag_dialog);

        EditText editTagEditText = editTagsDialog.findViewById(R.id.editTagDialog_editText);
        editTagEditText.setHint(tagToEdit);

        Button editTagEditButton = editTagsDialog.findViewById(R.id.editTagDialog_editButton);
        Button editTagCancelButton = editTagsDialog.findViewById(R.id.editTagDialog_cancelButton);
        editTagCancelButton.setOnClickListener(v -> editTagsDialog.dismiss());

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

    private void removeTag(String tagToRemove) {
        AlertDialog.Builder removeTagBuilder = new AlertDialog.Builder(this);
        removeTagBuilder.setTitle("Remove Tag");
        removeTagBuilder.setMessage("Remove " + tagToRemove + " from project tag list?");
        removeTagBuilder.setPositiveButton(R.string.ok_text, (dialog, which) -> {
            progetto.removeEtichetta(tagToRemove);
            firestoreUtils.updateProjectData(progetto.getId(), FirestoreUtils.KEY_TAGS, progetto.getEtichette());
            dialog.dismiss();
        });
        removeTagBuilder.setNegativeButton(R.string.cancel_text, (dialog, which) -> dialog.dismiss());
    }

    private void addNewTag() {
        Dialog addTagDialog = new Dialog(this);
        addTagDialog.setContentView(R.layout.add_project_tag_dialog);

        TextInputEditText addTagEditText = addTagDialog.findViewById(R.id.add_tag_editText);
        MaterialButton addTagPositiveButton = addTagDialog.findViewById(R.id.add_tag_positiveButton);
        MaterialButton addTagNegativeButton = addTagDialog.findViewById(R.id.add_tag_negativeButton);

        addTagNegativeButton.setOnClickListener(v -> addTagDialog.dismiss());

        addTagPositiveButton.setOnClickListener(v -> {
            if (!Objects.requireNonNull(addTagEditText.getText()).toString().equals("")) {
                progetto.addEtichetta(addTagEditText.getText().toString());
                firestoreUtils.updateProjectData(progetto.getId(), FirestoreUtils.KEY_TAGS, progetto.getEtichette());
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
            TextInputEditText changeTitleEditText = changeTitleDialog.findViewById(R.id.change_title_editText);
            changeTitleEditText.setHint(progetto.getTitolo());
            MaterialButton changeTitlePositiveButton = changeTitleDialog.findViewById(R.id.change_title_positiveButton);
            MaterialButton changeTitleNegativeButton = changeTitleDialog.findViewById(R.id.change_title_negativeButton);
            changeTitleNegativeButton.setOnClickListener(v -> changeTitleDialog.dismiss());

            changeTitlePositiveButton.setOnClickListener(v -> {
                if (!Objects.requireNonNull(changeTitleEditText.getText()).toString().equals("") && !changeTitleEditText.getText().toString().equals(progetto.getTitolo())) {
                    AlertDialog.Builder confirmTitleChangeBuilder = new AlertDialog.Builder(this);
                    confirmTitleChangeBuilder.setTitle("Confirm Title Change");
                    confirmTitleChangeBuilder.setMessage("Change title from " + progetto.getTitolo() + " to " + changeTitleEditText.getText().toString() + "?");
                    confirmTitleChangeBuilder.setPositiveButton(R.string.ok_text, (dialog, which) -> {
                        progetto.setTitolo(changeTitleEditText.getText().toString());
                        firestoreUtils.updateProjectData(progetto.getId(), FirestoreUtils.KEY_TITLE, progetto.getTitolo());
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

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                //  Ottiene i dati da Firestore, assicurando che non siano null
                List<DocumentSnapshot> documents = Objects.requireNonNull(task.getResult()).getDocuments();
                Map<String, Object> firestoreData = Objects.requireNonNull(documents.get(0).getData());

                data.putAll(firestoreData);
                data.put(KEY_ID, documents.get(0).getId());
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
            } else
                Log.e(TAG, "Error reading data");
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
        for (Iterator<Map.Entry<String, Boolean>> entries = project.getObiettivi().entrySet().iterator(); entries.hasNext();) {
            if (entries.next().getValue()) entries.remove();
        }

        //  Imposta la ListView per visualizzare gli obiettivi

        LinearLayoutManager objectivesLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        mObjectivesList.setLayoutManager(objectivesLayoutManager);

        List<String> objectives = new ArrayList<>(progetto.getObiettivi().keySet());

        //  Permette al leader di modificare il valore degli objectives
        //  Possono assumere false o true, true indica che sono completi.
        View.OnClickListener onObjectiveClick = v -> {
            if (progetto.getLeader().equals(firebaseAuthUtils.getCurrentUser().getDisplayName())) {
                String objective = ((TextView)v).getText().toString();

                if (!Objects.requireNonNull(progetto.getObiettivi().get(objective))) {
                    progetto.setObiettivoRaggiunto(objective);
                    firestoreUtils.updateProjectData(progetto.getId(), FirestoreUtils.KEY_OBJ, progetto.getObiettivi());

                    objectivesAdapter.notifyDataSetChanged();
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

        teammatesAdapter = new ProjectListsAdapter(progetto.getTeammates(), onTeammateClick);
        mTeammatesList.setAdapter(teammatesAdapter);
        teammatesAdapter.notifyDataSetChanged();

        mDescriptionTextView.setText(project.getDescrizione());
        mDescriptionTextView.setOnLongClickListener(v -> {
            editProjectDescription();
            return false;
        });
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
}