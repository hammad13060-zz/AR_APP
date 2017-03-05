package com.example.hammad13060.ar_app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NavigationSetupActivity extends AppCompatActivity {

    private boolean destinationSelected = false;
    private DestinationContainer destinationContainer;
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_setup);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    public void onSelectDestination(View view) {
        DestinationOptionsDialog.showDialog(this);
    }

    public void onStartJourney(View view) {
        if (destinationSelected) {
            try {
                JSONObject wifiSignalJSON = WifiUtil.getWifiSignalStrengthJSON(getApplicationContext());
                String wifiSignalJSONString = wifiSignalJSON.toString();
                RequestBody body = RequestBody.create(JSON, wifiSignalJSONString);

                Request request = new Request.Builder()
                        .url(State.SERVER_URL + "/indoorLocation")
                        .post(body)
                        .build();
                State.okHttpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.d(NavigationSetupActivity.class.getName(), "Client Error while hitting /indoorLocation");
                        // Toast.makeText(getApplicationContext(), "Client Error while hitting /indoorLocation", Toast.LENGTH_SHORT);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Log.d(NavigationSetupActivity.class.getName(), "Success while hitting /indoorLocation");
                        // Toast.makeText(getApplicationContext(), "Success while hitting /indoorLocation", Toast.LENGTH_SHORT);
                        try {
                            JSONObject responseJSON = new JSONObject(response.body().string());
                            IndoorLocationEvent indoorLocationEvent = new IndoorLocationEvent(
                                    responseJSON.getDouble("x"),
                                    responseJSON.getDouble("y"),
                                    responseJSON.getDouble("z"));
                            EventBus.getDefault().post(indoorLocationEvent);
                        } catch (JSONException e) {
                            Log.d(NavigationSetupActivity.class.getName(), "Invalid Response from /indoorLocation");
                            //Toast.makeText(getApplicationContext(), "Invalid Response from /indoorLocation", Toast.LENGTH_SHORT);
                            e.printStackTrace();
                        }
                    }
                });
            } catch (JSONException e) {
                Log.d(NavigationSetupActivity.class.getName(), "Cannot create wifi utils JSON");
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Please select a destination", Toast.LENGTH_SHORT);
        }
    }

    public void onDestinationSelected(DestinationContainer container) {
        TextView textView = (TextView) findViewById(R.id.destination_text_view);
        Log.d(NavigationSetupActivity.class.getName(), "Destination: " + container.getName());
        textView.setText("Destination: " + container.getName());
        destinationContainer = container;
        destinationSelected = true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getNavigationPath(IndoorLocationEvent e) {
        JSONArray source = new JSONArray();
        JSONArray destination = new JSONArray();
        try {
            source.put(e.x);
            source.put(e.y);
            source.put(e.z);

            destination.put(destinationContainer.getX());
            destination.put(destinationContainer.getY());
            destination.put(destinationContainer.getZ());

            JSONObject obj = new JSONObject();
            obj.put("source", source);
            obj.put("destination", destination);

            RequestBody body = RequestBody.create(JSON, obj.toString());

            Request request = new Request.Builder()
                    .url(State.SERVER_URL + "/smartNavigation")
                    .post(body)
                    .build();

            State.okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d(NavigationSetupActivity.class.getName(), "Client Error while hitting /smartNavigation");
                    //Toast.makeText(getApplicationContext(), "Client Error while hitting /smartNavigation", Toast.LENGTH_SHORT);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.d(NavigationSetupActivity.class.getName(), "Success while hitting /smartNavigation");
                    //Toast.makeText(getApplicationContext(), "Success while hitting /smartNavigation", Toast.LENGTH_SHORT);
                    // TODO: Parse JSON, Set States, Launch MapActivity, Kill this current activity
                    try {
                        State.UTMPath = new JSONObject(response.body().string());
                        State.smartNavigation = true;

                        JSONArray pathArray = State.UTMPath.getJSONArray("path");
                        State.path = new ArrayList<LatLng>();
                        for (int i = 0; i < pathArray.length(); i++) {
                            JSONArray loc = pathArray.getJSONArray(i);
                            LatLng latLng = (new UTMRef(loc.getDouble(0), loc.getDouble(1), "R", 43)).toLatLng();
                            State.path.add(latLng);
                        }
                        Intent i = new Intent(getApplicationContext(), MapActivity.class);
                        startActivity(i);
                        NavigationSetupActivity.this.finish();
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                }
            });


        } catch (JSONException e1) {
            e1.printStackTrace();
        }
    }


}
