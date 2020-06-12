package com.example.teamup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;

public class SponsorProjectActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler {
    public static final String TAG = SponsorProjectActivity.class.getSimpleName();

    BillingProcessor mBillingProcessor;
    String mProject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sponsor_project);

        mProject = getIntent().getStringExtra("project");
        Log.d(TAG, "Project: " + mProject);

        mBillingProcessor = new BillingProcessor(this, "test_key", this);
        mBillingProcessor.initialize();

        Button pay = findViewById(R.id.pay_button);
        pay.setOnClickListener(view -> {
            if (mBillingProcessor.isInitialized()) {
                Log.d(TAG, "Process Payment");
                mBillingProcessor.purchase(this, "android.test.purchased");
            }
        });
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
    public void onProductPurchased(String productId, TransactionDetails details) {
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
        //  TODO: flag project with title as sponsored in Firestore document
        Log.d(TAG, "Project Sponsored: " + title);
    }
}