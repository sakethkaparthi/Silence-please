package neinlabs.silenceplease;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import neinlabs.silenceplease.Database.MySQLiteHelper;

public class SavedLocations extends ActionBarActivity {
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
            Toast.makeText(getApplicationContext(),"Deleting all locations",Toast.LENGTH_SHORT).show();
            CustomListViewValuesArr.clear();
            showTableValues();
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
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
