package com.example.hammad13060.ar_app;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.vision.Frame;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SensorService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, SensorEventListener {
    private static final String TAG = "SensorService";
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private Location mLastIndoorLocation;
    private SensorManager mSensorManager;
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    public static final float ALPHA = 0.2f;

    public static double latitude = 28.544568010241093, longitude = 77.27253307961047;
    public double accuracy = 4.0;

    float gravity[] = {1.0f, 1.0f, 1.0f};
    float geomagnetic[] = {1.0f, 1.0f, 1.0f};
    float gyro[] = {1.0f, 1.0f, 1.0f};
    float dir[] = {1.0f, 1.0f, 1.0f, 1.0f};

    public SensorService() {
        // marking that sensor service is running

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int result = super.onStartCommand(intent, flags, startId);
        return result;

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        State.sensorServiceRunning = true;
        buildGoogleApiClient();
        makeForeground();
        mGoogleApiClient.connect();
        EventBus.getDefault().register(this);
        hitIndoorLocation();
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
        EventBus.getDefault().unregister(this);
        stopForeground(true);
    }

    private void makeForeground() {
        Notification noti = new NotificationCompat.Builder(getApplicationContext())
                .setContentTitle("Your new journey setup is complete")
                .build();
        startForeground(123, noti);
    }

    // on google api connection successfull connection callback
    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "connection successful with the google api client");
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(100);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "location permissions denied");
            return;
        }
        Log.d(TAG, "location permissions granted");
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, (LocationListener) this);
        mLastIndoorLocation = mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        latitude = mLastLocation.getLatitude();
        longitude = mLastLocation.getLongitude();
        Log.d(TAG, "Location: " + mLastLocation.toString());
        sendLocationEvent();
        try {
            fetchMeta();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mSensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);

        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    synchronized void buildGoogleApiClient() {
        Log.d(TAG, "buildig google api client");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
    }

    // google api connection failed callback
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "connection failed : rebuilding google api client");
        buildGoogleApiClient();
    }

    // location listeners callbacks
    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        latitude = mLastLocation.getLatitude();
        longitude = mLastLocation.getLongitude();
        Log.d(TAG, "Location: " + mLastLocation.toString());
        Log.d("GPS ACCURACY", String.valueOf(location.getAccuracy()));
        accuracy = location.getAccuracy();
        sendLocationEvent();
        /*try {
            fetchMeta();
        } catch (JSONException e) {
            e.printStackTrace();
        }*/
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gravity = lowPass(event.values.clone(), new float[3]);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic = lowPass(event.values.clone(), new float[3]);
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            gyro = lowPass(event.values.clone(), new float[3]);
        }

        float R[] = new float[16];
        SensorManager.getRotationMatrix(R, null, gravity, geomagnetic);
        float rhs[] = {gyro[0], gyro[1], gyro[2], 1.0f};
        Matrix.multiplyMV(dir, 0, R, 0, rhs, 0);
        /*try {
            fetchMeta();
        } catch (JSONException e) {
            e.printStackTrace();
        }*/
        Log.d(TAG, "Direction Vector: " + dir[0] + ", " + dir[1] + ", " + dir[2]);
    }

    private void sendLocationEvent() {
        EventBus.getDefault().post(new LocationEvent(mLastLocation.getLatitude(),
                mLastLocation.getLongitude(), mLastLocation.getAltitude()));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void hitIndoorLocation() {
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
                    hitIndoorLocation();
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
                                responseJSON.getDouble("z"),
                                responseJSON.getInt("level"));
                        LatLng latLng = (new UTMRef(responseJSON.getDouble("x"), responseJSON.getDouble("y"), "R", 43)).toLatLng();
                        mLastIndoorLocation = new Location("");
                        mLastIndoorLocation.setLatitude(latLng.latitude);
                        mLastIndoorLocation.setLongitude(latLng.longitude);
                        mLastIndoorLocation.setAltitude(responseJSON.getDouble("z"));
                        //fetchMeta();
                        EventBus.getDefault().post(indoorLocationEvent);
                        EventBus.getDefault().post(new UpdateDirectionsEvent(indoorLocationEvent));
                        /*try {
                            Thread.sleep(2000);
                            hitIndoorLocation();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }*/
                        hitIndoorLocation();
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
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDirectionsUpdate(UpdateDirectionsEvent e) {
        Log.d("step", "step");
        IndoorLocationEvent currentLocation = e.e;
        if (State.smartNavigation) {
            int i = 0;
            double dist = 100000000.0;
            IndoorLocationEvent closestPoint = null;
            int index = 0;
            while(i < State.visited.size()) {
                /*IndoorLocationEvent loc = new IndoorLocationEvent(State.steps.get(i).getX(), State.steps.get(i).getY(), State.steps.get(i).getZ());
                if (!State.visited.get(i) && distance(loc, currentLocation) <= 0.50) {
                    State.visited.set(i, true);
                } else if (!State.visited.get(i)) {
                    break;
                }*/

                IndoorLocationEvent loc = new IndoorLocationEvent(State.steps.get(i).getX(), State.steps.get(i).getY(), State.steps.get(i).getZ());
                if (distance(currentLocation, loc) < dist) {
                    closestPoint = loc;
                    dist = distance(currentLocation, loc);
                    index = i;
                }
                i++;
            }

            double[] d1 = {
                    State.steps.get(index + 1).getX() - State.steps.get(index).getX(),
                    State.steps.get(index + 1).getY() - State.steps.get(index).getY(),
                    State.steps.get(index + 1).getZ() - State.steps.get(index).getZ()
            };

            double[] d2 = {
                    State.steps.get(index + 2).getX() - State.steps.get(index + 1).getX(),
                    State.steps.get(index + 2).getY() - State.steps.get(index + 1).getY(),
                    State.steps.get(index + 2).getZ() - State.steps.get(index + 1).getZ()
            };

            double angle = 57.3248 * cosine(d1, d2);

            Log.d("angle", String.valueOf(angle));

            if (angle <= 55 && angle >= -55) {
                State.direction = Directions.STRAIGHT;
            } else if (angle > 55) {
                State.direction = Directions.LEFT;
            } else if (angle < -55) {
                State.direction = Directions.RIGHT;
            }
            EventBus.getDefault().post(new UpdateDirectionUi());
            /*if (i == State.visited.size()) {
                // TODO: terminate
                EventBus.getDefault().post(new JourneyComplete());
                return;
            }
            DestinationContainer prev = State.steps.get(i-1);
            DestinationContainer intermediateDest = State.steps.get(i);
            DestinationContainer diff = new DestinationContainer("diff", intermediateDest.getX() - prev.getX(),
                    intermediateDest.getY() - prev.getY(),
                    intermediateDest.getZ() - prev.getZ());
            double z = Math.abs(diff.getZ()) - 3.0;
            double x = Math.abs(diff.getX());
            double y = Math.abs(diff.getY());

            if (x > y && x > z) {
                if (diff.getX() < 0) {
                    State.direction = Directions.LEFT;
                } else if (diff.getX() >= 0) {
                    State.direction = Directions.RIGHT;
                }

            } else if (y > x && y > z) {
                if (diff.getY() < 0) {
                    State.direction = Directions.STRAIGHT;
                } else if (diff.getX() >= 0) {
                    State.direction = Directions.STRAIGHT;
                }
            } else if (z > x && z > y){
                State.direction = Directions.UP;

            }*/
        }
    }

    public double distance(IndoorLocationEvent p, IndoorLocationEvent q) {
        double dx = p.x - q.x;
        double dy = p.y - q.y;
        double dz = p.z - q.z;

        return Math.sqrt(dx*dx + dy*dy + dz*dz);
    }

    public double cosine(double[] vec1, double[] vec2) {
        double num = vec1[0] * vec2[0] + vec1[1] * vec2[1] + vec1[2] * vec2[2];

        return num / (norm(vec1) * norm(vec2));
    }

    public double norm(double[] vec) {
        return Math.sqrt(vec[0]*vec[0] + vec[1]*vec[1] + vec[2]*vec[2]);
    }

    protected float[] lowPass( float[] input, float[] output ) {
        if ( output == null ) return input;
        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }


    private void fetchMeta() throws JSONException {
        JSONObject req = new JSONObject();
        if (accuracy <= 2.0) {
            JSONArray directionVector = new JSONArray();
            JSONArray position = new JSONArray();
            directionVector.put(0, dir[0]);
            directionVector.put(1, dir[1]);
            directionVector.put(2, dir[2]);
            position.put(0, mLastLocation.getLatitude());
            position.put(1, mLastLocation.getLongitude());
            position.put(2, mLastLocation.getAltitude());
            req.put("direction", directionVector);
            req.put("origin", position);
        } else {
            JSONArray directionVector = new JSONArray();
            JSONArray position = new JSONArray();
            directionVector.put(0, dir[0]);
            directionVector.put(1, dir[1]);
            directionVector.put(2, dir[2]);
            position.put(0, mLastIndoorLocation.getLatitude());
            position.put(1, mLastIndoorLocation.getLongitude());
            position.put(2, mLastIndoorLocation.getAltitude() + 2.0);
            req.put("direction", directionVector);
            req.put("origin", position);
        }

        RequestBody body = RequestBody.create(JSON, req.toString());

        Request request = new Request.Builder()
                .url(State.SERVER_URL + "/meta")
                .post(body)
                .build();

        State.okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "fetchMeta error on client side");
                try {
                    Thread.sleep(2000);
                    try {
                        fetchMeta();
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                } catch (InterruptedException e2) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    EventBus.getDefault().post(new MetaEvent(new JSONObject(response.body().string())));
                    try {
                        Thread.sleep(2000);
                        fetchMeta();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
