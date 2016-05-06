package com.peanutwolf.googleappmonitor.Fragments;


import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.peanutwolf.googleappmonitor.Database.ShakeDBContentProvider;
import com.peanutwolf.googleappmonitor.DynamicPlotXY;
import com.peanutwolf.googleappmonitor.ExportDataTestActivity;
import com.peanutwolf.googleappmonitor.MapsActivity;
import com.peanutwolf.googleappmonitor.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment implements View.OnClickListener {

    private Button mViewMapButton;
    private Button mStartRoutingButton;
    private Button mSettingsButton;


    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        mViewMapButton = (Button)view.findViewById(R.id.btn_view_main);
        mViewMapButton.setOnClickListener(this);

        mStartRoutingButton = (Button)view.findViewById(R.id.btn_start_main);
        mStartRoutingButton.setOnClickListener(this);

        mSettingsButton = (Button)view.findViewById(R.id.btn_settings_main);
        mSettingsButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        Intent intent;

        switch (v.getId()){
            case R.id.btn_view_main:
                intent = new Intent(getActivity(), MapsActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_start_main:
                intent = new Intent(getActivity(), DynamicPlotXY.class);
                startActivity(intent);
                break;
            case R.id.btn_settings_main:
                intent = new Intent(getActivity(), ExportDataTestActivity.class);
                startActivity(intent);
                break;
            default:
                return;
        }

    }

}
