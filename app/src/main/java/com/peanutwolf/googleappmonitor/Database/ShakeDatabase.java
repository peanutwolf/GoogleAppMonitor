package com.peanutwolf.googleappmonitor.Database;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by vigursky on 31.03.2016.
 */
public class ShakeDatabase extends SQLiteOpenHelper {
    public static final String TABLE_SHAKE       = "shake";
    public static final String TABLE_TREK        = "trek";
    public static final String COLUMN_ID         = "_id";
    public static final String COLUMN_TREKID     = "trekID";
    public static final String COLUMN_AXISACCELX = "axisaccelx";
    public static final String COLUMN_AXISACCELY = "axisaccely";
    public static final String COLUMN_AXISACCELZ = "axisaccelz";
    public static final String COLUMN_AXISROTATX = "axisrotatx";
    public static final String COLUMN_AXISROTATY = "axisrotaty";
    public static final String COLUMN_AXISROTATZ = "axisrotatz";
    public static final String COLUMN_LONGITUDE  = "longitude";
    public static final String COLUMN_LATITUDE   = "latitude";
    public static final String COLUMN_SPEED      = "speed";
    public static final String COLUMN_TIMESTAMP  = "timestamp";
    public static final String COLUMN_DISTANCE   = "distance";

    private static final String DATABASE_NAME = "shakemeter.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_CREATE = "create table if not exists "
            + TABLE_SHAKE + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_AXISACCELX + " text not null, "
            + COLUMN_AXISACCELY + " text not null, "
            + COLUMN_AXISACCELZ + " text not null, "
            + COLUMN_AXISROTATX + " text not null, "
            + COLUMN_AXISROTATY + " text not null, "
            + COLUMN_AXISROTATZ + " text not null, "
            + COLUMN_LONGITUDE + " text not null, "
            + COLUMN_LATITUDE + " text not null, "
            + COLUMN_SPEED + " text not null, "
            + COLUMN_TIMESTAMP + " text not null,"
            + COLUMN_TREKID + " INTEGER, FOREIGN KEY("
            + COLUMN_TREKID + ") REFERENCES " + TABLE_TREK + " ("+ COLUMN_ID +")"
            +");";

    public static final String TABLE_ROUTES_CREATE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_TREK + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_TIMESTAMP + " text not null,"
            + COLUMN_DISTANCE + " text not null"
            + ");";

    public ShakeDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
        db.execSQL(TABLE_ROUTES_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " +  TABLE_SHAKE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TREK);
        onCreate(db);
    }

    public ArrayList<Cursor> getData(String Query){
        //get writable database
        SQLiteDatabase sqlDB = this.getWritableDatabase();
        String[] columns = new String[] { "mesage" };
        //an array list of cursor to save two cursors one has results from the query
        //other cursor stores error message if any errors are triggered
        ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
        MatrixCursor Cursor2= new MatrixCursor(columns);
        alc.add(null);
        alc.add(null);


        try{
            String maxQuery = Query ;
            //execute the query results will be save in Cursor c
            Cursor c = sqlDB.rawQuery(maxQuery, null);


            //add value to cursor2
            Cursor2.addRow(new Object[] { "Success" });

            alc.set(1,Cursor2);
            if (null != c && c.getCount() > 0) {


                alc.set(0,c);
                c.moveToFirst();

                return alc ;
            }
            return alc;
        } catch(SQLException sqlEx){
            Log.d("printing exception", sqlEx.getMessage());
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+sqlEx.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        } catch(Exception ex){

            Log.d("printing exception", ex.getMessage());

            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+ex.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        }


    }
}
