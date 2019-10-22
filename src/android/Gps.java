package com.tekin.cordova.wifi;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;

public class Gps extends CordovaPlugin {

    private CallbackContext promiseCallbackContext;
    private LocationManager locationManager;
    private Context context;


    @RequiresApi(api = Build.VERSION_CODES.M)
    public Gps() {
        Log.d("domii2", "test grant ");
        this.context = this.cordova.getActivity().getApplicationContext();
        int perm = this.context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        if (perm != PackageManager.PERMISSION_GRANTED) {
            Log.d("domii2", "not granted");
        }
    }

    public void hasPermGps () {

    }
}
