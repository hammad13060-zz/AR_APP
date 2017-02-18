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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import org.greenrobot.eventbus.EventBus;

public class SensorService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, SensorEventListener {
    private static final String TAG = "SensorService";
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private SensorManager mSensorManager;

    public static double latitude = 28.544568010241093, longitude = 77.27253307961047;

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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
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
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        latitude = mLastLocation.getLatitude();
        longitude = mLastLocation.getLongitude();
        Log.d(TAG, "Location: " + mLastLocation.toString());
        sendLocationEvent();

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
        sendLocationEvent();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gravity = event.values;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic = event.values;
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            gyro = event.values;
        }

        float R[] = new float[16];
        SensorManager.getRotationMatrix(R, null, gravity, geomagnetic);
        float rhs[] = {gyro[0], gyro[1], gyro[2], 1.0f};
        Matrix.multiplyMV(dir, 0, R, 0, rhs, 0);
        Log.d(TAG, "Direction Vector: " + dir[0] + ", " + dir[1] + ", " + dir[2]);
    }

    private void sendLocationEvent() {
        EventBus.getDefault().post(new LocationEvent(mLastLocation.getLatitude(),
                mLastLocation.getLongitude(), mLastLocation.getAltitude()));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
