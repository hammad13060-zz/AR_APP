package com.example.hammad13060.ar_app;

import android.content.Context;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by hammad13060 on 16/02/17.
 */
public class MapHandler implements OnMapReadyCallback {
    private Context context;
    private GoogleMap googleMap;
    Marker currentLocationMarker;
    private int zoom;
    private GoogleMap.OnMapClickListener listner;
    public MapHandler(Context context, int zoom) {
        this.context = context;
        this.googleMap = null;
        this.zoom = zoom;
        this.listner = null;
    }

    public MapHandler(Context context, GoogleMap.OnMapClickListener listener, int zoom) {
        this.context = context;
        this.googleMap = null;
        this.listner = listener;
        this.zoom = zoom;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        if (listner != null) {
            googleMap.setOnMapClickListener(listner);
            googleMap.getUiSettings().setZoomControlsEnabled(false);
            googleMap.getUiSettings().setScrollGesturesEnabled(false);
        }
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        double lat = SensorService.latitude;
        double lon = SensorService.longitude;
        currentLocationMarker = googleMap.addMarker(
                new MarkerOptions()
                        .position(new LatLng(lat, lon))
                        .title("Your Location"));
                zoomIntoPath(lat, lon);
    }

    public void UpdateLocation(double latitude, double longitude) {
        if (googleMap != null) {
            currentLocationMarker.setPosition(new LatLng(latitude, longitude));
            zoomIntoPath(latitude, longitude);
        }
    }

    private void zoomIntoPath(double lat, double lon) {
        LatLng currentLatLng = new LatLng(lat, lon);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(currentLatLng)
                .zoom(this.zoom)
                .build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public void setOnMapClick(GoogleMap.OnMapClickListener listener) {
        if (googleMap != null) {
            googleMap.setOnMapClickListener(listener);
        }
    }
}
