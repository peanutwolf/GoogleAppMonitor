package com.peanutwolf.googleappmonitor.Models;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.peanutwolf.googleappmonitor.Database.ShakeDBContentProvider;
import com.peanutwolf.googleappmonitor.Database.ShakeDatabase;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;


/**
 * Created by vigursky on 21.09.2016.
 */
public class ShakePointDAO {
    public static final String TAG = ShakePointDAO.class.getSimpleName();
    private Context mContext;

    public ShakePointDAO(Context context){
        this.mContext = context;
    }

    public List<ShakePointPOJO> getShakePoints(int trekId){
        List<ShakePointPOJO> shakes = new ArrayList<>();
        ContentResolver contentResolver = mContext.getContentResolver();

        Log.d(TAG, "[getShakePoints] Loading points on Thread="+Thread.currentThread().getName());

        Cursor cursor = contentResolver.query(ShakeDBContentProvider.CONTENT_SHAKES_URI,
                null, ShakeDatabase.COLUMN_TREKID + "=?", new String[] {String.valueOf(trekId)}, null);

        while(cursor.moveToNext()){
            ShakePointPOJO shakePoint = new ShakePointPOJO();
            shakePoint.dataToModel(cursor);
            shakes.add(shakePoint);
        }
        cursor.close();
        Log.d(TAG, "[getShakePoints] Points loaded!");

        return shakes;
    }

    public Observable<List<ShakePointPOJO>> asObservable(final int trekId) {
        return Observable.create(new Observable.OnSubscribe<List<ShakePointPOJO>>() {
            @Override
            public void call(Subscriber<? super List<ShakePointPOJO>> subscriber) {
                subscriber.onNext(getShakePoints(trekId));
                subscriber.unsubscribe();
            }
        });
    }

}
