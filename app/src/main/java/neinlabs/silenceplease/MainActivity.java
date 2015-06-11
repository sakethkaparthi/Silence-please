package neinlabs.silenceplease;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andexert.library.RippleView;
import com.gc.materialdesign.widgets.Dialog;
import com.gc.materialdesign.widgets.SnackBar;
import com.rey.material.widget.Switch;

import java.util.ArrayList;
import java.util.List;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import me.alexrs.prefs.lib.Prefs;
import neinlabs.silenceplease.Database.LocationProvider;
import neinlabs.silenceplease.Service.BackgroundService;
import neinlabs.silenceplease.Service.RecieverClass;
import neinlabs.silenceplease.Utils.CustomAdapter;
import neinlabs.silenceplease.Utils.Potato;
import neinlabs.silenceplease.Utils.ResideMenu;
import neinlabs.silenceplease.Utils.ResideMenuItem;


public class MainActivity extends Activity {
    ResideMenu resideMenu;
    ImageView iv;
    com.rey.material.widget.Switch k;
    public static CustomAdapter adapter;
    public static ListView listView;
    public static List<Location> CustomListViewValuesArr = new ArrayList<Location>();
    ImageView im;
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
    private Location cursorToComment(Cursor cursor) {
        Location comment = new Location();
        comment.setId(cursor.getLong(cursor.getColumnIndex(LocationProvider._ID)));
        comment.setName(cursor.getString(cursor.getColumnIndex(LocationProvider.NAME)));
        comment.setLat(cursor.getString(cursor.getColumnIndex(LocationProvider.latitude)));
        comment.setLon(cursor.getString(cursor.getColumnIndex(LocationProvider.longitude)));
        return comment;
    }
    private void deleteAll() {
        Uri uri = LocationProvider.CONTENT_URI;
        Cursor c = managedQuery(uri, null, null, null, "name");
        if (c.moveToFirst()) {
            do{
                int noD = getContentResolver().delete(LocationProvider.CONTENT_URI,LocationProvider.NAME+" = ? ",new String[]{c.getString(c.getColumnIndex(LocationProvider.NAME))});
            }while (c.moveToNext());
        }
        CustomListViewValuesArr.clear();
        adapter.notifyDataSetChanged();
    }

    public void onItemClick(String name)
    {
        int noD = getContentResolver().delete(LocationProvider.CONTENT_URI,LocationProvider.NAME+" = ? ",new String[]{name});
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resideMenu = new ResideMenu(this);
        resideMenu.setBackground(R.color.backgroundreside);
        resideMenu.attachToActivity(this);
        RelativeLayout relativeLayout;
        relativeLayout = (RelativeLayout) findViewById(R.id.HomeScreen);
        resideMenu.addIgnoredView(relativeLayout);
        final TextView textView1 = (TextView) findViewById(R.id.tv_appname);
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/RB.ttf");
        textView1.setTypeface(custom_font);
        k = (com.rey.material.widget.Switch) findViewById(R.id.switchView);
        final TextView textView2 = (TextView) findViewById(R.id.appState);
        Typeface custom_font2 = Typeface.createFromAsset(getAssets(), "fonts/RL.ttf");
        textView2.setTypeface(custom_font2);
        try {
            listView = (ListView) findViewById(R.id.list);
            CustomListViewValuesArr = getAllComments();

            Typeface custom_font4 = Typeface.createFromAsset(getAssets(), "fonts/RL.ttf");
            adapter = new CustomAdapter(this, CustomListViewValuesArr, getResources(), custom_font4);
            listView.setAdapter(adapter);
            im = (ImageView) findViewById(R.id.deleteall);
            im.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Dialog dialog = new Dialog(MainActivity.this, "Are you sure?", "This will delete all the location data and recovery is not possible");
                    dialog.setOnAcceptButtonClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            deleteAll();
                        }
                    });
                    dialog.addCancelButton("Cancel", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                }
            });
            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, final View view, final int position, long id) {
                    new SnackBar(MainActivity.this,
                            "Do you want to delete this data?",
                            "yes", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            TextView et = (TextView) view.findViewById(R.id.Name);
                            String name = et.getText().toString();
                            onItemClick(name);
                            new SnackBar(MainActivity.this, "Deleted", null, null).show();
                            CustomListViewValuesArr.clear();
                            CustomListViewValuesArr = getAllComments();
                            adapter.notifyDataSetChanged();
                        }
                    }).show();
                    return true;
                }
            });
            TextView textView3 = (TextView) findViewById(R.id.tv_locations);
            Typeface custom_font3 = Typeface.createFromAsset(getAssets(), "fonts/RB.ttf");
            textView3.setTypeface(custom_font3);
        } catch (Exception e) {
        }

        if (Prefs.with(this).getBoolean("state", true)) {
            k.setChecked(true);
            scheduleAlarm();
        } else {
            k.setChecked(false);
        }
        if (k.isChecked()) {
            textView2.setText("ON");
        } else {
            textView2.setText("OFF");
        }
        RippleView rippleView = (RippleView) findViewById(R.id.plese);
        rippleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Prefs.with(MainActivity.this).getBoolean("state", true)) {
                    k.setChecked(false);
                } else {
                    k.setChecked(true);
                }
            }
        });
        k.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(Switch aSwitch, boolean b) {
                if (aSwitch.isChecked()) {
                    textView2.setText("ON");
                    Prefs.with(MainActivity.this).save("state", true);
                    aSwitch.setChecked(true);
                    scheduleAlarm();
                } else {
                    textView2.setText("OFF");
                    aSwitch.setChecked(false);
                    Prefs.with(MainActivity.this).save("state", false);
                    cancelAlarm();
                }
            }
        });


        // create menu items;
        String titles[] = {"Saved Locations", "New place", "Settings"};
        int icon[] = {R.drawable.ic_place_white_24dp, R.drawable.ic_add, R.drawable.icon_settings};

        for (int i = 0; i < titles.length; i++) {
            final ResideMenuItem item = new ResideMenuItem(this, icon[i], titles[i], custom_font2);
            resideMenu.addMenuItem(item, ResideMenu.DIRECTION_LEFT);
            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (item.getTitle() == "Saved Locations") {
                        startActivity(new Intent(MainActivity.this, SavedLocations.class));
                    }
                    if (item.getTitle() == "New place") {
                        startActivity(new Intent(MainActivity.this, NewPlace.class));
                    }
                    if (item.getTitle() == "Settings") {
                        startActivity(new Intent(MainActivity.this, Settings.class));
                    }
                }
            });
        }
        if (!Potato.potate().getUtils().isInternetConnected(this)) {
            Crouton.makeText(MainActivity.this, "No Internet Connection", Style.ALERT, R.id.plese).show();
        }
        iv = (ImageView) findViewById(R.id.imageView2);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
            }
        });
        TextView desc = (TextView) findViewById(R.id.description);
        desc.setTypeface(custom_font2);
        String text = "Silence Please is a free <a href=\"https://github.com/sakethkaparthi/Silence-please\">Open source</a> android application which helps in turning the mobile to silent mode at desired locations without manual work. Start by adding a location from the menu on top left. Please feel free to send a pull request or open an issue";
        desc.setMovementMethod(LinkMovementMethod.getInstance());
        desc.setText(Html.fromHtml(text));
    }

    public void cancelAlarm() {
        Intent intent = new Intent(getApplicationContext(), RecieverClass.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, RecieverClass.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
        getApplicationContext().stopService(new Intent(MainActivity.this, BackgroundService.class));
        Log.d("no", "cancelled lel");
    }

    public void scheduleAlarm() {
        long time = 1;
        switch (Prefs.with(this).getInt("position", 0)) {
            case 0:
                time = 1;
                break;
            case 1:
                time = 5;
                break;
            case 2:
                time = 10;
                break;
            case 3:
                time = 30;
                break;
            case 4:
                time = 60;
                break;
        }
        Log.d("sync_freq", String.valueOf(time));
        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(getApplicationContext(), RecieverClass.class);
        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, RecieverClass.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Setup periodic alarm every 5 seconds
        long firstMillis = System.currentTimeMillis(); // first run of alarm is immediate
        long intervalMillis = time * 1000 * 60;
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis, intervalMillis, pIntent);
    }
}
