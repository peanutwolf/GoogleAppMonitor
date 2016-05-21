package com.peanutwolf.googleappmonitor.Services.Interfaces;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by pEANUTwOLF on 13.05.2016.
 */

public interface LocationServiceDataSource {

    @NonNull
    LatLng getLastKnownLatLng();

}
