package neinlabs.silenceplease;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import me.alexrs.prefs.lib.Prefs;
import neinlabs.silenceplease.Database.LocationProvider;
import neinlabs.silenceplease.Service.RecieverClass;
import neinlabs.silenceplease.Utils.Potato;
import neinlabs.silenceplease.Utils.ResideMenu;
import neinlabs.silenceplease.Utils.ResideMenuItem;
import neinlabs.silenceplease.Utils.buttons.FloatingActionButton;


public class MainActivity extends Activity implements OnMapReadyCallback,GoogleMap.OnMapClickListener{
    Handler handler;
    GoogleMap myMap;
    ResideMenu resideMenu;
    SharedPreferences sharedPref;
    ImageView iv;
    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
        sharedPref = getPreferences(Context.MODE_PRIVATE);
        resideMenu = new ResideMenu(this);
        resideMenu.setBackground(R.drawable.menu_background);
        resideMenu.attachToActivity(this);
        TextView textView1 = (TextView)findViewById(R.id.tv_appname);
        Typeface custom_font = Typeface.createFromAsset(getAssets(),"fonts/font.ttf") ;
        textView1.setTypeface(custom_font);
         // create menu items;
        String titles[] = { "Saved Locations" };
        int icon[] = { R.drawable.ic_place_white_24dp };

        for (int i = 0; i < titles.length; i++){
            final ResideMenuItem item = new ResideMenuItem(this, icon[i], titles[i]);
            resideMenu.addMenuItem(item,  ResideMenu.DIRECTION_LEFT);
            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(item.getTitle()=="Saved Locations"){
                        startActivity(new Intent(MainActivity.this,SavedLocations.class));
                    }
                }
            });
        }
        ResideMenuItem item =new ResideMenuItem(this,R.drawable.ic_cancel_white_24dp,"Stop");
        resideMenu.addMenuItem(item,ResideMenu.DIRECTION_LEFT);
        item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelAlarm();
            }
        });
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        resideMenu.addIgnoredView(mapFragment.getView());
        mapFragment.getMapAsync(this);
        FloatingActionButton fb = (FloatingActionButton)findViewById(R.id.normal_plus);
        final EditText et = (EditText)findViewById(R.id.et);
        scheduleAlarm();
        fb.setAlpha(1f);
        et.setAlpha(1f);
        handler = new Handler();
        if(!Potato.potate().getUtils().isInternetConnected(this)){
            Crouton.showText(MainActivity.this,"No Internet Connection", Style.ALERT);
        }
        iv = (ImageView)findViewById(R.id.imageView2);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
            }
        });

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
        if(id==R.id.Saved_locs){
            startActivity(new Intent(MainActivity.this,SavedLocations.class));
        }

        return super.onOptionsItemSelected(item);
    }
    public void cancelAlarm() {
        Intent intent = new Intent(getApplicationContext(), RecieverClass.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, RecieverClass.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
        Log.d("no","cancelled lel");
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
        Double latitude = Double.parseDouble(Prefs.with(this).getString("latitude","0"));
        Double longitude = Double.parseDouble(Prefs.with(this).getString("longitude", "0"));
        MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(latitude, longitude)).title("New Place");
        if (latitude!=0 && longitude!=0){
        myMap.addMarker(markerOptions);
        }
    }
    public List<Location> getAllComments() {
        List<Location> comments = new ArrayList<>();

        String URL = LocationProvider.URL;
        CursorLoader cursorLoader = new CursorLoader(this,Uri.parse(URL),null,null,null,"name");
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
    private Location cursorToComment(Cursor cursor) {
        Location comment = new Location();
        comment.setId(cursor.getLong(cursor.getColumnIndex(LocationProvider._ID)));
        comment.setName(cursor.getString(cursor.getColumnIndex(LocationProvider.NAME)));
        comment.setLat(cursor.getString(cursor.getColumnIndex(LocationProvider.latitude)));
        comment.setLon(cursor.getString(cursor.getColumnIndex(LocationProvider.longitude)));
        return comment;
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
    public void scheduleAlarm() {
        long time =1;
        Log.d("sync_freq",String.valueOf(time));
        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(getApplicationContext(), RecieverClass.class);
        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, RecieverClass.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Setup periodic alarm every 5 seconds
        long firstMillis = System.currentTimeMillis(); // first run of alarm is immediate
        long intervalMillis = time*1000*60;
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis, intervalMillis, pIntent);
    }
    public void startAnim(View view){
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade);
        view.startAnimation(animation);
    }
    SavedLocations sl = new SavedLocations();
    @Override
    public void onMapClick(final LatLng latLng) {
        myMap.clear();
        addMarkers();
        MarkerOptions marker = new MarkerOptions().position(latLng).title("New place");
        myMap.addMarker(marker);
        Prefs.with(this).save("latitude", String.valueOf(latLng.latitude));
        Prefs.with(this).save("longitude", String.valueOf(latLng.longitude));
        FloatingActionButton fb = (FloatingActionButton)findViewById(R.id.normal_plus);
        final EditText et = (EditText)findViewById(R.id.et);
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
                startActivity(new Intent(MainActivity.this, SavedLocations.class));
            }
        });
      }

}
