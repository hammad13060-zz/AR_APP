package com.example.hammad13060.ar_app;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by hammad13060 on 03/03/17.
 */

public class WifiUtil {
    public static JSONObject getWifiSignalStrengthJSON(Context context) throws JSONException {
        JSONObject wifiStrengthObject = new JSONObject();
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        for (int h = 0; h < 10; h++){
            wifiManager.startScan();
        }
        List<ScanResult> list = wifiManager.getScanResults();
        String[] wifis = new String[list.size()];
        for(int i = 0; i < list.size(); i++){
            wifis[i] = ((list.get(i)).toString());
        }
        JSONObject rssi_object = new JSONObject();
        for(String eachWifi: wifis) {
            String[] temp = eachWifi.split(",");
            String key = temp[1].trim().split(" ")[1].trim();
            String value = temp[3].trim().split(":")[1].trim();
            rssi_object.put(key, Float.parseFloat(value));
        }
        wifiStrengthObject.put("rssi", rssi_object);
        return wifiStrengthObject;
    }
}
