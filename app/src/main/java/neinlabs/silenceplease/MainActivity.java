package neinlabs.silenceplease;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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

import neinlabs.silenceplease.buttons.FloatingActionButton;


public class MainActivity extends Activity implements OnMapReadyCallback,GoogleMap.OnMapClickListener{

    GoogleMap myMap;
    SQLiteDatabase mydb;
    Double distance;
    private AudioManager myAudioManager;
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
    public void CheckIfInRange(Double d){
         myAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
         if(d<100.0){
             myAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
             }else{
             myAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setMyLocationEnabled(true);
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
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
    public void Rotate(View view){
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.move);
        view.startAnimation(animation);
    }

    String format = null;

    @Override
    public void onMapClick(final LatLng latLng) {
        myMap.clear();
        MarkerOptions marker = new MarkerOptions().position(latLng).title("New place");
        myMap.addMarker(marker);
        mydb = openOrCreateDatabase(DBNAME, Context.MODE_PRIVATE, null);
        Cursor allrows = mydb.rawQuery("SELECT * FROM " + TABLE, null);
        for (int i = 0;i<allrows.getCount();i++){
            allrows.moveToPosition(i);
            String NAME = allrows.getString(1);
            String LATITUDE = allrows.getString(2);
            String LONGITUDE = allrows.getString(3);
            MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(Double.parseDouble(LATITUDE), Double.parseDouble(LONGITUDE))).title(NAME);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            myMap.addMarker(markerOptions);
        }
        FloatingActionButton fb = (FloatingActionButton)findViewById(R.id.normal_plus);
        final EditText et = (EditText)findViewById(R.id.et);
        et.setVisibility(View.VISIBLE);
        startAnim(et);
        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseHelper k = new DatabaseHelper();
                k.setk(String.valueOf(latLng.latitude));
                k.setl(String.valueOf(latLng.longitude));
                k.setn(et.getText().toString());
                startActivity(new Intent(MainActivity.this, DatabaseHelper.class));
            }
        });
        FetchWeatherTask weatherTask = new FetchWeatherTask();
         android.location.Location lt = myMap.getMyLocation();
         String latlng = String.valueOf(lt.getLatitude()) + "," + String.valueOf(lt.getLongitude());
         format = String.valueOf(latLng.latitude) + "," + String.valueOf(latLng.longitude);
         weatherTask.execute(latlng);
         Log.d("ok","from mapclick " + String.valueOf(distance));
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
                        .appendQueryParameter(FORMAT_PARAM,     format)
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
