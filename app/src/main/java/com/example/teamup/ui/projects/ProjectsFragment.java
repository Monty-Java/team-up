package com.example.teamup.ui.projects;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.teamup.MainActivity;
import com.example.teamup.ProjectActivity;
import com.example.teamup.R;
import com.example.teamup.utilities.FirestoreUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProjectsFragment extends Fragment {
    private static final String TAG = ProjectsFragment.class.getSimpleName();

    private ProjectsViewModel projectsViewModel;
    private List<String> leaderProjectsList;
    private List<String> teammateProjectsList;

    FirestoreUtils firestoreUtils;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        final MainActivity activity = (MainActivity) getActivity();
        firestoreUtils = new FirestoreUtils(activity.firestore);

        projectsViewModel =
                ViewModelProviders.of(this).get(ProjectsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_projects, container, false);

        ListView leaderProjectsListView = root.findViewById(R.id.leaderProjects_listView);
        ListView teammateProjectsListView = root.findViewById(R.id.teammateProjects_listView);


        //  IUI-4: Un click su un progetto avvia ProjectActivity con i dati relativi a quel progetto.
        leaderProjectsListView.setOnItemClickListener((parent, view, position, id) -> {
            //  TODO: questo codice si ripete- fare refactor in un metodo, passandogli le ListView come parametro
            String title = leaderProjectsListView.getItemAtPosition(position).toString();
            Intent projectIntent = new Intent(getContext(), ProjectActivity.class);
            projectIntent.putExtra("title", title);
            startActivity(projectIntent);
        });

        teammateProjectsListView.setOnItemClickListener((parent, view, position, id) -> {
            String title = teammateProjectsListView.getItemAtPosition(position).toString();
            Intent projectIntent = new Intent(getContext(), ProjectActivity.class);
            projectIntent.putExtra("title", title);
            startActivity(projectIntent);
        });

        /*
        final TextView textView = root.findViewById(R.id.text_gallery);
        projectsViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        */

        //  TODO: REFACTOR QUESTO BORDELLO!!!!!!!!!!!!
        //  Ottiene i progetti di cui l'utente è Leader e li inserisce in una ListView.
        //  La ListView implementa un OnItemClickListener, che apre la scheda relativa
        //  ad un progetto che riceve un click.
        //activity = (MainActivity) getActivity();
        FirebaseUser user = activity.firebaseAuthUtils.getCurrentUser();

        Query queryLeader = activity.firestore.collection("projects").whereEqualTo("leader", user.getDisplayName());
        queryLeader.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                final ListView projects = root.findViewById(R.id.leaderProjects_listView);
                leaderProjectsList = new ArrayList<String>();

                for (QueryDocumentSnapshot snapshot : task.getResult())
                    leaderProjectsList.add(snapshot.getData().get("title").toString());

                ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(
                        activity,
                        android.R.layout.simple_list_item_1,
                        leaderProjectsList
                );
                projects.setAdapter(listAdapter);
            }
        });

        activity.firestore.collection("projects").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                teammateProjectsList = new ArrayList<String>();
               for (DocumentSnapshot snapshot : task.getResult()) {
                   if (snapshot.getData().containsKey("teammates")) {
                       List<String> team = (List<String>) snapshot.getData().get("teammates");

                       for (String s : team) {
                           if (s.contains(user.getDisplayName())) {
                               teammateProjectsList.add((String) snapshot.getData().get("title").toString());
                           }
                       }

                       ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(
                               activity,
                               android.R.layout.simple_list_item_1,
                               teammateProjectsList
                       );
                       teammateProjectsListView.setAdapter(listAdapter);
                   }
               }
           }
        });

        return root;
    }
}
