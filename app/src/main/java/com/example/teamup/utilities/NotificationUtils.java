package com.example.teamup.utilities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.teamup.R;
import com.example.teamup.activity.MainActivity;
import com.example.teamup.activity.NotificationViewActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class NotificationUtils extends FirebaseMessagingService {
    public static final String TAG = NotificationUtils.class.getSimpleName();

    //  Ottiene il messaggio associato alla notifica ricevuta
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String clickAction = remoteMessage.getNotification().getClickAction();  //  Non viene mai usato ma forse per qualche motivo arcano fa funzionare le notifiche?
        String messageTitle = remoteMessage.getNotification().getTitle();
        String messageBody = remoteMessage.getNotification().getBody();
        String notificationType = remoteMessage.getData().get("notificationType");
        String sentFrom = remoteMessage.getData().get("sender");
        String recipient = remoteMessage.getData().get("recipient");
        String project = remoteMessage.getData().get("project");

        //  Costruisce la notifica
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, getString(R.string.default_notification_channel_id))
                .setSmallIcon(R.drawable.team_up_logo)
                .setContentTitle(messageTitle)
                .setContentText(messageBody);

        Intent viewNotificationIntent = new Intent(this, NotificationViewActivity.class);
        viewNotificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        viewNotificationIntent.putExtra("type", notificationType);
        viewNotificationIntent.putExtra("sender", sentFrom);
        viewNotificationIntent.putExtra("recipient", recipient);
        viewNotificationIntent.putExtra("project", project);

        int notificationId = (int) System.currentTimeMillis();

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, viewNotificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        notificationBuilder.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Un NotificationChannel dev'essere definito per versioni di Android successive ad Oreo (incluso)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(getString(R.string.default_notification_channel_id),
                    "Default Channel",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        assert notificationManager != null;
        notificationManager.notify(notificationId, notificationBuilder.build());

        Log.d(TAG, "Notification received");
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
    }
}
