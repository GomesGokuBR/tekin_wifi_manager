package com.tekin.plugin;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.net.NetworkSpecifier;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.v4.content.PermissionChecker;
import android.util.Log;
import android.widget.Toast;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.LOG;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import javax.security.auth.callback.Callback;

/**
 * This class echoes a string called from JavaScript.
 */
public class TekinWifi extends CordovaPlugin {

  public static final int CONTINUE = 1;
  private boolean permissionStateWifi = false;
  private ArrayList<String> arrayList = new ArrayList<>();
  private WifiManager wifiManager;
  //private BroadcastReceiver wifiScanReceiver;

  private CallbackContext listeWifiContext;
  private LocationManager locationManager;

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    this.hasWifiStatePermission();
    Log.d("domii2", action);
    switch (action) {
      case "toggleWifi":
        if (this.permissionStateWifi) this.toggleWifi(args, callbackContext);
        return true;
      case "getListeWifi":
        if (this.permissionStateWifi) this.listWifi(callbackContext);
        return true;
      case "connect":
        if (this.permissionStateWifi) this.connect(callbackContext, args);
        return true;
      case "hasPermissions":
        if (locationManager == null) {
          Context context = this.cordova.getContext();
          this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
          this.hasPermissions(callbackContext);
          return true;
        } else {
          this.hasPermissions(callbackContext);
          return true;
        }
    }
    return false;
  }

  /**
   * Perùet de activer/desactiver le wifi du device
   *
   * @param args
   * @param callbackContext
   * @throws JSONException
   */
  private void toggleWifi(JSONArray args, CallbackContext callbackContext) throws JSONException {
    if (this.permissionStateWifi) {
      boolean turnWifi = args.getBoolean(0);
      JSONObject response = new JSONObject();
      if (turnWifi) {
        this.wifiManager.setWifiEnabled(true);
        response.put("toggleWifi", true);
        callbackContext.success(response);
      } else {
        this.wifiManager.setWifiEnabled(false);
        response.put("toggleWifi", false);
        callbackContext.success(response);
      }
    }
  }

  /**
   * Permet de verifier si l'app possède les permissions nécessaires pour controller le wifi
   * @param callbackContext
   * @throws JSONException
   */
  private void hasPermissions (CallbackContext callbackContext) throws JSONException {
    Context context = this.cordova.getActivity().getApplicationContext();
    int hasPerm = PermissionChecker.checkSelfPermission (context, Manifest.permission.ACCESS_FINE_LOCATION);
    boolean wifiState = wifiManager.isWifiEnabled();
    boolean statusGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    JSONObject response = new JSONObject();
    response.put("hasPermissions", hasPerm);
    response.put("location", statusGps);
    response.put("wiifiState", wifiState);
    callbackContext.success(response);
  }

  /**
   * Permet de lister la listes des reseaux wifi visibles par le device=
   *
   * @param callbackContext context de retour d'appel de function
   * @throws JSONException
   */
  private void listWifi(CallbackContext callbackContext) throws JSONException {
    if (this.permissionStateWifi) {
      arrayList.clear();
      this.listeWifiContext = callbackContext;
      cordova.getContext().registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
      if (!PermissionHelper.hasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
        PermissionHelper.requestPermission(this, CONTINUE, Manifest.permission.ACCESS_FINE_LOCATION);
      } else {
        wifiManager.startScan();
      }
    }
  }


  BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {

      JSONObject response = new JSONObject();
      JSONArray jsonArrayWifi = new JSONArray();

      List<ScanResult> results = wifiManager.getScanResults();
      context.unregisterReceiver(this);

      for (ScanResult scanResult : results) {
        JSONObject wifiJson = new JSONObject();
        try {
          wifiJson.put("ssid", scanResult.SSID);
          wifiJson.put("mac", scanResult.BSSID);
          wifiJson.put("puissance", scanResult.level);
          jsonArrayWifi.put(wifiJson);
        } catch (JSONException e) {
          // LOG.d("wifi_tek", e.getMessage());
          listeWifiContext.error(e.getMessage());
        }
      }
      try {
        response.put("wifi", jsonArrayWifi);
        listeWifiContext.success(response);
      } catch (JSONException e) {
        e.printStackTrace();
        listeWifiContext.error(e.getMessage());
      }
    }
  };

  public void connect(CallbackContext callbackContext, JSONArray args) throws JSONException{
    JSONObject wifi = (JSONObject) args.get(0);
    JSONObject response = new JSONObject();

    if(wifiManager.isWifiEnabled()){

      WifiConfiguration wifiConfiguration = new WifiConfiguration();
      wifiConfiguration.SSID = String.format("\"%s\"", wifi.get("ssid"));
      wifiConfiguration.preSharedKey = String.format("\"%s\"", wifi.get("pwd"));

      wifiManager.disconnect();
      int netID = wifiManager.addNetwork(wifiConfiguration);
      boolean result = wifiManager.enableNetwork(netID, true);
      // Toast.makeText(cordova.getContext(), "Connection " + wifiConfiguration.SSID + " " + result, Toast.LENGTH_SHORT).show();
      wifiManager.reconnect();
      if(result) response.put("connect", true);
      else response.put("connect", false);

      callbackContext.success(response);
    }
    else {
      Toast.makeText(cordova.getContext(), "Wifi n'est pas allumé", Toast.LENGTH_SHORT).show();
      response.put("connect", false);
      response.put("message", "Wifi n'est pas allumé");
      response.put("code_error", "undefined");
      callbackContext.error(response);
    }
  }

  /**
   * Permet de Verifier si le device possede les autorisations necessares pour la gestions de wifi.
   */
  private void hasWifiStatePermission() {
    if (!PermissionHelper.hasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
      String[] permissions = new String[3];
      permissions[0] = Manifest.permission.ACCESS_FINE_LOCATION;
      permissions[1] = Manifest.permission.ACCESS_WIFI_STATE;
      permissions[2] = Manifest.permission.CHANGE_WIFI_STATE;
      PermissionHelper.requestPermissions(this, CONTINUE, permissions);
    }
    else {
      this.permissionStateWifi = true;
    }
    Context mContext = cordova.getContext();
    this.wifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
  }

  @Override
  public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
    super.onRequestPermissionResult(requestCode, permissions, grantResults);
    if(grantResults[0] == 0){
      this.permissionStateWifi = true;
    }
  }
}
