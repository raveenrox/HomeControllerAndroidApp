package com.rfstudio.homecontroller;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;
import java.util.ArrayList;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity
        implements ConnectionCallbacks, OnConnectionFailedListener, ResultCallback<Status>
{

    GoogleCloudMessaging gcm;
    String regId;
    String PROJECT_NUMBER = "948034608446";

    private static final String TAG = "RAV-GEO";

    private EditText txtUsername;
    private EditText txtPassword;
    private EditText txtUrl;
    private EditText txtName;
    private ToggleButton geoFenceState;

    private static final String PREF_NAME = "settings";
    private SharedPreferences preferences;
    private HelperClass helperClass;

    protected GoogleApiClient googleApiClient;
    protected ArrayList<Geofence> geoFenceList;
    private boolean geoFencesAdded;
    private PendingIntent geoFencePendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        helperClass = new HelperClass(this, null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.rav_grey));
        }

        txtUsername = (EditText) findViewById(R.id.txtUsername);
        txtPassword = (EditText) findViewById(R.id.txtPassword);
        txtUrl = (EditText) findViewById(R.id.txtUrl);
        txtName = (EditText) findViewById(R.id.txtName);
        geoFenceState = (ToggleButton) findViewById(R.id.geoFenceState);

        preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        loadPreferences();

        geoFenceList = new ArrayList<Geofence>();
        geoFencePendingIntent = null;

        geoFencesAdded = preferences.getBoolean("geoFenceState", false);
        geoFenceState.setChecked(geoFencesAdded);

        populateGeofenceList();
        buildGoogleApiClient();
    }

    protected synchronized void buildGoogleApiClient()
    {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to GoogleApiClient");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "Connection suspended");
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        builder.addGeofences(geoFenceList);

        return builder.build();
    }

    public void geoFenceButtonHandler(View view) {
        if (!googleApiClient.isConnected()) {
            Toast.makeText(this, "Not Connected", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            if(preferences.getBoolean("geoFenceState", false)) {
                LocationServices.GeofencingApi.addGeofences(googleApiClient, getGeofencingRequest(), getGeoFencePendingIntent()).setResultCallback(this);

            } else {
                LocationServices.GeofencingApi.removeGeofences(googleApiClient, getGeoFencePendingIntent()).setResultCallback(this);
            }
        } catch (SecurityException securityException) {
            logSecurityException(securityException);
        }
    }

    private void logSecurityException(SecurityException securityException) {
        Log.e(TAG, "Invalid location permission. " +
                "You need to use ACCESS_FINE_LOCATION with geofences", securityException);
    }

    public void onResult(Status status) {
        if (status.isSuccess()) {
            geoFencesAdded = !geoFencesAdded;
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("geoFenceState", geoFencesAdded);
            editor.commit();

            geoFenceState.setChecked(geoFencesAdded);

            Toast.makeText(
                    this,
                    (geoFencesAdded ? "Geo Fence Added" : "Geo Fence Removed"),
                    Toast.LENGTH_SHORT
            ).show();
        } else {
            String errorMessage = "Error"+status.getStatusCode();
            Log.e(TAG, errorMessage);
        }
    }

    private PendingIntent getGeoFencePendingIntent() {
        if (geoFencePendingIntent != null) {
            return geoFencePendingIntent;
        }
        Intent intent = new Intent(this, GeoFenceTransitionsIntentService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void populateGeofenceList() {
        HashMap<String, LatLng> LOCATIONS = new HashMap<String, LatLng>();
        LOCATIONS.put("HOME", new LatLng(7.2804603,79.8668137));
        for (Map.Entry<String, LatLng> entry : LOCATIONS.entrySet()) {
            geoFenceList.add(new Geofence.Builder()
                    .setRequestId(entry.getKey())
                    .setCircularRegion(
                            entry.getValue().latitude,
                            entry.getValue().longitude,
                            preferences.getInt("radius", 500)
                    )
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build());
        }
    }

    public void settingsAct(View v)
    {

        switch(v.getId()) {
            case R.id.btnSave:
                savePreferences();
                break;
            case R.id.btnClear:
                txtName.setText("");
                txtUsername.setText("");
                txtPassword.setText("");
                txtUrl.setText("");
                break;
            case R.id.btnRestore:
                txtUrl.setText("rfstudio.dlinkddns.com");
                break;
            case R.id.btnGeoFence:
                Intent intent = new Intent(this, GeoFencingActivity.class);
                startActivity(intent);
                break;

            // FIXME: 9/11/2015 notification button
           /* case R.id.btnNotif:
                Notification.Builder builder = new Notification.Builder(this);
                builder.setSmallIcon(R.mipmap.ic_launcher);
                builder.setContentTitle("Near Home");
                builder.setContentText("Do you want to open the gate?");

                //builder.setContentIntent(resultPendingIntent);
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(0, builder.build());
test
                break;*/
        }
    }

    private void savePreferences()
    {
        if(!txtName.getText().toString().isEmpty() && !txtUsername.getText().toString().isEmpty() && !txtPassword.getText().toString().isEmpty() && !txtUrl.getText().toString().isEmpty())
        {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("name", txtName.getText().toString());
            editor.putString("username", txtUsername.getText().toString());
            editor.putString("password", txtPassword.getText().toString());
            editor.putString("url", txtUrl.getText().toString());
            editor.apply();

        }else
        {
            Toast.makeText(this, "Fill the form correctly", Toast.LENGTH_LONG).show();

        }
    }

    private void loadPreferences()
    {
        txtName.setText(preferences.getString("name", ""));
        txtUsername.setText(preferences.getString("username",""));
        txtPassword.setText(preferences.getString("password",""));
        txtUrl.setText(preferences.getString("url", ""));
        geoFenceState.setChecked(preferences.getBoolean("geoFenceState", false));
    }

    public void registerGCM(View view) {
        getRegID();
    }

    public void getRegID() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if(gcm==null) {
                        gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    }
                    regId = gcm.register(PROJECT_NUMBER);
                    msg = "Device Registered, registration ID = "+regId;
                    Log.i("RAV-GCM", regId);

                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("regId", regId);
                    editor.apply();

                    helperClass.saveDevId(regId);

                } catch (Exception ex) { msg = "Error: "+ ex.getMessage(); }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                super.onPostExecute(msg);
                //Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        }.execute(null, null, null);
    }
}
