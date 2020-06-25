package com.example.teamup.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.teamup.R;
import com.example.teamup.utilities.FirebaseAuthUtils;
import com.example.teamup.utilities.FirestoreUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MainActivity";

    public FirebaseAuthUtils firebaseAuthUtils;
    public FirestoreUtils firestoreUtils;

    private AppBarConfiguration mAppBarConfiguration;

    //  UI
    ImageView userProfilePicture;
    TextView userDisplayName;
    TextView userEmail;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        //  Ottiene i riferimenti agli elementi dell'UI
        View headerView = navigationView.getHeaderView(0);

        userProfilePicture = headerView.findViewById(R.id.imageView_profilePic);
        userDisplayName = headerView.findViewById(R.id.display_name);
        userEmail = headerView.findViewById(R.id.email);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_projects, R.id.nav_profile, R.id.nav_discover, R.id.nav_logout)
                .setDrawerLayout(drawer)
                .build();
        NavHostFragment hostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        assert hostFragment != null;
        navController = hostFragment.getNavController();
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        //  Registra un listener per permettere di effettuare un logout
        //  quando l'utente clicca sull'opzione appropriata.
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        firebaseAuthUtils = new FirebaseAuthUtils(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance(), this);
        firestoreUtils = firebaseAuthUtils.getFirestoreUtils();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser firebaseUser = firebaseAuthUtils.getCurrentUser();
        if (firebaseUser != null) {
            //  Aggiorna l'UI con i dati dell'utente
            firestoreUtils.getProfilePic(firebaseUser.getUid(), userProfilePicture);
            userDisplayName.setText(firebaseUser.getDisplayName());
            userEmail.setText(firebaseUser.getEmail());
        }
    }

    public void onFabClick(View view) {
        Log.d(TAG, "onFabClick");

        final Dialog newProjectDialog = new Dialog(MainActivity.this);
        newProjectDialog.setContentView(R.layout.new_project_dialog);
        MaterialTextView leader = newProjectDialog.findViewById(R.id.leaderName_textView);

        String leaderText = "Leader: " + userDisplayName.getText().toString();
        leader.setText(leaderText);

        MaterialButton newProjectCancelButton = newProjectDialog.findViewById(R.id.cancelNewProjectButton);
        newProjectCancelButton.setOnClickListener(v -> newProjectDialog.dismiss());

        MaterialButton newProjectConfirmButton = newProjectDialog.findViewById(R.id.button_confirmNewProject);
        newProjectConfirmButton.setOnClickListener(v -> {
            TextInputEditText title = newProjectDialog.findViewById(R.id.projectTitle_editText);
            TextInputEditText description = newProjectDialog.findViewById(R.id.description_editText);
            TextInputEditText objectives = newProjectDialog.findViewById(R.id.objectives_editText);
            TextInputEditText tags = newProjectDialog.findViewById(R.id.projectTag_editText);

            String[] obj = Objects.requireNonNull(objectives.getText()).toString().split("\n");
            Map<String, Boolean> objectiveMap = new HashMap<>();
            for (String o : obj) {
                objectiveMap.put(o, false);
            }

            String[] tg = Objects.requireNonNull(tags.getText()).toString().split("\\W+");
            List<String> tagList = new ArrayList<>(Arrays.asList(tg));

            boolean checkEmptyFields = true;

            if (Objects.requireNonNull(title.getText()).toString().equals("")) {
                checkEmptyFields = false;
                title.setError("Empty field");
            }

            if (Objects.requireNonNull(description.getText()).toString().equals("")) {
                checkEmptyFields = false;
                description.setError("Empty field");
            }

            if (objectiveMap.isEmpty()) {
                checkEmptyFields = false;
                objectives.setError("Empty field");
            }

            if (tags.getText().toString().equals("")) {
                checkEmptyFields = false;
                tags.setError("Empty field");
            }

            if (checkEmptyFields) {
                firestoreUtils.storeNewProjectData(
                        title.getText().toString(),
                        description.getText().toString(),
                        userDisplayName.getText().toString(),
                        objectiveMap, tagList);
                newProjectDialog.hide();
            }
        });

        newProjectDialog.show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Log.d(TAG, "onNavigationItemSelected");

        int menuItemId = menuItem.getItemId();

        switch (menuItemId) {
            case R.id.nav_projects:
                Log.d(TAG, "onNavigationItemSelected: My Projects");
                navController.navigate(menuItemId);
                if (mAppBarConfiguration.getDrawerLayout() != null) {
                    mAppBarConfiguration.getDrawerLayout().closeDrawers();
                }
                break;

            case R.id.nav_profile:
                Log.d(TAG, "onNavigationItemSelected: My Profile");
                navController.navigate(menuItemId);
                if (mAppBarConfiguration.getDrawerLayout() != null) {
                    mAppBarConfiguration.getDrawerLayout().closeDrawers();
                }
                break;

            case R.id.nav_discover:
                Log.d(TAG, "onNavigationItemSelected: Discover");
                navController.navigate(menuItemId);
                if (mAppBarConfiguration.getDrawerLayout() != null) {
                    mAppBarConfiguration.getDrawerLayout().closeDrawers();
                }
                break;

            case R.id.nav_logout:
                firebaseAuthUtils.logout();
                break;

            default:
                Log.w(TAG, "onNavigationItemSelected: impossibile trovare item con id: " + menuItemId);
        }

        return true;
    }
}
