package com.tekin.plugin;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.LOG;
import org.apache.cordova.PermissionHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * This class echoes a string called from JavaScript.
 */
public class TekinWifi extends CordovaPlugin {

  public static final int CONTINUE = 1;
  private boolean permissionStateWifi = false;
  private ArrayList<String> arrayList = new ArrayList<>();
  private WifiManager wifiManager;
  private BroadcastReceiver wifiScanReceiver;

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    this.hasWifiStatePermission();
    if (action.equals("toggleWifi")) {
      if(this.permissionStateWifi) this.toggleWifi(args, callbackContext);
      return true;
    }
    return false;
  }

  /**
   * Perùet de activer/desactiver le wifi du device
   * @param args
   * @param callbackContext
   * @throws JSONException
   */
    private void toggleWifi(JSONArray args, CallbackContext callbackContext) throws JSONException {
      if(this.permissionStateWifi){
        boolean turnWifi = args.getBoolean(0);
        Context mContext = cordova.getContext();
        this.wifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        JSONObject response = new JSONObject();
        if(turnWifi) {
          this.wifiManager.setWifiEnabled(true);
          response.put("toggleWifi", true);
          this.listWifi();
          callbackContext.success(response);
        }else {
          this.wifiManager.setWifiEnabled(false);
          response.put("toggleWifi", false);
          callbackContext.success(response);
        }
      }
    }

  /**
   * Permet de lister la listes des reseaux wifi visibles par le device=
   * @param args
   * @param callbackContext
   * @throws JSONException
   */
    private void listWifi() throws JSONException {
      if(this.permissionStateWifi){
        arrayList.clear();

        cordova.getContext().registerReceiver(wifiReceiver, new IntentFilter(wifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        if(!PermissionHelper.hasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)){
          PermissionHelper.requestPermission(this, CONTINUE, Manifest.permission.ACCESS_FINE_LOCATION);
          Toast.makeText(cordova.getContext(), "Request permission ACCESS_FINE_LOCATION", Toast.LENGTH_LONG).show();
        }
        else {
          wifiManager.startScan();
          Toast.makeText(cordova.getContext(), "Scanning wifi ...", Toast.LENGTH_LONG).show();
        }
      }
    }


    BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        List<ScanResult> results = wifiManager.getScanResults();
        context.unregisterReceiver(this);
        for (ScanResult scanResult: results){
          if(scanResult.SSID.equals("Hacare_hotspot")){
            Log.d("tek_wifi", "Wifi trouve "+scanResult.SSID);
            WifiConfiguration wifiConfiguration = new WifiConfiguration();
            wifiConfiguration.SSID = String.format("\"%s\"", scanResult.SSID);
            wifiConfiguration.preSharedKey = String.format("\"%s\"", "tekin_password");

            int netId = wifiManager.addNetwork(wifiConfiguration);
            wifiManager.disconnect();
            wifiManager.enableNetwork(netId, true);
            wifiManager.reconnect();
          }
        }
        //wifiManager.startScan();
      }
    };


    private void scanSuccess() {
      List<ScanResult> results = wifiManager.getScanResults();
    }

  private void scanFailure() {
    // handle failure: new scan did NOT succeed
    // consider using old scan results: these are the OLD results!
    List<ScanResult> results = wifiManager.getScanResults();
  }

  private void unregisterReceiverWifi(Context mContext){
     mContext.unregisterReceiver(this.wifiScanReceiver);
  }

  private void hasWifiStatePermission(){
      if(PermissionHelper.hasPermission(this, Manifest.permission.ACCESS_WIFI_STATE)){
        this.permissionStateWifi = true;
        LOG.d("tek_wifi", "permison wifi state true");
      }
      else {
        LOG.d("tek_wifi", "permison wifi state false");
        PermissionHelper.requestPermission(this, CONTINUE, Manifest.permission.ACCESS_WIFI_STATE);
      }
    }
}
