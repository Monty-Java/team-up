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
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.teamup.R;
import com.example.teamup.utilities.FirebaseAuthUtils;
import com.example.teamup.utilities.FirestoreUtils;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MainActivity";
    private static final String ERROR_MSG = "Empty field";

    private FirebaseAuthUtils mFirebaseAuthUtils;
    private FirestoreUtils mFirestoreUtils;

    private AppBarConfiguration mAppBarConfiguration;

    //  UI
    ImageView mUserProfilePicture;
    TextView mUserDisplayName;
    TextView mUserEmail;
    private NavController mNavController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        //  Ottiene i riferimenti agli elementi dell'UI
        View headerView = navigationView.getHeaderView(0);

        mUserProfilePicture = headerView.findViewById(R.id.imageView_profilePic);
        mUserDisplayName = headerView.findViewById(R.id.display_name);
        mUserEmail = headerView.findViewById(R.id.email);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_projects, R.id.nav_profile, R.id.nav_discover, R.id.nav_logout)
                .setDrawerLayout(drawer)
                .build();
        NavHostFragment hostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        assert hostFragment != null;
        mNavController = hostFragment.getNavController();
        NavigationUI.setupActionBarWithNavController(this, mNavController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, mNavController);

        //  Registra un listener per permettere di effettuare un logout
        //  quando l'utente clicca sull'opzione appropriata.
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mFirebaseAuthUtils = new FirebaseAuthUtils(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance(), this);
        mFirestoreUtils = mFirebaseAuthUtils.getFirestoreUtils();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser firebaseUser = mFirebaseAuthUtils.getCurrentUser();
        if (firebaseUser != null) {
            //  Aggiorna l'UI con i dati dell'utente
            mFirestoreUtils.getProfilePic(firebaseUser.getUid(), mUserProfilePicture);
            mUserDisplayName.setText(firebaseUser.getDisplayName());
            mUserEmail.setText(firebaseUser.getEmail());
        }
    }

    public void onFabClick(View view) {
        Log.d(TAG, "onFabClick");

        final Dialog newProjectDialog = new Dialog(MainActivity.this);
        newProjectDialog.setContentView(R.layout.new_project_dialog);
        TextView leader = newProjectDialog.findViewById(R.id.leaderName_textView);

        String leaderText = "Leader: " + mUserDisplayName.getText().toString();
        leader.setText(leaderText);

        Button cancelButton = newProjectDialog.findViewById(R.id.cancelNewProject_button);
        cancelButton.setOnClickListener(v -> newProjectDialog.dismiss());

        Button newProjectConfirmButton = newProjectDialog.findViewById(R.id.button_confirmNewProject);
        newProjectConfirmButton.setOnClickListener(v -> {
            EditText title = newProjectDialog.findViewById(R.id.projectTitle_editText);
            EditText description = newProjectDialog.findViewById(R.id.description_editText);
            EditText objectives = newProjectDialog.findViewById(R.id.objectives_editText);
            EditText tags = newProjectDialog.findViewById(R.id.projectTag_editText);

            String[] obj = objectives.getText().toString().split("\n");
            Map<String, Boolean> objectiveMap = new HashMap<>();
            for (String o : obj) {
                objectiveMap.put(o, false);
            }

            String[] tg = tags.getText().toString().split("\\W+");
            List<String> tagList = new ArrayList<>(Arrays.asList(tg));

            boolean checkEmptyFields = true;

            if (title.getText().toString().equals("")) {
                checkEmptyFields = false;
                title.setError(ERROR_MSG);
            }

            if (description.getText().toString().equals("")) {
                checkEmptyFields = false;
                description.setError(ERROR_MSG);
            }

            if (objectiveMap.isEmpty()) {
                checkEmptyFields = false;
                objectives.setError(ERROR_MSG);
            }

            if (tags.getText().toString().equals("")) {
                checkEmptyFields = false;
                tags.setError(ERROR_MSG);
            }

            if (checkEmptyFields) {
                mFirestoreUtils.storeNewProjectData(
                        title.getText().toString(),
                        description.getText().toString(),
                        mUserDisplayName.getText().toString(),
                        objectiveMap, tagList);
                newProjectDialog.hide();
            }
        });

        newProjectDialog.show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        mNavController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(mNavController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Log.d(TAG, "onNavigationItemSelected");

        int menuItemId = menuItem.getItemId();

        switch (menuItemId) {
            case R.id.nav_projects:
                Log.d(TAG, "onNavigationItemSelected: My Projects");
                mNavController.navigate(menuItemId);
                if (mAppBarConfiguration.getDrawerLayout() != null) {
                    mAppBarConfiguration.getDrawerLayout().closeDrawers();
                }
                break;

            case R.id.nav_profile:
                Log.d(TAG, "onNavigationItemSelected: My Profile");
                mNavController.navigate(menuItemId);
                if (mAppBarConfiguration.getDrawerLayout() != null) {
                    mAppBarConfiguration.getDrawerLayout().closeDrawers();
                }
                break;

            case R.id.nav_discover:
                Log.d(TAG, "onNavigationItemSelected: Discover");
                mNavController.navigate(menuItemId);
                if (mAppBarConfiguration.getDrawerLayout() != null) {
                    mAppBarConfiguration.getDrawerLayout().closeDrawers();
                }
                break;

            case R.id.nav_logout:
                mFirebaseAuthUtils.logout();
                break;

            default:
                Log.w(TAG, "onNavigationItemSelected: impossibile trovare item con id: " + menuItemId);
        }

        return true;
    }

    public FirebaseAuthUtils getFirebaseAuthUtils() { return mFirebaseAuthUtils; }
}
