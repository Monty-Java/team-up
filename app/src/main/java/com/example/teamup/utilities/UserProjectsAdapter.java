package com.example.teamup.utilities;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teamup.R;

import java.util.List;

public class UserProjectsAdapter extends RecyclerView.Adapter<ProjectListsAdapter.ViewHolder> {

    private static final String TAG = UserProjectsAdapter.class.getSimpleName();

    private List<String> projects;
    private View.OnClickListener onProjectClick;

    public UserProjectsAdapter(List<String> projects, View.OnClickListener onProjectClick) {
        Log.d(TAG, "Costruttore");

        this.projects = projects;
        this.onProjectClick = onProjectClick;
    }

    @NonNull
    @Override
    public ProjectListsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder");

        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.project_lists_item, parent, false);

        return new ProjectListsAdapter.ViewHolder(view, onProjectClick);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectListsAdapter.ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder");

        String project = projects.get(position);
        holder.bindTo(project);
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView projectTitle;

        private final View.OnClickListener onProjectClick;

        ViewHolder(@NonNull View itemView, View.OnClickListener onObjectiveClick) {
            super(itemView);

            projectTitle = itemView.findViewById(R.id.objectiveTitle);
            this.onProjectClick = onObjectiveClick;
        }

        void bindTo(String objective) {

            projectTitle.setText(objective);

            projectTitle.setOnClickListener(onProjectClick);
        }
    }
}
