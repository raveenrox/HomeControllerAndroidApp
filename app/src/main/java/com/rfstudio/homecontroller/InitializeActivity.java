package com.rfstudio.homecontroller;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

public class InitializeActivity extends Activity {
    EditText txtUsername;
    EditText txtPassword;
    EditText txtUrl;
    EditText txtName;

    private static final String PREF_NAME = "settings";

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initialize);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.rav_grey));
        }

        txtUsername = (EditText) findViewById(R.id.txtInitUsername);
        txtPassword = (EditText) findViewById(R.id.txtInitPassword);
        txtUrl = (EditText) findViewById(R.id.txtInitUrl);
        txtName = (EditText) findViewById(R.id.txtInitName);

        txtName.setText("");
        txtUsername.setText("");
        txtPassword.setText("");
        txtUrl.setText("");

        preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
    }

    public void initAct(View v)
    {
        switch(v.getId()) {
            case R.id.btnInitSave:
                savePreferences();
                break;
            case R.id.btnInitClear:
                txtName.setText("");
                txtUsername.setText("");
                txtPassword.setText("");
                txtUrl.setText("");
                break;
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
            editor.putBoolean("initialized", true);
            editor.commit();
            Intent intent = new Intent(this, SplashScreenActivity.class);
            startActivity(intent);
            finish();
        }
        else
        {
            Toast.makeText(this, "Fill the form correctly", Toast.LENGTH_LONG).show();
        }

    }
}
