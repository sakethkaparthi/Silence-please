package neinlabs.silenceplease;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class MainActivity extends Activity implements OnMapReadyCallback,GoogleMap.OnMapClickListener{
    GoogleMap myMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
        // latitude and longitude
        double latitude =12.1231231 ;
        double longitude = 23.21321312;

// create marker
        MarkerOptions marker = new MarkerOptions().position(new LatLng(latitude, longitude)).title("Hello Maps ");
        marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
// adding marker
        googleMap.addMarker(marker);
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        myMap = mapFragment.getMap();
        myMap.setOnMapClickListener(this);


    }

    @Override
    public void onMapClick(final LatLng latLng) {
        myMap.clear();
        MarkerOptions marker = new MarkerOptions().draggable(true).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).position(latLng).title("is marker");
        myMap.addMarker(marker);
        Button b = (Button)findViewById(R.id.b);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                test k = new test();
                k.setk(String.valueOf(latLng.latitude));
                k.setl(String.valueOf(latLng.longitude));
                startActivity(new Intent(MainActivity.this, test.class));
            }
        });
       }
}
