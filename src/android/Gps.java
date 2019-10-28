package com.tekin.cordova.wifi;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PermissionHelper;

public class Gps {

    private LocationManager locationManager;
    private Context context;

    public Gps(Context ctx) {
        this.context = ctx;
        this.locationManager = (LocationManager) this.context.getSystemService(this.context.LOCATION_SERVICE);
    }

    /**
     * Verifie si l'app possède les autorisation d'utiliser  le gps
     * @return boolean
     */
    public boolean hasPermGps () {
        int perm = this.context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        return perm == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Demande la permisison a l'user d'utiliser la location, qui est necessaire a l'utilisation de
     * wifi
     * @param plugin
     */
    public void requestPermission (CordovaPlugin plugin) {
        plugin.cordova.getThreadPool().execute(() -> {
            String[] permissions = new String[3];
            permissions[0] = Manifest.permission.ACCESS_FINE_LOCATION;
            permissions[1] = Manifest.permission.ACCESS_WIFI_STATE;
            permissions[2] = Manifest.permission.CHANGE_WIFI_STATE;
            PermissionHelper.requestPermissions(plugin, 1, permissions);
        });
    }

    /**
     * Permet de recuperer l'état actuel du gps
     * @return boolean true = on | false = off
     */
    public boolean getGpsState () {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

}
