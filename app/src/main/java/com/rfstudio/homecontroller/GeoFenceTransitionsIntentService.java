package com.rfstudio.homecontroller;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.util.Log;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import java.util.ArrayList;
import java.util.List;

public class GeoFenceTransitionsIntentService extends IntentService {

    protected static final String TAG = "RAV-geo";

    public GeoFenceTransitionsIntentService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geoFencingEvent = GeofencingEvent.fromIntent(intent);
        if (geoFencingEvent.hasError()) {
            String errorMessage = "Error "+geoFencingEvent.getErrorCode();
            Log.e(TAG, errorMessage);
            return;
        }
        int geoFenceTransition = geoFencingEvent.getGeofenceTransition();

        if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            List<Geofence> triggeringGeoFences = geoFencingEvent.getTriggeringGeofences();

            String geoFenceTransitionDetails = getGeoFenceTransitionDetails(this, geoFenceTransition, triggeringGeoFences);

            sendNotification(geoFenceTransitionDetails);
            Log.i(TAG, geoFenceTransitionDetails);
        } else {
            Log.e(TAG, "Error");
        }
    }

    private String getGeoFenceTransitionDetails(Context context, int geoFenceTransition, List<Geofence> triggeringGeoFences) {

        String geoFenceTransitionString = getTransitionString(geoFenceTransition);

        ArrayList triggeringGeoFencesIdsList = new ArrayList();
        for (Geofence geofence : triggeringGeoFences) {
            triggeringGeoFencesIdsList.add(geofence.getRequestId());
        }
        String triggeringGeoFencesIdsString = TextUtils.join(", ",  triggeringGeoFencesIdsList);

        return geoFenceTransitionString + ": " + triggeringGeoFencesIdsString;
    }

    private void sendNotification(String notificationDetails) {

        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                        R.mipmap.ic_launcher))
                .setColor(Color.RED)
                .setContentTitle(notificationDetails)
                .setContentText("Notif Sample")
                .setContentIntent(notificationPendingIntent);
        builder.setAutoCancel(true);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(0, builder.build());
    }

    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return "Entered Area";
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return "Area Exited";
            default:
                return "Unknown Transition";
        }
    }
}