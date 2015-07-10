package sakethkaparthi.silenceplease;

import android.app.Activity;
import android.content.CursorLoader;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.gc.materialdesign.widgets.Dialog;
import com.gc.materialdesign.widgets.SnackBar;

import java.util.ArrayList;
import java.util.List;

import sakethkaparthi.silenceplease.Database.LocationProvider;
import sakethkaparthi.silenceplease.Utils.CustomAdapter;

public class SavedLocations extends Activity {
    public static CustomAdapter adapter;
    public static ListView listView;
    public static List<Location> CustomListViewValuesArr = new ArrayList<Location>();
    ImageView im;
    com.rey.material.app.Dialog dialog;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        listView=(ListView)findViewById(R.id.list);
        CustomListViewValuesArr= getAllComments();
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/RL.ttf") ;
        adapter =new CustomAdapter(this,CustomListViewValuesArr,getResources(),custom_font);
        listView.setAdapter(adapter);
        im = (ImageView)findViewById(R.id.deleteall);
        im.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(SavedLocations.this, "Are you sure?", "This will delete all the location data and recovery is not possible");
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
                new SnackBar(SavedLocations.this,
                        "Do you want to delete this data?",
                        "yes", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TextView et = (TextView) view.findViewById(R.id.Name);
                        String name = et.getText().toString();
                        onItemClick(name);
                        new SnackBar(SavedLocations.this, "Deleted", null, null).show();
                        CustomListViewValuesArr.clear();
                        CustomListViewValuesArr = getAllComments();
                        adapter.notifyDataSetChanged();
                    }
                }).show();
                return true;
            }
        });
        TextView textView1 = (TextView)findViewById(R.id.tv_locations);
        Typeface custom_font2 = Typeface.createFromAsset(getAssets(), "fonts/RB.ttf") ;
        textView1.setTypeface(custom_font2);
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
}
