package com.rfstudio.homecontroller;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class SettingsActivity extends AppCompatActivity{

    GoogleCloudMessaging gcm;
    String regId;
    String PROJECT_NUMBER = "948034608446";

    private EditText txtUsername;
    private EditText txtPassword;
    private EditText txtUrl;
    private EditText txtName;

    private static final String PREF_NAME = "settings";

    private SharedPreferences preferences;

    private HelperClass helperClass;


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

        preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        loadPreferences();
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
