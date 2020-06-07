package com.example.teamup.utilities;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teamup.ProjectActivity;
import com.example.teamup.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DiscoveryProjectsAdapter
        extends RecyclerView.Adapter<DiscoveryProjectsAdapter.ViewHolder>
        implements Filterable {

    private static final String TAG = DiscoveryProjectsAdapter.class.getSimpleName();

    private Activity activity;
    private List<Progetto> projects;
    private List<Progetto> projectsAll;

    public DiscoveryProjectsAdapter(Activity activity, List<Progetto> projects) {
        Log.d(TAG, "Costruttore");

        this.activity = activity;
        this.projects = projects;
        this.projectsAll = new ArrayList<>(projects);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder");

        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.discovery_project_item, parent, false);

        return new ViewHolder(view, activity);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder");

        Progetto project = projects.get(position);

        holder.bindTo(project);
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount");
        return projects.size();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    Filter filter = new Filter() {

        // Run on background thread
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            List<Progetto> filteredList = new ArrayList<>();

            if (constraint.toString().isEmpty()) {
                filteredList.addAll(projectsAll);
            } else {
                for (Progetto project : projectsAll) {
                    if (project.getTitolo().toLowerCase().contains(constraint.toString().toLowerCase())
                            || project.getEtichette().toString().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        filteredList.add(project);
                    }
                }
            }

            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredList;
            return filterResults  ;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            projects.clear();
            projects.addAll((Collection<? extends Progetto>) results.values);
            notifyDataSetChanged();
        }
    };

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Progetto project;

        private final ConstraintLayout projectLayout;
        private final TextView projectTitle;
        private final TextView projectTags;

        private Activity activity;

        ViewHolder(@NonNull View itemView, Activity activity) {
            super(itemView);

            projectLayout = itemView.findViewById(R.id.projectLayout);
            projectTitle = itemView.findViewById(R.id.projectTitle);
            projectTags = itemView.findViewById(R.id.projectTags);
        }

        void bindTo(Progetto project) {
            this.project = project;

            projectTitle.setText(project.getTitolo());
            projectTags.setText("Tags: " + project.getEtichette().toString());


            projectLayout.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Intent projectIntent = new Intent(activity, ProjectActivity.class);
            projectIntent.putExtra(FirestoreUtils.KEY_TITLE, projectTitle.getText().toString());
            activity.startActivity(projectIntent);
        }
    }
}
