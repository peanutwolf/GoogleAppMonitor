package com.peanutwolf.googleappmonitor;

import android.app.Activity;
import android.os.Bundle;
import com.google.android.gms.maps.GoogleMap;
import com.peanutwolf.googleappmonitor.Fragments.MainFragment;


public class MainActivity extends Activity {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if(savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.main_view_fragment, new MainFragment())
                    .commit();
        }

        return;
    }

}
