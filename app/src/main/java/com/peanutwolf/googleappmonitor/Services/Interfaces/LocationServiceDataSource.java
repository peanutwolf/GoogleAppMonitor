package com.peanutwolf.googleappmonitor.Services.Interfaces;

import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by pEANUTwOLF on 13.05.2016.
 */

public interface LocationServiceDataSource {

    @NonNull
    LatLng getLastKnownLatLng();

    @Nullable
    Location getLastKnownLocation();

    void setLocationListener(@NonNull LocationListener locationListener);

    @NonNull
    float getSpeed();
}
