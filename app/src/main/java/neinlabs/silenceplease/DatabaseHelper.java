package neinlabs.silenceplease;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DatabaseHelper extends Activity {
    private ArrayAdapter<String> mForecastAdapter;
    SQLiteDatabase mydb;
    private static String DBNAME = "PERSONS.db";    // THIS IS THE SQLITE DATABASE FILE NAME.
    private static String TABLE = "MY_TABLE";       // THIS IS THE TABLE NAME
    static String k = "MAX";
    static String l = "lel";
    static String n = null;
    public void setk(String kai){
        k = kai;
    }
    public void setl(String lay){
        l = lay;
    }
    public void setn(String name){
        n = name;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        createTable();
        insertIntoTable(n,k,l);
        Toast.makeText(getApplicationContext(), "Showing table values after updation.", Toast.LENGTH_SHORT).show();
        showTableValues();
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
            dropTable();
            mForecastAdapter.clear();
            showTableValues();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    // THIS FUNCTION SETS COLOR AND PADDING FOR THE TEXTVIEWS
    public void setColor(TextView t){
        t.setTextColor(Color.BLACK);
        t.setPadding(20, 5, 0, 5);
        t.setTextSize(1, 15);
    }

    // CREATE TABLE IF NOT EXISTS
    public void createTable(){
        try{
            mydb = openOrCreateDatabase(DBNAME, Context.MODE_PRIVATE,null);
            mydb.execSQL("CREATE TABLE IF  NOT EXISTS "+ TABLE +" (ID INTEGER PRIMARY KEY, NAME TEXT,LATITUDE TEXT,LONGITUDE TEXT);");
            mydb.close();
        }catch(Exception e){
            Toast.makeText(getApplicationContext(), "Error in creating table", Toast.LENGTH_LONG);
        }
    }
    // THIS FUNCTION INSERTS DATA TO THE DATABASE
    public void insertIntoTable(String name,String lat,String lon){
        try{
            mydb = openOrCreateDatabase(DBNAME, Context.MODE_PRIVATE,null);
            mydb.execSQL("INSERT INTO " + TABLE + "(NAME,LATITUDE,LONGITUDE) VALUES('"+ name +"','"+lat+"','"+lon+"');");
            mydb.close();
        }catch(Exception e){
            Toast.makeText(getApplicationContext(), "Error in inserting into table", Toast.LENGTH_LONG);
        }
    }
    // THIS FUNCTION SHOWS DATA FROM THE DATABASE
    public void showTableValues(){

            String[] data = {
                      };
            List<String> weekForecast = new ArrayList<String>(Arrays.asList(data));

            // Now that we have some dummy forecast data, create an ArrayAdapter.
            // The ArrayAdapter will take data from a source (like our dummy forecast) and
            // use it to populate the ListView it's attached to.
            mForecastAdapter =
                    new ArrayAdapter<String>(
                            getApplicationContext(), // The current context (this activity)
                            R.layout.list_item, // The name of the layout ID.
                            R.id.titleTextview, // The ID of the textview to populate.
                            weekForecast);
            ListView listView = (ListView) findViewById(R.id.listview_forecast);
            listView.setAdapter(mForecastAdapter);
            mydb = openOrCreateDatabase(DBNAME, Context.MODE_PRIVATE, null);
            Cursor allrows = mydb.rawQuery("SELECT * FROM " + TABLE, null);
        for (int i = 0;i<allrows.getCount();i++){
            allrows.moveToPosition(i);
            String NAME = allrows.getString(1);
            mForecastAdapter.add(NAME);
        }

            mydb.close();
    }


    // THIS FUNCTION UPDATES THE DATABASE ACCORDING TO THE CONDITION
    public void updateTable(){
        try{
            mydb = openOrCreateDatabase(DBNAME, Context.MODE_PRIVATE,null);
            mydb.execSQL("UPDATE " + TABLE + " SET NAME = '"+k+"' WHERE PLACE = 'JAPAN'");
            mydb.close();
        }catch(Exception e){
            Toast.makeText(getApplicationContext(), "Error encountered", Toast.LENGTH_LONG);
        }
    }
    // THIS FUNCTION DELETES VALUES FROM THE DATABASE ACCORDING TO THE CONDITION
    public void deleteValues(){
        try{
            mydb = openOrCreateDatabase(DBNAME, Context.MODE_PRIVATE,null);
            mydb.execSQL("DELETE FROM " + TABLE + " WHERE PLACE = 'USA'");
            mydb.close();
        }catch(Exception e){
            Toast.makeText(getApplicationContext(), "Error encountered while deleting.", Toast.LENGTH_LONG);
        }
    }
    // THIS FUNTION DROPS A TABLE
    public void dropTable(){
        try{
            mydb = openOrCreateDatabase(DBNAME, Context.MODE_PRIVATE,null);
            mydb.execSQL("DROP TABLE " + TABLE);
            mydb.close();
        }catch(Exception e){
            Toast.makeText(getApplicationContext(), "Error encountered while dropping.", Toast.LENGTH_LONG);
        }
    }
}
