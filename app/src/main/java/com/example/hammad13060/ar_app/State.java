package com.example.hammad13060.ar_app;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.OkHttpClient;

/**
 * Created by hammad13060 on 16/02/17.
 */
public class State {
    public static boolean sensorServiceRunning = false;
    public static boolean smartNavigation = false;
    //public static final String SERVER_URL = "http://192.168.58.35:3000"; // Hammad
    public static final String SERVER_URL = "http://192.168.59.10:5000"; // GAURAV
    //public static final String SERVER_URL = "http://192.168.0.102:3000";
    public static OkHttpClient okHttpClient = new OkHttpClient();

    public static JSONObject UTMPath = null;
    public static ArrayList<LatLng> path = null;
    public static ArrayList<Boolean> visited = null;
    public static ArrayList<DestinationContainer> steps;
    public static Directions direction = null;
}
