package neinlabs.silenceplease;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import neinlabs.silenceplease.Database.MySQLiteHelper;
import neinlabs.silenceplease.Service.RecieverClass;
import neinlabs.silenceplease.Utils.Potato;
import neinlabs.silenceplease.Utils.ResideMenu;
import neinlabs.silenceplease.Utils.ResideMenuItem;
import neinlabs.silenceplease.Utils.buttons.FloatingActionButton;


public class MainActivity extends Activity implements OnMapReadyCallback,GoogleMap.OnMapClickListener{
    Handler handler;
    GoogleMap myMap;
    MySQLiteHelper mDbHelper;
    ResideMenu resideMenu;
    private AudioManager myAudioManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resideMenu = new ResideMenu(this);
        resideMenu.setBackground(R.drawable.menu_background);
        resideMenu.attachToActivity(this);

        // create menu items;
        String titles[] = { "Home", "Saved Locations", "Calendar", "Settings" };
        int icon[] = { R.drawable.icon_home, R.drawable.icon_profile, R.drawable.icon_calendar, R.drawable.icon_settings };

        for (int i = 0; i < titles.length; i++){
            final ResideMenuItem item = new ResideMenuItem(this, icon[i], titles[i]);
            resideMenu.addMenuItem(item,  ResideMenu.DIRECTION_LEFT); // or  ResideMenu.DIRECTION_RIGHT
            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(item.getTitle()=="Saved Locations"){
                        startActivity(new Intent(MainActivity.this,SavedLocations.class));
                    }
                    if(item.getTitle()=="Settings"){
                        startActivity(new Intent(MainActivity.this,SettingsActivity.class));
                    }
                }
            });
        }
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        resideMenu.addIgnoredView(mapFragment.getView());
        mapFragment.getMapAsync(this);
        FloatingActionButton fb = (FloatingActionButton)findViewById(R.id.normal_plus);
        final EditText et = (EditText)findViewById(R.id.et);
        mDbHelper = new MySQLiteHelper(this);
        scheduleAlarm();
        fb.setAlpha(0f);
        et.setAlpha(0f);
        startAnim(fb);
        startAnim(et);
        fb.setAlpha(1f);
        et.setAlpha(1f);
        handler = new Handler();
        if(!Potato.potate().getUtils().isInternetConnected(this)){
            Crouton.showText(MainActivity.this,"No Internet Connection", Style.ALERT);
        }
       // BackgroundService bs = new BackgroundService();
       // bs.setList(mDbHelper.getAllComments());
    }
     @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    public void CheckIfInRange(Double d){
         myAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
         if(d<100.0){
             myAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
             }
         }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this,SettingsActivity.class));
            return true;
        }
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
    }
 public void addMarkers(){
     List<Location> list = mDbHelper.getAllComments();
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
        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(getApplicationContext(), RecieverClass.class);
        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, RecieverClass.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Setup periodic alarm every 5 seconds
        long firstMillis = System.currentTimeMillis(); // first run of alarm is immediate
        int intervalMillis = 10000; // 5 seconds
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis, intervalMillis, pIntent);
    }

  public void checkMarkerRanges(){
      List<Location> list = mDbHelper.getAllComments();
      if(list!=null) {
          for(int k =0 ; k<list.size();k++){
              String LATITUDE = list.get(k).getLat();
              String LONGITUDE = list.get(k).getLon();
              FetchWeatherTask weatherTask = new FetchWeatherTask();
              android.location.Location lt = myMap.getMyLocation();
              String latlng = String.valueOf(lt.getLatitude()) + "," + String.valueOf(lt.getLongitude());
              format = LATITUDE + "," + LONGITUDE;
              Log.d("checkMarker", latlng);
              weatherTask.execute(latlng, format);
      }
  }
  }
   private Runnable runnable = new Runnable() {
       @Override
       public void run() {
           checkMarkerRanges();
           handler.postDelayed(runnable,2000);
       }
   };
    public void startAnim(View view){
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade);
        view.startAnimation(animation);
    }
    String format = null;
    SavedLocations sl = new SavedLocations();
    @Override
    public void onMapClick(final LatLng latLng) {
        myMap.clear();
        addMarkers();
        if(Potato.potate().getUtils().isInternetConnected(this)){
        //handler.post(runnable);
        }
        MarkerOptions marker = new MarkerOptions().position(latLng).title("New place");
        myMap.addMarker(marker);
        FloatingActionButton fb = (FloatingActionButton)findViewById(R.id.normal_plus);
        final EditText et = (EditText)findViewById(R.id.et);
        et.setVisibility(View.VISIBLE);
        startAnim(et);
        final Location location = new Location();

        fb.setOnClickListener(new View.OnClickListener() {
        @Override
         public void onClick(View v) {
            cancelAlarm();
            try{
            location.setId(sl.getSize()+1);}catch (Exception e){
                e.printStackTrace();
            }
            location.setName(et.getText().toString());
            location.setLat(String.valueOf(latLng.latitude));
            location.setLon(String.valueOf(latLng.longitude));
            mDbHelper.createComment(location);
            startActivity(new Intent(MainActivity.this,SavedLocations.class));
           }
       });
      }
    public class FetchWeatherTask extends AsyncTask<String, Void, String> {

                 private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

                 private String getWeatherDataFromJson(String forecastJsonStr)
         throws JSONException {
            JSONObject data = new JSONObject(forecastJsonStr);
            JSONArray rows = data.getJSONArray("rows");
             JSONObject lol = rows.getJSONObject(0);
             JSONArray elements = lol.getJSONArray("elements");
             JSONObject k = elements.getJSONObject(0);
             JSONObject distance = k.getJSONObject("distance");
             double p = distance.getDouble("value");
             Log.d(LOG_TAG, "value : " + String.valueOf(p));
             return String.valueOf(p);
             }

                @Override
         protected String doInBackground(String... params) {

                    // If there's no zip code, there's nothing to look up. Verify size of params.
                             if (params.length == 0) {
                 return null;
                 }

                    // These two need to be declared outside the try/catch
                             // so that they can be closed in the finally block.
                                    HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

                   // Will contain the raw JSON response as a string
                             String forecastJsonStr = null;
             try {
                 // Construct the URL for the OpenWeatherMap query
                         // Possible parameters are avaiable at OWM's forecast API page, at
                                 // http://openweathermap.org/API#forecast
                                         final String FORECAST_BASE_URL =
                         "https://maps.googleapis.com/maps/api/distancematrix/json?";
                 final String QUERY_PARAM = "origins";
                 final String FORMAT_PARAM = "destinations";

                        Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                         .appendQueryParameter(QUERY_PARAM, params[0])
                        .appendQueryParameter(FORMAT_PARAM, params[1])
                        .build();

                      URL url = new URL(builtUri.toString());

                         Log.v(LOG_TAG, "Built URI " + builtUri.toString());
                                     // Create the request to OpenWeatherMap, and open the connection
                                 urlConnection = (HttpURLConnection) url.openConnection();
                 urlConnection.setRequestMethod("GET");
                 urlConnection.connect();

                         // Read the input stream into a String
                                InputStream inputStream = urlConnection.getInputStream();
                 StringBuffer buffer = new StringBuffer();
                 if (inputStream == null) {
                     // Nothing to do.
                             return null;
                     }
                 reader = new BufferedReader(new InputStreamReader(inputStream));

                         String line;
                 while ((line = reader.readLine()) != null) {
                     // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                             // But it does make debugging a *lot* easier if you print out the completed
                                     // buffer for debugging.
                                             buffer.append(line + "\n");
                     }

                         if (buffer.length() == 0) {
                     // Stream was empty. No point in parsing.
                             return null;
                     }
                 forecastJsonStr = buffer.toString();
                } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                 // If the code didn't successfully get the weather data, there's no point in attemping
                         // to parse it.
                                 return null;
                 } finally {
                 if (urlConnection != null) {
                     urlConnection.disconnect();
                     }
                 if (reader != null) {
                    try {
                         reader.close();
                         } catch (final IOException e) {
                         Log.e(LOG_TAG, "Error closing stream", e);
                         }
                     }
                 }
             try {
                 return getWeatherDataFromJson(forecastJsonStr);
                 } catch (JSONException e) {
                 e.printStackTrace();
                 }

                     return null;
             }

                 @Override
         protected void onPostExecute(String result) {
                CheckIfInRange(Double.parseDouble(result));
             }

           }
}
