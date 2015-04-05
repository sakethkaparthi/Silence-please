package neinlabs.silenceplease;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import neinlabs.silenceplease.Database.MySQLiteHelper;

public class SavedLocations extends Activity {
    public static CustomAdapter adapter;
    public static ListView listView;
    public static MySQLiteHelper mDbHelper;
    public static List<Location> CustomListViewValuesArr = new ArrayList<Location>();
    public void add(Location l){
        CustomListViewValuesArr.add(l);
    }
    public int getSize(){
        return CustomListViewValuesArr.size();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        listView=(ListView)findViewById(R.id.list);
        mDbHelper = new MySQLiteHelper(this);
        CustomListViewValuesArr= mDbHelper.getAllComments();
        adapter =new CustomAdapter(this,CustomListViewValuesArr,getResources());
        listView.setAdapter(adapter);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                new SweetAlertDialog(SavedLocations.this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Are you sure?")
                        .setContentText("Won't be able to recover this Data!")
                        .setConfirmText("Yes,delete it!")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                onItemClick(position);
                                sDialog
                                        .setTitleText("Deleted!")
                                        .setContentText("Your Loacation has been deleted!")
                                        .setConfirmText("OK")
                                        .setConfirmClickListener(null)
                                        .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
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

    public void onItemClick(int mPosition)
    {
        try{
            neinlabs.silenceplease.Location tempValues = (neinlabs.silenceplease.Location) CustomListViewValuesArr.get(mPosition);
            mDbHelper.deleteComment(tempValues);
            CustomListViewValuesArr.remove(mPosition);
            adapter.notifyDataSetChanged();
            Crouton.showText(SavedLocations.this, "Location Deleted", Style.CONFIRM);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
