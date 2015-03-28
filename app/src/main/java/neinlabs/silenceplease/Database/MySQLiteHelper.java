package neinlabs.silenceplease.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import neinlabs.silenceplease.Location;

/**
 * Created by Saketh on 27-03-2015.
 */
public class MySQLiteHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "locations1.db";
    private static final int DATABASE_VERSION = 1;
    private String[] allColumns = { DBContract.LocationEntry._ID,
            DBContract.LocationEntry.COLUMN_CITY_NAME,DBContract.LocationEntry.COLUMN_COORD_LAT,DBContract.LocationEntry.COLUMN_COORD_LONG };

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    SQLiteDatabase database= this.getWritableDatabase();
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_LOCATION_TABLE = "CREATE TABLE " + DBContract.LocationEntry.TABLE_NAME + " (" +
                DBContract.LocationEntry._ID + " INTEGER PRIMARY KEY," +
                DBContract.LocationEntry.COLUMN_CITY_NAME + " TEXT NOT NULL, " +
                DBContract.LocationEntry.COLUMN_COORD_LAT + " REAL NOT NULL, " +
                DBContract.LocationEntry.COLUMN_COORD_LONG + " REAL NOT NULL" +
                ");";
        sqLiteDatabase.execSQL(SQL_CREATE_LOCATION_TABLE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MySQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.LocationEntry.TABLE_NAME);
        onCreate(db);
    }
    public void createComment(Location location) {

        ContentValues values = new ContentValues();
        values.put(DBContract.LocationEntry._ID,location.getId());
        values.put(DBContract.LocationEntry.COLUMN_CITY_NAME, location.getName());
        values.put(DBContract.LocationEntry.COLUMN_COORD_LAT, location.getLat());
        values.put(DBContract.LocationEntry.COLUMN_COORD_LONG, location.getLon());
        long insertId = database.insert(DBContract.LocationEntry.TABLE_NAME, null,
                values);
        location.setId(insertId);

    }
    public List<Location> getAllComments() {
        List<Location> comments = new ArrayList<>();

        Cursor cursor = database.query(DBContract.LocationEntry.TABLE_NAME,
                allColumns, null, null, null, null, null);
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
        comment.setId(cursor.getLong(0));
        comment.setName(cursor.getString(1));
        comment.setLat(cursor.getString(2));
        comment.setLon(cursor.getString(3));
        return comment;
    }
    public void deleteComment(Location location) {
        long id = location.getId();
        System.out.println("Comment deleted with id: " + id);
        Log.d("Commentdeletedwith id: ", String.valueOf(id));
        database.delete(DBContract.LocationEntry.TABLE_NAME, DBContract.LocationEntry._ID
                + " = " + id, null);
    }

}
