package neinlabs.silenceplease;

import android.app.Activity;
import android.content.CursorLoader;
import android.database.Cursor;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import neinlabs.silenceplease.Database.LocationProvider;
import neinlabs.silenceplease.Utils.CustomAdapter;

public class SavedLocations extends Activity {
    public static CustomAdapter adapter;
    public static ListView listView;
    public static List<Location> CustomListViewValuesArr = new ArrayList<Location>();

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
        adapter =new CustomAdapter(this,CustomListViewValuesArr,getResources());
        listView.setAdapter(adapter);
        TextView textView1 = (TextView)findViewById(R.id.tv_locations);
        Typeface custom_font = Typeface.createFromAsset(getAssets(),"fonts/font.ttf") ;
        textView1.setTypeface(custom_font);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view, final int position, long id) {
                new SweetAlertDialog(SavedLocations.this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Are you sure?")
                        .setContentText("Won't be able to recover this Data!")
                        .setConfirmText("Yes,delete it!")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                TextView et = (TextView)view.findViewById(R.id.Name);
                                String name = et.getText().toString();
                                onItemClick(name);
                                sDialog
                                        .setTitleText("Deleted!")
                                        .setContentText("Your Loacation has been deleted!")
                                        .setConfirmText("OK")
                                        .setConfirmClickListener(null)
                                        .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                                CustomListViewValuesArr.clear();
                                CustomListViewValuesArr=getAllComments();
                                adapter.notifyDataSetChanged();
                            }
                        })
                        .show();
                return true;
            }
        });
        ImageView imageView = (ImageView)findViewById(R.id.imageView);

        imageView.setImageDrawable(Drawable.createFromPath(""));


    }
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_test, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.delete) {
            new SweetAlertDialog(SavedLocations.this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Are you sure?")
                    .setContentText("Won't be able to recover this Data!")
                    .setConfirmText("Yes,delete it!")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            CustomListViewValuesArr.clear();
                            showTableValues();
                            sDialog
                                    .setTitleText("Deleted!")
                                    .setContentText("Your Loacations have been deleted!")
                                    .setConfirmText("OK")
                                    .setConfirmClickListener(null)
                                    .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                        }
                    })
                    .show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public void showTableValues(){

        adapter.notifyDataSetChanged();
    }

    public void onItemClick(String name)
    {
        int noD = getContentResolver().delete(LocationProvider.CONTENT_URI,LocationProvider.NAME+" = ? ",new String[]{name});
    }
}
