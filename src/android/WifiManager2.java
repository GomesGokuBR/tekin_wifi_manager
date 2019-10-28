package com.tekin.cordova.wifi;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class WifiManager2 {

    private Context context;
    private BroadcastReceiver broadcastReceiverWifiState;
    private BroadcastReceiver broadcastReceiverWifiScan;
    private boolean statusWifi;
    private WifiManager wifiManager;
    private CallbackContext callbackListeWifi;

    public WifiManager2(Context ctx) {
        this.context = ctx;
        this.wifiManager = (WifiManager) ctx.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        this.statusWifi = wifiManager.isWifiEnabled();
        this.observerWifi();
    }

    /**
     * Permet de changer l'etat du wifi
     * @param turn boolean
     * @return boolean
     */
    public boolean toggleWifi (boolean turn) {
        if (turn) {
            this.wifiManager.setWifiEnabled(true);
            this.statusWifi = true;
        } else {
            this.wifiManager.setWifiEnabled(false);
            this.statusWifi = false;
        }
        return this.statusWifi;
    }

    /**
     * Permet d'ecouter le changemment de status du wifi
     */
    private void observerWifi () {
        this.broadcastReceiverWifiState = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);

                switch (wifiState) {
                    case WifiManager.WIFI_STATE_ENABLED: {
                        statusWifi = true;
                        break;
                    }
                    case WifiManager.WIFI_STATE_DISABLED: {
                        statusWifi = false;
                    }
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        this.context.registerReceiver(this.broadcastReceiverWifiState, intentFilter);
    }

    /**
     * Permet de scanner la liste des wifi
     * @param callBackCtx callback de retour de l'app js
     */
    public void scanWifi (CallbackContext callBackCtx) throws JSONException {
        if (this.callbackListeWifi != null) {
            this.callbackListeWifi = callBackCtx;

            this.observerScanWifi();
        } else {
            JSONObject errResponse = new JSONObject();

            errResponse.put("error_code", 1);
            errResponse.put("message", "scan wifi est en cours");

            callBackCtx.error(errResponse);
        }
    }

    /**
     * Permet d'ecouter le resultat du scan wifi
     */
    private void observerScanWifi () {
        this.broadcastReceiverWifiScan = new BroadcastReceiver() {
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
                        callbackListeWifi.error(e.getMessage());
                    }
                }
                try {
                    response.put("wifi", jsonArrayWifi);
                    callbackListeWifi.success(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                    callbackListeWifi.error(e.getMessage());
                }
            }
        };

        this.context.registerReceiver(this.broadcastReceiverWifiScan, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    /**
     * Permet de se connecter un wifi avec son SSID et MDP
     * @param wifi JSONObject
     * @return 1 = Ajour ok | 2 Erreur de config wifi | 3 le wifi n'est pas allumé
     * @throws JSONException error
     */
    public int connectToWifi (JSONObject wifi) throws JSONException {
        if(wifiManager.isWifiEnabled()){

            WifiConfiguration wifiConfiguration = new WifiConfiguration();
            wifiConfiguration.SSID = String.format("\"%s\"", wifi.get("ssid"));
            wifiConfiguration.preSharedKey = String.format("\"%s\"", wifi.get("pwd"));

            // wifiManager.removeNetwork(wifiConfiguration.networkId);
            int netID = wifiManager.addNetwork(wifiConfiguration);
            boolean result = wifiManager.enableNetwork(netID, true);
            wifiManager.reconnect();

            if(result) {
                return 1;
            } else {
                return 2;
            }
        }
        else {
            return 3;
        }
    }

    /**
     * Retourne les droits accorde a l'app de utilliser le wifi
     * @return boolean
     */
    public boolean hasPerm () {
        int perm = this.context.checkSelfPermission(Manifest.permission.ACCESS_WIFI_STATE);
        return perm == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Permet de recuperer l'etat actuel du wifi
     * @return true pour on | false pour off
     */
    public boolean getWifiState () {
        return this.statusWifi;
    }

}
