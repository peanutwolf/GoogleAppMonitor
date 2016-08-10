package com.peanutwolf.googleappmonitor.Fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.peanutwolf.googleappmonitor.R;
import com.peanutwolf.googleappmonitor.Services.Interfaces.LocationServiceDataSource;
import com.peanutwolf.googleappmonitor.Utilities.DynamicDataSourceLoop;
import com.peanutwolf.googleappmonitor.Utilities.GoogleLocationProvider;

import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by vigursky on 04.08.2016.
 */
public class MapViewFragment extends Fragment implements DynamicDataSourceLoop.iCallback, OpenStreetMapConstants{

    public static final String TAG = MapViewFragment.class.getName();
    private LocationServiceDataSource mLocationDataSource;
    private SharedPreferences mPrefs;
    private MyLocationNewOverlay mLocationOverlay;
    private MapView mMapView;
    private ScaleBarOverlay mScaleBarOverlay;
    private RotationGestureOverlay mRotationGestureOverlay;
    private CompassOverlay mCompassOverlay;
    private ImageButton mCenterMapBtn;
    private ImageButton mFollowMeBnt;
    private iMapCallback mMapCallback;

    public enum MyMapCommands{
        LOCATION_ENABLED,
        LOCATION_DISABLED
    }

    public interface iMapCallback{
        void onMapCommand(MyMapCommands command);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_main_mapview, container, false);
        mMapView = (MapView) v.findViewById(R.id.mapview);

        mCenterMapBtn = (ImageButton) v.findViewById(R.id.ic_center_map);
        mFollowMeBnt = (ImageButton) v.findViewById(R.id.ic_follow_me);
        mMapView.setTileSource(TileSourceFactory.CYCLEMAP);

        mCenterMapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "centerMap clicked ");

                GeoPoint myPosition = mLocationOverlay.getMyLocation();

                if (myPosition != null) {
                    mMapView.getController().animateTo(myPosition);
                }
            }
        });

        mFollowMeBnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mLocationOverlay.isMyLocationEnabled()){
                    Log.i(TAG, "MyLocationEnabled, FollowMeEnabled ");
                    mLocationOverlay.enableMyLocation();
                    mLocationOverlay.enableFollowLocation();
                }else{
                    Log.i(TAG, "MyLocationDisabled, FollowMeDisabled ");
                    mLocationOverlay.disableMyLocation();
                    mLocationOverlay.disableFollowLocation();
                }
            }
        });

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final Context context = this.getActivity();
        final DisplayMetrics dm = context.getResources().getDisplayMetrics();



        mPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        this.mLocationOverlay = new MyLocationNewOverlay(new GoogleLocationProvider(context),mMapView);

        mScaleBarOverlay = new ScaleBarOverlay(mMapView);
        mScaleBarOverlay.setCentred(true);
        mScaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10);

        mRotationGestureOverlay = new RotationGestureOverlay(context, mMapView);
        mRotationGestureOverlay.setEnabled(false);

        mMapView.setBuiltInZoomControls(false);
        mMapView.setMultiTouchControls(true);
        mMapView.getOverlays().clear();
        mMapView.getOverlays().add(this.mLocationOverlay);
        mMapView.getOverlays().add(this.mScaleBarOverlay);
        mMapView.getOverlays().add(this.mRotationGestureOverlay);

        mMapView.getController().setZoom(mPrefs.getInt(PREFS_ZOOM_LEVEL, 1));
        mMapView.scrollTo(mPrefs.getInt(PREFS_SCROLL_X, 0), mPrefs.getInt(PREFS_SCROLL_Y, 0));

        //sorry for the spaghetti code this is to filter out the compass on api 8
        //Note: the compass overlay causes issues on API 8 devices. See https://github.com/osmdroid/osmdroid/issues/218
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
            mCompassOverlay = new CompassOverlay(context, new InternalCompassOrientationProvider(context),
                    mMapView);
            mCompassOverlay.enableCompass();
            mMapView.getOverlays().add(this.mCompassOverlay);
        }

        if(this.getActivity() instanceof iMapCallback){
            mMapCallback = (iMapCallback)this.getActivity();
        }

        mLocationOverlay.runOnFirstFix(new Runnable() {
            @Override
            public void run() {
                MapViewFragment.this.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mMapCallback.onMapCommand(MyMapCommands.LOCATION_ENABLED);
                    }
                });
            }
        });
    }

    @Override
    public void onPause() {
        final SharedPreferences.Editor edit = mPrefs.edit();
        edit.putString(PREFS_TILE_SOURCE, mMapView.getTileProvider().getTileSource().name());
        edit.putInt(PREFS_SCROLL_X, mMapView.getScrollX());
        edit.putInt(PREFS_SCROLL_Y, mMapView.getScrollY());
        edit.putInt(PREFS_ZOOM_LEVEL, mMapView.getZoomLevel());
        edit.putBoolean(PREFS_SHOW_LOCATION, mLocationOverlay.isMyLocationEnabled());

        //sorry for the spaghetti code this is to filter out the compass on api 8
        //Note: the compass overlay causes issues on API 8 devices. See https://github.com/osmdroid/osmdroid/issues/218
        if (mCompassOverlay!=null) {
            edit.putBoolean(PREFS_SHOW_COMPASS, mCompassOverlay.isCompassEnabled());
            this.mCompassOverlay.disableCompass();
        }
        edit.commit();

        this.mLocationOverlay.disableMyLocation();
        this.mLocationOverlay.disableFollowLocation();

        super.onPause();
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        //this part terminates all of the overlays and background threads for osmdroid
        //only needed when you programmatically create the map
//        mMapView.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        final String tileSourceName = mPrefs.getString(PREFS_TILE_SOURCE,
                TileSourceFactory.DEFAULT_TILE_SOURCE.name());
        try {
            final ITileSource tileSource = TileSourceFactory.getTileSource(tileSourceName);
            mMapView.setTileSource(tileSource);
        } catch (final IllegalArgumentException e) {
            mMapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        }
        if (mPrefs.getBoolean(PREFS_SHOW_LOCATION, false)) {
            this.mLocationOverlay.enableMyLocation();
        }

        //sorry for the spaghetti code this is to filter out the compass on api 8
        //Note: the compass overlay causes issues on API 8 devices. See https://github.com/osmdroid/osmdroid/issues/218
        if (mPrefs.getBoolean(PREFS_SHOW_COMPASS, false)) {
            if (mCompassOverlay!=null)
                this.mCompassOverlay.enableCompass();
        }
    }

    public void drawTrack(List<GeoPoint> trackPoints){
        Polyline route = new Polyline(this.getActivity());
        route.setPoints(trackPoints);
        route.setVisible(true);

        mMapView.getOverlays().add(route);
    }

    @Override
    public void onUpdate() {

    }


}
