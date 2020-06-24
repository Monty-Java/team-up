package com.example.teamup.fragment.projects;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.teamup.activity.MainActivity;
import com.example.teamup.activity.ProjectActivity;
import com.example.teamup.R;
import com.example.teamup.utilities.FirestoreUtils;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProjectsFragment extends Fragment {

    //  UI
    private List<String> leaderProjectsList;
    private List<String> teammateProjectsList;

    FirestoreUtils firestoreUtils;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        final MainActivity activity = (MainActivity) requireActivity();
        firestoreUtils = new FirestoreUtils(activity.firestoreUtils.getFirestoreInstance());

        View root = inflater.inflate(R.layout.fragment_projects, container, false);

        ListView leaderProjectsListView = root.findViewById(R.id.leaderProjects_listView);
        ListView teammateProjectsListView = root.findViewById(R.id.teammateProjects_listView);

        findUserProjects(activity,
                activity.firebaseAuthUtils.getCurrentUser(),
                firestoreUtils.getFirestoreInstance(),
                leaderProjectsListView,
                teammateProjectsListView);

        setProjectClickListener(leaderProjectsListView);
        setProjectClickListener(teammateProjectsListView);

        return root;
    }

    //  Registra un OnItemClickListener sulla ListView che dichiara un Intent
    //  per avviare ProjectActivity quando l'utente clicca sul titolo di un progetto
    private void setProjectClickListener(ListView listView) {
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String title = listView.getItemAtPosition(position).toString();
            Intent projectIntent = new Intent(getContext(), ProjectActivity.class);
            projectIntent.putExtra(FirestoreUtils.KEY_TITLE, title);
            startActivity(projectIntent);
        });
    }

    //  Ottiene i titoli dei progetti in cui l'utente è coinvolto, separando quelli
    //  di cui è Leader e quelli in cui è Teammate in liste distinte
    private void findUserProjects(Activity activity, FirebaseUser firebaseUser,
                                  FirebaseFirestore firebaseFirestore,
                                  ListView listViewLeader, ListView listViewTeammate) {

        //  Query per i progetti di cui l'utente corrente è Leader
        Query queryLeader = firebaseFirestore.collection(FirestoreUtils.KEY_PROJECTS).whereEqualTo(FirestoreUtils.KEY_LEADER, firebaseUser.getDisplayName());
        queryLeader.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                leaderProjectsList = new ArrayList<>();

                for (QueryDocumentSnapshot snapshot : Objects.requireNonNull(task.getResult()))
                    leaderProjectsList.add((String) snapshot.getData().get(FirestoreUtils.KEY_TITLE));

                ArrayAdapter<String> listAdapter = new ArrayAdapter<>(
                        activity,
                        android.R.layout.simple_list_item_1,
                        leaderProjectsList
                );
                listViewLeader.setAdapter(listAdapter);
            }
        });

        //  Ottiene i riferimenti a tutti i progetti
        firebaseFirestore.collection(FirestoreUtils.KEY_PROJECTS).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                teammateProjectsList = new ArrayList<>();
                for (DocumentSnapshot snapshot : Objects.requireNonNull(task.getResult())) {

                    //  Aggiunge i progetti che contengono il campo "teammates" nella lista teammateProjectsList
                    if (Objects.requireNonNull(snapshot.getData()).containsKey(FirestoreUtils.KEY_TEAMMATES)) {
                        @SuppressWarnings(value = "unchecked") List<String> team = (List<String>) snapshot.getData().get(FirestoreUtils.KEY_TEAMMATES);

                        if (team != null) {
                            //  Itera la lista team cercando i progetti in cui l'utente corrente è coinvolto
                            for (String s : team) {
                                if (s.contains(Objects.requireNonNull(firebaseUser.getDisplayName()))) {
                                    teammateProjectsList.add((String) snapshot.getData().get(FirestoreUtils.KEY_TITLE));
                                }
                            }
                        }


                        ArrayAdapter<String> listAdapter = new ArrayAdapter<>(
                                activity,
                                android.R.layout.simple_list_item_1,
                                teammateProjectsList
                        );
                        listViewTeammate.setAdapter(listAdapter);
                    }
                }
            }
        });
    }
}
