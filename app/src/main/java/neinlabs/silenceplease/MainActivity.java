package neinlabs.silenceplease;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import me.alexrs.prefs.lib.Prefs;
import neinlabs.silenceplease.Service.RecieverClass;
import neinlabs.silenceplease.Utils.Potato;
import neinlabs.silenceplease.Utils.ResideMenu;
import neinlabs.silenceplease.Utils.ResideMenuItem;
import neinlabs.silenceplease.Utils.Switch;



public class MainActivity extends Activity{
        ResideMenu resideMenu;
        ImageView iv;
        Switch k;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            resideMenu = new ResideMenu(this);
            resideMenu.setBackground(R.color.backgroundreside);
            resideMenu.attachToActivity(this);
            RelativeLayout relativeLayout;
            relativeLayout = (RelativeLayout)findViewById(R.id.HomeScreen);
            resideMenu.addIgnoredView(relativeLayout);
            final TextView textView1 = (TextView)findViewById(R.id.tv_appname);
            Typeface custom_font = Typeface.createFromAsset(getAssets(),"fonts/RB.ttf") ;
            textView1.setTypeface(custom_font);
            k=(Switch)findViewById(R.id.switchView);
            final TextView textView2 = (TextView)findViewById(R.id.appState);
            Typeface custom_font2 = Typeface.createFromAsset(getAssets(),"fonts/RL.ttf") ;
            textView2.setTypeface(custom_font2);
            if(Prefs.with(this).getBoolean("state",true)){
                k.setChecked(true);
                scheduleAlarm();
            }else{
                k.setChecked(false);
            }
            if(k.isChecked()){
                textView2.setText("ON");
            }else {
                textView2.setText("OFF");
            }

            k.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (k.isChecked()) {
                        textView2.setText("ON");
                        Prefs.with(MainActivity.this).save("state", true);
                        scheduleAlarm();
                    } else {
                        textView2.setText("OFF");
                        Prefs.with(MainActivity.this).save("state", false);
                        cancelAlarm();
                    }
                }
            });

         // create menu items;
            String titles[] = { "Saved Locations","New place" };
            int icon[] = { R.drawable.ic_place_white_24dp,R.drawable.ic_add };

            for (int i = 0; i < titles.length; i++){
                final ResideMenuItem item = new ResideMenuItem(this, icon[i], titles[i]);
                resideMenu.addMenuItem(item,  ResideMenu.DIRECTION_LEFT);
                item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (item.getTitle() == "Saved Locations") {
                            startActivity(new Intent(MainActivity.this, SavedLocations.class));
                        }
                        if (item.getTitle() == "New place") {
                            startActivity(new Intent(MainActivity.this, NewPlace.class));
                        }
                    }
                });
            }

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
            TextView desc = (TextView)findViewById(R.id.description);
            desc.setTypeface(custom_font2);
            desc.setText("Silence Please is a free Open source android application which helps in turning the mobile to silent mode at desired locations without manual work. Start by adding a location from the menu on top left. Please feel free to send a pull request or open an issue");

        }
        public void cancelAlarm() {
        Intent intent = new Intent(getApplicationContext(), RecieverClass.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, RecieverClass.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
        Log.d("no","cancelled lel");
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
}
