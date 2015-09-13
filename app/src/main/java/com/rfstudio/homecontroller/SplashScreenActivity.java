package com.rfstudio.homecontroller;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;

public class SplashScreenActivity extends Activity {

    private static final String PREF_NAME = "settings";

    private HelperDataClass helperDataClass;
    private HelperClass helperClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        helperDataClass = new HelperDataClass();
        helperClass = new HelperClass(this, helperDataClass);
        helperClass.preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.rav_dark_blue));
        }

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (helperClass.preferences.getBoolean("initialized", false)) {
                    if(helperClass.isOnline()) {
                        String fullString = "";
                        try
                        {
                            URL url = new URL("http://" + helperClass.preferences.getString("url","192.168.1.100") + "/hc/db.xml");
                            URLConnection urlConnection = url.openConnection();
                            urlConnection.setConnectTimeout(HelperDataClass.timeOut);
                            urlConnection.setReadTimeout(HelperDataClass.readTimeOut);
                            helperDataClass.serverOnline = true;
                            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                            String line;
                            while ((line = reader.readLine()) != null) {
                                fullString += line+'\n';
                            }
                            reader.close();
                            try {
                                FileOutputStream fileOutputStream = openFileOutput("db.xml", Context.MODE_PRIVATE);
                                fileOutputStream.write(fullString.getBytes());
                                fileOutputStream.close();
                            }catch (Exception ex)
                            {
                                ex.printStackTrace();
                            }
                            helperDataClass.fullString = fullString;
                            helperClass.parse();
                            helperClass.sendRequest("ping");
                            new Thread(){
                                public void run()
                                {
                                    try {
                                        Thread.sleep(3000);
                                        if(helperDataClass.serverOnline) {
                                            //helperClass.checkStatus();
                                            helperClass.getStatus();
                                        }
                                    }catch (InterruptedException ex)
                                    {
                                        Log.d("RAV", "Error-Splash-Thread-Interrupt");
                                    }
                                }
                            }.start();
                        }catch (SocketTimeoutException ex) {
                            Toast.makeText(SplashScreenActivity.this, "Connection to the server timed out", Toast.LENGTH_LONG).show();
                            helperDataClass.serverOnline = false;
                        }
                        catch (Exception ex)
                        {
                            Log.d("RAV", "Error-Splash");
                            ex.printStackTrace();
                        }

                        Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                        intent.putExtra("helper", helperDataClass);
                        startActivity(intent);
                        finish();
                    }else {
                        Toast.makeText(SplashScreenActivity.this, "Please check the internet connectivity", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(SplashScreenActivity.this, "Please enter the details!", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(SplashScreenActivity.this, InitializeActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }, (1000 * 2));
    }
}
