package com.jlanka.tripplanner.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONObject;

public class DatabaseHandler extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "EVPLUG";

    // Contacts table name
    private static final String TABLE_EVDATA = "evdata";

    // Contacts Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_TIME = "timestamp";
    private static final String KEY_DATA = "data";

    private static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_EVDATA + "("
            + KEY_ID + " INTEGER PRIMARY KEY," + KEY_TIME + " TEXT,"
            + KEY_DATA + " TEXT" + ")";

    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_EVDATA;

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //Create tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL(SQL_DELETE_ENTRIES);

        // Create tables again
        onCreate(db);
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    //Insert values to the table contacts
    public void addData(VehicleData vehicle){
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues values=new ContentValues();
        values.put(KEY_TIME, vehicle.getTime());
        values.put(KEY_DATA, vehicle.getData());

        db.insert(TABLE_EVDATA, null, values);
        db.close();
    }

    public JSONObject getAllContacts(String dataType) {

        String selectQuery = "SELECT  * FROM " + TABLE_EVDATA;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        JSONObject returnData = new JSONObject();

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                long time = Long.parseLong(cursor.getString(1));
                String json = cursor.getString(2);
                try {
                    JSONObject obj = new JSONObject(json);
                    String value = obj.getString(dataType);
                    returnData.put(String.valueOf(time),value);
//                    Log.d("Data Type : "+dataType, "Timestamp : "+time+"        "+value);
                } catch (Throwable t) {
                    Log.e("My App", "Could not parse malformed JSON: \"" + json + "\"");
                }
            } while (cursor.moveToNext());
        }
        return returnData;
    }
}