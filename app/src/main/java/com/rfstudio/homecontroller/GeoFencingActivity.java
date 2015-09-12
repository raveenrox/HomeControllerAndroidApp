package com.rfstudio.homecontroller;

import android.app.Activity;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.provider.SyncStateContract;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class GeoFencingActivity extends Activity implements OnMapReadyCallback {

    private SeekBar radiusBar;

    private GoogleMap map;
    private Circle circle;
    private CircleOptions circleOptions;
    private Location location;

    private int radius=0;
    private LatLng position;

    private static final String PREF_NAME = "settings";
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geo_fencing);

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        location = locationManager.getLastKnownLocation(locationManager.getBestProvider(new Criteria(), false));

        preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        position = new LatLng(Double.parseDouble(preferences.getString("latitude",Double.toString(location.getLatitude()))),
                Double.parseDouble(preferences.getString("longitude",Double.toString(location.getLongitude()))));

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        map = mapFragment.getMap();
        map.setMyLocationEnabled(true);
        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                circle.setCenter(latLng);
                position=latLng;
            }
        });

        radiusBar = (SeekBar) findViewById(R.id.mapRadius);
        radiusBar.setProgress((preferences.getInt("radius", 500)/100)-5);
        radiusBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                circle.setRadius((progress+5)*100);
                radius=(progress+5)*100;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_geo_fencing, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        circleOptions = new CircleOptions()
                .center(position)
                .radius(preferences.getInt("radius", 500))
                .fillColor(R.color.rav_map_fill);
        circle = googleMap.addCircle(circleOptions);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 13f));
    }

    public void geoFence(View view) {
        switch (view.getId())
        {
            case R.id.mapSave:
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("latitude",position.latitude+"");
                editor.putString("longitude",position.longitude+"");
                editor.putInt("radius",radius);
                editor.apply();
                Toast.makeText(this, "Position Saved", Toast.LENGTH_LONG).show();
                NavUtils.navigateUpFromSameTask(this);
                break;
            case R.id.mapCancel:
                NavUtils.navigateUpFromSameTask(this);
                break;
        }
    }
}
