package com.peanutwolf.googleappmonitor.Models;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.peanutwolf.googleappmonitor.Database.ShakeDBContentProvider;
import com.peanutwolf.googleappmonitor.Database.ShakeDatabase;
import com.peanutwolf.googleappmonitor.Services.DataSaverService;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by vigursky on 07.09.2016.
 */
public class TrekModelDAO {
    public static final String TAG = TrekModelDAO.class.getSimpleName()+"Class";
    private Context mContext;

    public TrekModelDAO(Context context){
        this.mContext = context;
    }

    public List<TrekModel> getTrekModels(){
        List<TrekModel> treks = new ArrayList<>();
        ContentResolver contentResolver = mContext.getContentResolver();

        Cursor cursor = contentResolver.query(ShakeDBContentProvider.CONTENT_TREK_URI,
                new String[]{ShakeDatabase.COLUMN_ID,
                        ShakeDatabase.COLUMN_TIMESTAMP,
                        ShakeDatabase.COLUMN_DISTANCE},
                null, null, null);

        while(cursor.moveToNext()){
            TrekModel trekModel = new TrekModel();
            trekModel.setId(cursor.getInt(0));
            trekModel.setTimestamp(cursor.getString(1));
            trekModel.setDistance(cursor.getString(2));
        }

        cursor.close();

        return treks;
    }

    public int removeTrek(int trekId){
        ContentResolver contentResolver = mContext.getContentResolver();

        Log.d(TAG, "[removeTrek] trying to remove trekId = " + trekId);

        int deleted = contentResolver.delete(ShakeDBContentProvider.CONTENT_SHAKES_URI,
                ShakeDatabase.COLUMN_TREKID + "=?",
                new String[] {String.valueOf(trekId)});

        contentResolver.delete(ShakeDBContentProvider.CONTENT_TREK_URI,
                ShakeDatabase.COLUMN_ID + "=?",
                new String[] {String.valueOf(trekId)});

        return deleted;
    }

    public Observable<List<TrekModel>> asObservable() {
        return Observable.create(new Observable.OnSubscribe<List<TrekModel>>() {
            @Override
            public void call(Subscriber<? super List<TrekModel>> subscriber) {
                subscriber.onNext(getTrekModels());
                subscriber.onCompleted();
            }
        });
    }

}
