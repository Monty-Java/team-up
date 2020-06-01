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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProjectsFragment extends Fragment {
    private static final String TAG = ProjectsFragment.class.getSimpleName();

    private ProjectsViewModel projectsViewModel;
    private List<String> projectsList;

    FirestoreUtils firestoreUtils;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        final MainActivity activity = (MainActivity) getActivity();
        firestoreUtils = new FirestoreUtils(activity.firestore);

        projectsViewModel =
                ViewModelProviders.of(this).get(ProjectsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_projects, container, false);

        ListView projectsListView = root.findViewById(R.id.listview_projects);

        //  IUI-4: Un click su un progetto avvia ProjectActivity con i dati relativi a quel progetto.
        projectsListView.setOnItemClickListener((parent, view, position, id) -> {
            String title = projectsListView.getItemAtPosition(position).toString();
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

        Query query = activity.firestore.collection("projects").whereEqualTo("leader", user.getDisplayName());
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    final ListView projects = root.findViewById(R.id.listview_projects);
                    projectsList = new ArrayList<String>();

                    //  TODO: iterate through Firestore data for this user's projects
                    for (QueryDocumentSnapshot snapshot : task.getResult())
                        projectsList.add(snapshot.getData().get("title").toString());

                    ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(
                            activity,
                            android.R.layout.simple_list_item_1,
                            projectsList
                    );
                    projects.setAdapter(listAdapter);
                }
            }
        });

        return root;
    }
}
