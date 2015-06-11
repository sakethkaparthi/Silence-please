package neinlabs.silenceplease;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.util.Log;

import me.alexrs.prefs.lib.Prefs;
import neinlabs.silenceplease.Service.BackgroundService;
import neinlabs.silenceplease.Service.RecieverClass;
import neinlabs.silenceplease.Utils.Potato;

/**
 * Created by Saketh on 14-05-2015.
 */
public class BroadcastReciever extends BroadcastReceiver {
    private final static String TAG = "BroadcastReciever";
    private boolean stop=false;
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().matches("android.location.PROVIDERS_CHANGED"))
        {
            LocationManager lm ;
            boolean gps_enabled = false;
            lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            try{
                gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            }catch(Exception ex){}
            if(gps_enabled && Prefs.with(context).getBoolean("state",false)){
                long time =1;
                Log.d(TAG, String.valueOf(time));
                // Construct an intent that will execute the AlarmReceiver
                Intent intent1 = new Intent(context, RecieverClass.class);
                // Create a PendingIntent to be triggered when the alarm goes off
                final PendingIntent pIntent = PendingIntent.getBroadcast(context, RecieverClass.REQUEST_CODE,
                        intent1, PendingIntent.FLAG_UPDATE_CURRENT);
                // Setup periodic alarm every 5 seconds
                long firstMillis = System.currentTimeMillis(); // first run of alarm is immediate
                long intervalMillis = time*1000*60;
                AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis, intervalMillis, pIntent);
            }else if(!gps_enabled){
                {
                    Potato.potate().getNotifications().clearNotifications(context);
                    Intent intent2 = new Intent(context, RecieverClass.class);
                    final PendingIntent pIntent = PendingIntent.getBroadcast(context, RecieverClass.REQUEST_CODE,
                            intent2, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    alarm.cancel(pIntent);
                    context.stopService(new Intent(context, BackgroundService.class));
                    Log.d(TAG,"cancelled lel");
                    stop = true;
                }
            }
        }

    }
    public boolean getStop(){
        return stop;
    }
}
