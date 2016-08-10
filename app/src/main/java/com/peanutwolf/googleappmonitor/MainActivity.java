package com.peanutwolf.googleappmonitor;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.peanutwolf.googleappmonitor.Database.ShakeDBContentProvider;
import com.peanutwolf.googleappmonitor.Fragments.MapViewFragment;
import com.peanutwolf.googleappmonitor.Models.ShakePointModel;
import com.peanutwolf.googleappmonitor.Services.Interfaces.ShakeServiceDataSource;
import com.peanutwolf.googleappmonitor.Services.ShakeSensorService;

import org.osmdroid.views.MapView;

import java.util.LinkedList;

public class MainActivity extends AppCompatActivity implements ShakeServiceDataSource<ShakePointModel>, View.OnClickListener, MapViewFragment.iMapCallback {
    public static final String TAG = MainActivity.class.getName();
    private Intent mShakeServiceIntent;
    private ShakeSensorService mShakeSensorService;
    private Button mStartTrackBtn;
    private TextView mShakeSensorAxisTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mStartTrackBtn = (Button) findViewById(R.id.btn_start_track);
        assert mStartTrackBtn != null;
        mStartTrackBtn.setOnClickListener(this);

        mShakeSensorAxisTxt = (TextView) findViewById(R.id.txt_shake_axis);
        if(BuildConfig.DEBUG){
            assert mShakeSensorAxisTxt != null;
            mShakeSensorAxisTxt.setVisibility(View.VISIBLE);
        }

        mShakeServiceIntent = new Intent(getApplicationContext(), ShakeSensorService.class);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(myToolbar);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        bindService(mShakeServiceIntent, mShakeConnector, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
        unbindService(mShakeConnector);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    ServiceConnection mShakeConnector = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mShakeSensorService = ((ShakeSensorService.ShakeSensorBinder)service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    public LinkedList<ShakePointModel> getAccelerationData() {
        if(mShakeSensorService != null){
            LinkedList<ShakePointModel> accelerationData = mShakeSensorService.getAccelerationData();
            if(!accelerationData.isEmpty() && BuildConfig.DEBUG){
                ShakePointModel lastPoint = accelerationData.getLast();
                mShakeSensorAxisTxt.setText("x:"+lastPoint.getAxisAccelerationX()+"\n"+
                                            "y:"+lastPoint.getAxisAccelerationY()+"\n"+
                                            "z:"+lastPoint.getAxisAccelerationZ());
            }
            return accelerationData;
        }
        else {
            return null;
        }
    }

    @Override
    public int getAverageAccelerationData() {
        if(mShakeSensorService != null)
            return mShakeSensorService.getAverageAccelerationData();
        else
            return 0;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear_treks:
                int deletedRows = getContentResolver().delete(ShakeDBContentProvider.CONTENT_URI, null,null);
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Clear treks");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.setMessage(deletedRows + "  rows cleared");
                alertDialog.show();
                break;
            case R.id.action_export_data:
                startActivity(new Intent(this.getApplicationContext(), ExportDataTestActivity.class));
                break;
            case R.id.action_view_routes:
                startActivity(new Intent(this.getApplicationContext(), RouteViewerActivity.class));
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        if(mShakeSensorService.isDataSavingAllowed()){
            mShakeSensorService.setAllowDataSaving(false);
            mStartTrackBtn.setText(getResources().getText(R.string.str_ride_it));
        }else{
            mShakeSensorService.setAllowDataSaving(true);
            mStartTrackBtn.setText(getResources().getText(R.string.str_stop_it));
        }
    }

    @Override
    public void onMapCommand(MapViewFragment.MyMapCommands command) {
        assert mStartTrackBtn != null;
        switch (command){
            case LOCATION_ENABLED:
                mStartTrackBtn.setClickable(true);
                break;
            case LOCATION_DISABLED:
                mStartTrackBtn.setClickable(false);
                break;
        }

    }
}
