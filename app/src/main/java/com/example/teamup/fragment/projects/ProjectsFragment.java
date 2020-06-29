package com.example.teamup.fragment.projects;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teamup.R;
import com.example.teamup.activity.MainActivity;
import com.example.teamup.activity.ProjectActivity;
import com.example.teamup.utilities.FirestoreUtils;
import com.example.teamup.utilities.UserProjectsAdapter;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProjectsFragment extends Fragment {

    //  UI
    private RecyclerView leaderProjectsListView;
    private RecyclerView teammateProjectsListView;
    private List<String> teammateProjectsList;

    private UserProjectsAdapter leaderProjectsAdapter;
    private UserProjectsAdapter teammateProjectsAdapter;

    FirestoreUtils firestoreUtils;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        final MainActivity activity = (MainActivity) requireActivity();
        firestoreUtils = new FirestoreUtils(activity.firestoreUtils.getFirestoreInstance());

        View root = inflater.inflate(R.layout.fragment_projects, container, false);


        leaderProjectsListView = root.findViewById(R.id.leaderProjects_listView);
        teammateProjectsListView = root.findViewById(R.id.teammateProjects_listView);

        populateProjectsListViews(
                activity.firebaseAuthUtils.getCurrentUser(),
                firestoreUtils.getFirestoreInstance());

        return root;
    }

    //  Ottiene i titoli dei progetti in cui l'utente è coinvolto, separando quelli
    //  di cui è Leader e quelli in cui è Teammate in liste distinte
    private void populateProjectsListViews(FirebaseUser firebaseUser,
                                           FirebaseFirestore firebaseFirestore) {

        LinearLayoutManager leaderProjectsLayoutManager = new LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false);
        leaderProjectsListView.setLayoutManager(leaderProjectsLayoutManager);

        //  Query per i progetti di cui l'utente corrente è Leader
        Query queryLeader = firebaseFirestore.collection(FirestoreUtils.KEY_PROJECTS).whereEqualTo(FirestoreUtils.KEY_LEADER, firebaseUser.getDisplayName());
        queryLeader.addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (queryDocumentSnapshots != null) {
                List<String> leaderProjects = new ArrayList<>();
                for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                    if (documentChange.getType() == DocumentChange.Type.ADDED) {
                        leaderProjects.add((String) documentChange.getDocument().getData().get(FirestoreUtils.KEY_TITLE));
                    }
                }

                View.OnClickListener onLeaderProjectClick = v -> {
                    String title = ((TextView)v).getText().toString();
                    Intent projectIntent = new Intent(getContext(), ProjectActivity.class);
                    projectIntent.putExtra(FirestoreUtils.KEY_TITLE, title);
                    startActivity(projectIntent);
                };

                leaderProjectsAdapter = new UserProjectsAdapter(leaderProjects, onLeaderProjectClick);

                leaderProjectsListView.setAdapter(leaderProjectsAdapter);
                leaderProjectsAdapter.notifyDataSetChanged();
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

                        LinearLayoutManager teammateProjectsLayoutManager = new LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false);
                        teammateProjectsListView.setLayoutManager(teammateProjectsLayoutManager);

                        View.OnClickListener onTeammateProjectClick = v -> {
                            String title = ((TextView)v).getText().toString();
                            Intent projectIntent = new Intent(getContext(), ProjectActivity.class);
                            projectIntent.putExtra(FirestoreUtils.KEY_TITLE, title);
                            startActivity(projectIntent);
                        };

                        teammateProjectsAdapter = new UserProjectsAdapter(teammateProjectsList, onTeammateProjectClick);
                        teammateProjectsListView.setAdapter(teammateProjectsAdapter);
                        teammateProjectsAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }
}
