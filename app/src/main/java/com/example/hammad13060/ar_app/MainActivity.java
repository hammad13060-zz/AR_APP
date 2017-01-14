package com.example.hammad13060.ar_app;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static final String TAG = "MainActivity";
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buildGoogleApiClient();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
    }

    // on google api connection successfull connection callback
    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "connection successful with the google api client");
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(100);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d(TAG, "location permissions denied");
            return;
        }
        Log.d(TAG, "location permissions granted");
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, (LocationListener) this);
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        Log.d(TAG, "Location: " + mLastLocation.toString());
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
        Log.d(TAG, "Location: " + mLastLocation.toString());
    }
}
