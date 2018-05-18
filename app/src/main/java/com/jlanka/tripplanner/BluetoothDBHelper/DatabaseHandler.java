package com.jlanka.tripplanner.BluetoothDBHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "EVDATA";
    private static final String TABLE_DATA = "TempData";
    private static final String KEY_ID = "id";
    private static final String KEY_TIME = "timestamp";
    private static final String KEY_DATA = "data";

    public DatabaseHandler(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE_DATA = "CREATE TABLE " + TABLE_DATA + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_TIME + " TEXT,"
                + KEY_DATA + " TEXT" + ")";
        db.execSQL(CREATE_TABLE_DATA);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DATA);
        onCreate(db);
    }

    public void save(VehicleData vehicle){
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put(KEY_TIME, vehicle.getTime());
        values.put(KEY_DATA, vehicle.getData());

        db.insert(TABLE_DATA, null, values);
        db.close();
    }

    public List<VehicleData> findAll(){
        List<VehicleData> listperson=new ArrayList<VehicleData>();
        String query="SELECT * FROM "+TABLE_DATA;

        SQLiteDatabase db=this.getReadableDatabase();
        Cursor cursor=db.rawQuery(query, null);

        if(cursor.moveToFirst()){
            do{
                VehicleData vehicleData=new VehicleData();
                vehicleData.setId(Integer.valueOf(cursor.getString(0)));
                vehicleData.setTime(cursor.getLong(1));
                vehicleData.setData(cursor.getString(2));
                listperson.add(vehicleData);
            }while(cursor.moveToNext());
        }

        return listperson;
    }

}
