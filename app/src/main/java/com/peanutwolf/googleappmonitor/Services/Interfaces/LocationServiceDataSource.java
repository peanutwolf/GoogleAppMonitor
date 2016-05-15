package com.peanutwolf.googleappmonitor.Services.Interfaces;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by pEANUTwOLF on 13.05.2016.
 */

public interface LocationServiceDataSource {

    LatLng getLastKnownLatLng();

}
