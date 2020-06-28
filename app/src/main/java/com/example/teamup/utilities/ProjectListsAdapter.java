package com.example.teamup.utilities;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teamup.R;

import java.util.List;

public class ProjectListsAdapter extends RecyclerView.Adapter<ProjectListsAdapter.ViewHolder> {

    private static final String TAG = ProjectListsAdapter.class.getSimpleName();

    private List<String> objectives;
    private View.OnClickListener onObjectiveClick;

    public ProjectListsAdapter(List<String> objectives, View.OnClickListener onObjectiveClick) {
        Log.d(TAG, "Costruttore");

        this.objectives = objectives;
        this.onObjectiveClick = onObjectiveClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder");

        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.project_lists_item, parent, false);

        return new ProjectListsAdapter.ViewHolder(view, onObjectiveClick);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder");

        String objective = objectives.get(position);
        holder.bindTo(objective);
    }

    @Override
    public int getItemCount() {
        return objectives.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final ConstraintLayout objectiveLayout;
        private final TextView objectiveTitle;

        private final View.OnClickListener onObjectiveClick;

        ViewHolder(@NonNull View itemView, View.OnClickListener onObjectiveClick) {
            super(itemView);

            objectiveLayout = itemView.findViewById(R.id.objectiveLayout);
            objectiveTitle = itemView.findViewById(R.id.objectiveTitle);
            this.onObjectiveClick = onObjectiveClick;
        }

        void bindTo(String objective) {

            objectiveTitle.setText(objective);

            objectiveTitle.setOnClickListener(onObjectiveClick);
        }
    }


}
