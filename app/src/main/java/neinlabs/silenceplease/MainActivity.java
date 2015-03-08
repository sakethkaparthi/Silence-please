package neinlabs.silenceplease;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import neinlabs.silenceplease.buttons.FloatingActionButton;


public class MainActivity extends Activity implements OnMapReadyCallback,GoogleMap.OnMapClickListener{
    GoogleMap myMap;
    SQLiteDatabase mydb;
    private static String DBNAME = "PERSONS.db";    // THIS IS THE SQLITE DATABASE FILE NAME.
    private static String TABLE = "MY_TABLE";       // THIS IS THE TABLE NAME
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        FloatingActionButton fb = (FloatingActionButton)findViewById(R.id.normal_plus);
        final EditText et = (EditText)findViewById(R.id.et);
        fb.setAlpha(0f);
        et.setAlpha(0f);
        startAnim(fb);
        startAnim(et);
        fb.setAlpha(1f);
        et.setAlpha(1f);
    }
     @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
        googleMap.setMyLocationEnabled(true);


            MapFragment mapFragment = (MapFragment) getFragmentManager()
                    .findFragmentById(R.id.map);
            myMap = mapFragment.getMap();
            myMap.setOnMapClickListener(this);
        myMap.clear();
        myMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        mydb = openOrCreateDatabase(DBNAME, Context.MODE_PRIVATE, null);
        Cursor allrows = mydb.rawQuery("SELECT * FROM " + TABLE, null);
        for (int i = 0;i<allrows.getCount();i++){
            allrows.moveToPosition(i);
            String NAME = allrows.getString(1);
            String LATITUDE = allrows.getString(2);
            String LONGITUDE = allrows.getString(3);
            MarkerOptions marker = new MarkerOptions().position(new LatLng(Double.parseDouble(LATITUDE), Double.parseDouble(LONGITUDE))).title(NAME);
            marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            myMap.addMarker(marker);

        }

    }


    public void startAnim(View view){
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade);
        view.startAnimation(animation);
    }


    @Override
    public void onMapClick(final LatLng latLng) {
        FloatingActionButton fb = (FloatingActionButton)findViewById(R.id.normal_plus);
        final EditText et = (EditText)findViewById(R.id.et);
        et.setVisibility(View.VISIBLE);
        startAnim(et);
        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                test k = new test();
                k.setk(String.valueOf(latLng.latitude));
                k.setl(String.valueOf(latLng.longitude));
                k.setn(et.getText().toString());
                startActivity(new Intent(MainActivity.this, test.class));
            }
        });
       }
}
