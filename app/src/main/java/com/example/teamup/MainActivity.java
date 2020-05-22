package com.example.teamup;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.teamup.utilities.FirebaseAuthUtils;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MainActivity";

    private FirebaseAuthUtils firebaseAuthUtils;

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

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        //  Ottiene i riferimenti agli elementi dell'UI
        View headerView = navigationView.getHeaderView(0);
        userDisplayName = headerView.findViewById(R.id.display_name);
        userEmail = headerView.findViewById(R.id.email);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_projects, R.id.nav_settings, R.id.nav_logout)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        //  Registra un listener per permettere di effettuare un logout
        //  quando l'utente clicca sull'opzione appropriata.
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        firebaseAuthUtils = new FirebaseAuthUtils(FirebaseAuth.getInstance(), this);
        FirebaseUser firebaseUser = firebaseAuthUtils.getCurrentUser();
        if (firebaseUser != null) {
            //  Aggiorna l'UI con i dati dell'utente
            userDisplayName.setText(firebaseUser.getDisplayName());
            userEmail.setText(firebaseUser.getEmail());
        }
    }

    public void onFabClick(View view) {
        Log.d(TAG, "onDiscoverClick");

        Snackbar.make(view, "Porta l'utente a IUI-6", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .show();
    }

    public void onDiscoverClick(View view) {
        Log.d(TAG, "onDiscoverClick");

        //  TODO: Intent per inziare l'Activity relativa a IUI-7
        Toast.makeText(MainActivity.this, "Porta l'utente a IUI-7", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.profile, menu);
        return true;
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
                Toast.makeText(this, "My Projects", Toast.LENGTH_LONG).show();
                navController.navigate(menuItemId);
                if (mAppBarConfiguration.getDrawerLayout() != null) {
                    mAppBarConfiguration.getDrawerLayout().closeDrawers();
                }
                break;

            case R.id.nav_settings:
                Toast.makeText(this, "Settings", Toast.LENGTH_LONG).show();
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
