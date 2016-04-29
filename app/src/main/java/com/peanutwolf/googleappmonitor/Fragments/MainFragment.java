package com.peanutwolf.googleappmonitor.Fragments;


import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.peanutwolf.googleappmonitor.DynamicPlotXY;
import com.peanutwolf.googleappmonitor.MapsActivity;
import com.peanutwolf.googleappmonitor.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment implements View.OnClickListener {

    private Button mViewMapButton;
    private Button mStartRoutingButton;


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
            default:
                break;
        }

    }
}
