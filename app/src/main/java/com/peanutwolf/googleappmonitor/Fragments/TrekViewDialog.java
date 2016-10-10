package com.peanutwolf.googleappmonitor.Fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.CursorLoader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import com.peanutwolf.googleappmonitor.Database.ShakeDBContentProvider;
import com.peanutwolf.googleappmonitor.Database.ShakeDatabase;
import com.peanutwolf.googleappmonitor.Models.ShakePointDAO;
import com.peanutwolf.googleappmonitor.Models.ShakePointPOJO;
import com.peanutwolf.googleappmonitor.R;

import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by vigursky on 23.08.2016.
 */
public class TrekViewDialog extends DialogFragment implements OpenStreetMapConstants{

    public static final String TAG = TrekViewDialog.class.getSimpleName();
    private static final String TREK_ID_ARG = "TREK_ID";
    private static final int SHAKE_LOADER_ID = 1;
    private Button dismissWindow;
    private MapView mMapView;
    private List<ShakePointPOJO> mRoutePointsList;
    private SharedPreferences mPrefs;
    private Polyline mTrekPolyline;
    private ShakePointDAO mShakePointsDAO;
    private Observable<List<ShakePointPOJO>> mShakePointsObserver;
    private Subscription mShakeSubscriptionHandle;

    public TrekViewDialog(){

    }

    public static TrekViewDialog newInstance(){
        TrekViewDialog dialog = new TrekViewDialog();
        return dialog;
    }

    public static TrekViewDialog newInstance(Integer trekID){
        TrekViewDialog dialog = new TrekViewDialog();

        Bundle args = new Bundle();
        args.putInt(TREK_ID_ARG, trekID);
        dialog.setArguments(args);

        dialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.RouteViewDialog);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_route_view, container);

        mMapView = (MapView) view.findViewById(R.id.mapview_dialog);

        dismissWindow = (Button)view.findViewById(R.id.btn_cancel_route_dialog);
        dismissWindow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        mShakePointsDAO = new ShakePointDAO(this.getContext());

        Bundle args = this.getArguments();

        mShakePointsObserver = mShakePointsDAO.asObservable(args.getInt(TREK_ID_ARG, 0));
        mShakeSubscriptionHandle = mShakePointsObserver.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<List<ShakePointPOJO>>() {
                    @Override
                    public void call(List<ShakePointPOJO> shakePoints) {
                        Log.d(TAG, "[onNext] Loaded ShakePoints size=" + shakePoints.size());
                        TrekViewDialog.this.setRoutePoints(shakePoints);
                        if (TrekViewDialog.this.mMapView.isLayoutOccurred()) {
                            drawTrekPoints(shakePoints, true);
                        }
                    }
                });

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mPrefs = this.getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public void onStart(){
        super.onStart();

        mMapView.getController().setZoom(mPrefs.getInt(PREFS_ZOOM_LEVEL, 1));
        mMapView.scrollTo(mPrefs.getInt(PREFS_SCROLL_X, 0), mPrefs.getInt(PREFS_SCROLL_Y, 0));

        mMapView.addOnFirstLayoutListener(new MapView.OnFirstLayoutListener() {
            @Override
            public void onFirstLayout(View v, int left, int top, int right, int bottom) {
                if (TrekViewDialog.this.mRoutePointsList != null) {
                    TrekViewDialog.this.drawTrekPoints(TrekViewDialog.this.mRoutePointsList, true);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mShakeSubscriptionHandle.unsubscribe();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        return dialog;
    }

    public void setRoutePoints(List<ShakePointPOJO> routePointsList){
        this.mRoutePointsList = routePointsList;
    }

    @NonNull
    private Polyline prepareTrack(@NonNull List<? extends ShakePointPOJO> routePointsList){
        Polyline track = new Polyline(this.getActivity());
        List<GeoPoint> geoPoints = new ArrayList<>();

        for(ShakePointPOJO shakePointModel : routePointsList){
            GeoPoint geoPoint = new GeoPoint(shakePointModel.getCurrentLatitude(), shakePointModel.getCurrentLongitude());
            geoPoints.add(geoPoint);
        }

        track.setGeodesic(true);
        track.setPoints(geoPoints);
        return track;
    }

    private void drawTrekPoints(@NonNull List<? extends ShakePointPOJO> pointsList, boolean zoomToTrek){

        if(zoomToTrek == true){
            this.zoomToTrekPoints(pointsList);
        }

        mTrekPolyline = TrekViewDialog.this.prepareTrack(pointsList);
        mTrekPolyline.setVisible(true);
//        mTrekPolyline.setColor(Color.GREEN);
        Paint paint = mTrekPolyline.getPaint();
        Bitmap bitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.RED);
//        paint.setShader(new BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT));
        Shader shader1 = new LinearGradient(0, 0, 0, 10 /*canvas height*/, new int[]{Color.GREEN}, new float[]{0}, Shader.TileMode.MIRROR );
        Shader shader2 = new LinearGradient(0, 10, 0, 20 /*canvas height*/, new int[]{Color.GREEN}, new float[]{0}, Shader.TileMode.MIRROR );
        //paint.setShader(new LinearGradient(0, 0, 0, 128 /*canvas height*/, new int[]{Color.GREEN, Color.BLUE, Color.BLACK, Color.WHITE}, new float[]{0,0.5f,.55f,1}, Shader.TileMode.MIRROR ));
        mMapView.getOverlays().add(mTrekPolyline);
    }

    private void zoomToTrekPoints(List<? extends ShakePointPOJO> pointsList){
        BoundingBoxE6 trackBoundingBox = TrekViewDialog.this.calcTrackBoundingBox(pointsList);
        mMapView.zoomToBoundingBox(trackBoundingBox, false);
    }

    private BoundingBoxE6 calcTrackBoundingBox(@NonNull List<? extends ShakePointPOJO> routePointsList){
        double north, east, south, west;
        BoundingBoxE6 trackBox;

        if(routePointsList.size() == 0){
            return null;
        }
        north = south = routePointsList.get(0).getCurrentLatitude();
        east = west = routePointsList.get(0).getCurrentLongitude();

        for(ShakePointPOJO shakePointModel : routePointsList){
            if(north < shakePointModel.getCurrentLatitude()){
                north = shakePointModel.getCurrentLatitude();
            }
            if(south > shakePointModel.getCurrentLatitude()){
                south = shakePointModel.getCurrentLatitude();
            }
            if(west > shakePointModel.getCurrentLongitude()){
                west = shakePointModel.getCurrentLongitude();
            }
            if(east < shakePointModel.getCurrentLongitude()){
                east = shakePointModel.getCurrentLongitude();
            }
        }

        trackBox = new BoundingBoxE6(north, east, south, west);

        return trackBox;
    }

}

