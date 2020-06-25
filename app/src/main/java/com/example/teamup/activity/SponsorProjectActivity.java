package com.example.teamup.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.example.teamup.R;
import com.example.teamup.utilities.FirestoreUtils;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class SponsorProjectActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler {
    public static final String TAG = SponsorProjectActivity.class.getSimpleName();

    BillingProcessor mBillingProcessor;
    String mProject;
    FirestoreUtils mFirestore;

    //  TODO: BUG - se un pagamento è stato effettuato e si ritorna a questa Activity per un altro progetto, il pagamento avviene in automatico senza mostrare i prompt

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sponsor_project);

        mProject = getIntent().getStringExtra("project");
        setTitle("Sponsor " + mProject);

        mFirestore = new FirestoreUtils(FirebaseFirestore.getInstance());

        mBillingProcessor = new BillingProcessor(this, "test_key", this);
        mBillingProcessor.initialize();
        if (mBillingProcessor.isPurchased("android.test.purchased"))
            mBillingProcessor.consumePurchase("android.test.purchased");    //  Permette di ripetere il pagamento

        MaterialButton pay = findViewById(R.id.pay_button);
        pay.setOnClickListener(view -> mFirestore.getFirestoreInstance().collection(FirestoreUtils.KEY_PROJECTS)
                .whereEqualTo(FirestoreUtils.KEY_TITLE, mProject)
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot snapshot = Objects.requireNonNull(task.getResult()).getDocuments().get(0);
                if (!snapshot.contains(FirestoreUtils.KEY_SPONSORED)) {
                    if (mBillingProcessor.isInitialized()) {
                        Log.d(TAG, "Process Payment");
                        mBillingProcessor.purchase(this, "android.test.purchased");
                    }
                } else {
                    Toast.makeText(this, "The project has already been sponsored", Toast.LENGTH_LONG).show();
                }
            } else Log.d(TAG, "Error reading data from Firestore");
        }));
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "Destroying Activity");
        if (mBillingProcessor != null) mBillingProcessor.release();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (!mBillingProcessor.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onProductPurchased(@NonNull String productId, TransactionDetails details) {
        //  Chiamato quando il pagamento è stato effettuato con successo.
        //  Scrive un flag sul documento Firestore associato al progetto
        //  per permettere di posizionarlo in testa alla lista la prossima
        //  volta che DiscoverFragment è aperto.
        Log.d(TAG, "Transaction complete");

        markProjectSponsored(mProject);
    }

    @Override
    public void onPurchaseHistoryRestored() {
        //  Chiamato quando il purchase history è stato ristorato
        //  e la lista di tutti i ProductID è stata caricata da Google Play
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        //  Chiamato in caso di errore nel processo di pagamento
    }

    @Override
    public void onBillingInitialized() {
        //  Chiamato quando il BillingProcessor è inizializzato e pronto
        //  per effettuare un acquisto. L'effetto di un acquisto consiste
        //  nella riorganizzazione della lista di DiscoverFragment, spostando
        //  il progetto associato all'acquisto in testa.

        Log.d(TAG, "onBillingInitialized: Ready to process payments");
    }

    private void markProjectSponsored(String title) {
        mFirestore.getFirestoreInstance().collection(FirestoreUtils.KEY_PROJECTS)
                .whereEqualTo(FirestoreUtils.KEY_TITLE, title)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String projectId = Objects.requireNonNull(task.getResult()).getDocuments().get(0).getId();
                        mFirestore.updateProjectData(projectId, FirestoreUtils.KEY_SPONSORED, true);
                    } else Log.d(TAG, "Error updating project data");
        });
    }
}