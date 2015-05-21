package neinlabs.silenceplease.Service;

import android.app.IntentService;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import me.alexrs.prefs.lib.Prefs;
import neinlabs.silenceplease.BroadcastReciever;
import neinlabs.silenceplease.Database.LocationProvider;
import neinlabs.silenceplease.Location;
import neinlabs.silenceplease.R;
import neinlabs.silenceplease.Utils.Potato;

/**
 * Created by Saketh on 05-04-2015.
 */
public class BackgroundService extends IntentService {
    static List<Location> list;
    SharedPreferences mSettings;
    AudioManager myAudioManager;
    public BackgroundService() {
        super("test-service");
    }
    public List<Location> getAllComments() {
        List<Location> comments = new ArrayList<>();

        String URL = LocationProvider.URL;
        CursorLoader cursorLoader = new CursorLoader(getApplicationContext(),Uri.parse(URL),null,null,null,"name");
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
    @Override
    protected void onHandleIntent(Intent intent) {
        mSettings = getApplication().getSharedPreferences("Settings", 0);
        BroadcastReciever reciever = new BroadcastReciever();
        if(reciever.getStop()){
            Log.d("service","stopped man");
            stopSelf();
            }
        LocationManager lm = null;
        boolean gps_enabled = false,network_enabled = false;
        if(lm==null)
            lm = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        try{
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }catch(Exception ex){}
        try{
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }catch(Exception ex){}
        if(!gps_enabled){
            Potato.potate().getNotifications().showNotificationNoSound("GPS Connectivity Error","You have location services disabled", R.drawable.ic_place_white_24dp,new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS),getApplicationContext());
           }
        Log.d("service",String.valueOf(mSettings.getInt("sync_frequency",1)));
        list=getAllComments();
        Log.d("service", "start");
        MyLocation myLocation = new MyLocation();
        MyLocation.LocationResult locationResult = new MyLocation.LocationResult(){
            @Override
            public void gotLocation(android.location.Location location) {
                if (list != null && Potato.potate().getUtils().isInternetConnected(getApplicationContext())) {
                    for (int k = 0; k < list.size(); k++) {
                        String LATITUDE = list.get(k).getLat();
                        String LONGITUDE = list.get(k).getLon();
                        String latlng = String.valueOf(location.getLatitude()) + "," + String.valueOf(location.getLongitude());
                        String format = LATITUDE + "," + LONGITUDE;
                        Log.d("service", latlng);
                        FetchDistanceTask ft = new FetchDistanceTask();
                        ft.execute(latlng,format);
                        Log.d("service",format);
                    }
                }
            };
        };
        myLocation.getLocation(this, locationResult);
        Log.d("service","end");
    }
    public void CheckIfInRange(Double d){
        myAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        if(d< Prefs.with(getApplicationContext()).getInt("slider_val",100)){
            Log.d("settings", String.valueOf(Prefs.with(getApplicationContext()).getInt("slider_val", 100)));
            if(Prefs.with(getApplicationContext()).getInt("action_pos",0)==0) {
                myAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
            }else{
                myAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            }
        }
    }
    public class FetchDistanceTask extends AsyncTask<String, Void, String> {

        private final String LOG_TAG = FetchDistanceTask.class.getSimpleName();

        private String getDistanceDataFromJson(String forecastJsonStr)
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
            if (params.length == 0) {
                return null;
            }
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String forecastJsonStr = null;
            try {
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
                // Create the request to Google web Matrix, and open the connection
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
                return getDistanceDataFromJson(forecastJsonStr);
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
    public static class MyLocation {
        Timer timer1;
        LocationManager lm;
        LocationResult locationResult;
        boolean gps_enabled=false;
        boolean network_enabled=false;

        public boolean getLocation(Context context, LocationResult result)
        {
            //I use LocationResult callback class to pass location value from MyLocation to user code.
            //Log.d("service","getLocation");
            locationResult=result;
            if(lm==null)
                lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            //exceptions will be thrown if provider is not permitted.
            try{gps_enabled=lm.isProviderEnabled(LocationManager.GPS_PROVIDER);}catch(Exception ex){}
            try{network_enabled=lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);}catch(Exception ex){}

            //don't start listeners if no provider is enabled
            if(!gps_enabled && !network_enabled)
                return false;

            if(gps_enabled)
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGps);
            if(network_enabled)
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNetwork);
            timer1=new Timer();
            timer1.schedule(new GetLastLocation(), 20000);
            return true;
        }

        LocationListener locationListenerGps = new LocationListener() {
            public void onLocationChanged(android.location.Location location) {
                timer1.cancel();
                locationResult.gotLocation(location);
                lm.removeUpdates(this);
                lm.removeUpdates(locationListenerNetwork);
            }
            public void onProviderDisabled(String provider) {}
            public void onProviderEnabled(String provider) {}
            public void onStatusChanged(String provider, int status, Bundle extras) {}
        };

        LocationListener locationListenerNetwork = new LocationListener() {
            public void onLocationChanged(android.location.Location location) {
                timer1.cancel();
                locationResult.gotLocation(location);
                lm.removeUpdates(this);
                lm.removeUpdates(locationListenerGps);
            }
            public void onProviderDisabled(String provider) {}
            public void onProviderEnabled(String provider) {}
            public void onStatusChanged(String provider, int status, Bundle extras) {}
        };

        class GetLastLocation extends TimerTask {
            @Override
            public void run() {
                android.location.Location myLocation;
                myLocation = getLastKnownLocation();
                locationResult.gotLocation(myLocation);
            }
        }

        public static abstract class LocationResult{
            public abstract void gotLocation(android.location.Location location);
        }

        private android.location.Location getLastKnownLocation() {

            List<String> providers = lm.getProviders(true);
            android.location.Location bestLocation = null;
            for (String provider : providers) {
                android.location.Location l = lm.getLastKnownLocation(provider);
                if (l == null) {
                    continue;
                }
                if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                    // Found best last known location: %s", l);
                    bestLocation = l;
                }
            }
            return bestLocation;
        }
    }
}