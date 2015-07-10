package sakethkaparthi.silenceplease;

import android.app.Activity;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.gc.materialdesign.views.ButtonFloat;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import me.alexrs.prefs.lib.Prefs;
import sakethkaparthi.silenceplease.Database.LocationProvider;


public class NewPlace extends Activity implements OnMapReadyCallback,GoogleMap.OnMapClickListener {
    GoogleMap myMap;
    ButtonFloat fb;
    EditText et;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_place);
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        fb = (ButtonFloat)findViewById(R.id.fab);
        et = (EditText)findViewById(R.id.et);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_place, menu);
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
    private Location cursorToComment(Cursor cursor) {
        Location comment = new Location();
        comment.setId(cursor.getLong(cursor.getColumnIndex(LocationProvider._ID)));
        comment.setName(cursor.getString(cursor.getColumnIndex(LocationProvider.NAME)));
        comment.setLat(cursor.getString(cursor.getColumnIndex(LocationProvider.latitude)));
        comment.setLon(cursor.getString(cursor.getColumnIndex(LocationProvider.longitude)));
        return comment;
    }
    @Override
    public void onMapClick(final LatLng latLng) {
        myMap.clear();
        addMarkers();
        MarkerOptions marker = new MarkerOptions().position(latLng).title("New place");
        myMap.addMarker(marker);
        Prefs.with(this).save("latitude", String.valueOf(latLng.latitude));
        Prefs.with(this).save("longitude", String.valueOf(latLng.longitude));
        et.setVisibility(View.VISIBLE);
        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add a new student record
                ContentValues values = new ContentValues();
                values.put(LocationProvider.NAME,
                        ((EditText) findViewById(R.id.et)).getText().toString());

                values.put(LocationProvider.latitude,
                        String.valueOf(latLng.latitude));
                values.put(LocationProvider.longitude,
                        String.valueOf(latLng.longitude));
                Uri uri = getContentResolver().insert(
                        LocationProvider.CONTENT_URI, values);
                startActivity(new Intent(NewPlace.this, SavedLocations.class));
            }
        });
    }
    public List<Location> getAllComments() {
        List<Location> comments = new ArrayList<>();

        String URL = LocationProvider.URL;
        CursorLoader cursorLoader = new CursorLoader(this, Uri.parse(URL),null,null,null,"name");
        Cursor cursor = cursorLoader.loadInBackground();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Location location = cursorToComment(cursor);
            comments.add(location);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return comments;
    }
    public void addMarkers(){
        List<Location> list = getAllComments();
        if(list!=null) {
            for (int i = 0; i < list.size(); i++) {
                String NAME = list.get(i).getName();
                String LATITUDE = list.get(i).getLat();
                String LONGITUDE = list.get(i).getLon();
                MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(Double.parseDouble(LATITUDE), Double.parseDouble(LONGITUDE))).title(NAME);
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                myMap.addMarker(markerOptions);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setMyLocationEnabled(true);
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        myMap = mapFragment.getMap();
        myMap.setOnMapClickListener(this);
        myMap.clear();
        myMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        addMarkers();
        Double latitude = Double.parseDouble(Prefs.with(this).getString("latitude", "0"));
        Double longitude = Double.parseDouble(Prefs.with(this).getString("longitude", "0"));
        MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(latitude, longitude)).title("New Place");
        if (latitude != 0 && longitude != 0) {
            myMap.addMarker(markerOptions);
        }
    }
}