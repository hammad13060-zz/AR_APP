package com.example.hammad13060.ar_app;

import android.content.Context;
import android.graphics.Color;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

/**
 * Created by hammad13060 on 16/02/17.
 */
public class MapHandler implements OnMapReadyCallback {
    private Context context;
    private GoogleMap googleMap;
    Marker currentLocationMarker;
    private int zoom;
    private GroundOverlay groundOverlay = null;
    private GoogleMap.OnMapClickListener listner;
    private int floorLevel = -1;
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
        if (State.smartNavigation) {
            googleMap.setBuildingsEnabled(false);
        }
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        //googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        double lat = SensorService.latitude;
        double lon = SensorService.longitude;
        plotPath();
        if (currentLocationMarker != null) {
            currentLocationMarker.remove();
        }
        currentLocationMarker = googleMap.addMarker(
                new MarkerOptions()
                        .position(new LatLng(lat, lon))
                        .title("Your Location")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.dot)));
        zoomIntoPath(lat, lon);

        if (groundOverlay != null) {
            groundOverlay.remove();
        }

        groundOverlay = googleMap.addGroundOverlay(new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.acad2))
                .positionFromBounds(new LatLngBounds(
                        new LatLng(28.544204557084843, 77.27199076000993),
                        new LatLng(28.544786146043666, 77.27309369835217)
                )));
    }

    public void UpdateLocation(double latitude, double longitude) {
        if (googleMap != null) {
            currentLocationMarker.setPosition(new LatLng(latitude, longitude));
            zoomIntoPath(latitude, longitude);
        }
    }

    public void updateLocation(IndoorLocationEvent e) {
        if (googleMap != null) {
            LatLng latLng = (new UTMRef(e.x, e.y, "R", 43)).toLatLng();
            currentLocationMarker.setPosition(latLng);
            //zoomIntoPath(latLng.latitude, latLng.longitude);
            /*if (listner == null) {
                if (groundOverlay != null) {
                    groundOverlay.remove();
                }
                //if (floorLevel != e.level) {
                    floorLevel = e.level;
                    switch (e.level) {
                        case 0:
                            groundOverlay = googleMap.addGroundOverlay(new GroundOverlayOptions()
                                    .image(BitmapDescriptorFactory.fromResource(R.drawable.ground_floor))
                                    .positionFromBounds(new LatLngBounds(
                                            new LatLng(28.544204557084843, 77.27199076000993),
                                            new LatLng(28.544786146043666, 77.27309369835217)
                                    )));
                            break;
                        case 1:
                            groundOverlay = googleMap.addGroundOverlay(new GroundOverlayOptions()
                                    .image(BitmapDescriptorFactory.fromResource(R.drawable.first_floor))
                                    .positionFromBounds(new LatLngBounds(
                                            new LatLng(28.544204557084843, 77.27199076000993),
                                            new LatLng(28.544786146043666, 77.27309369835217)
                                    )));
                            break;
                        case 2:
                            groundOverlay = googleMap.addGroundOverlay(new GroundOverlayOptions()
                                    .image(BitmapDescriptorFactory.fromResource(R.drawable.second_floor))
                                    .positionFromBounds(new LatLngBounds(
                                            new LatLng(28.544204557084843, 77.27199076000993),
                                            new LatLng(28.544786146043666, 77.27309369835217)
                                    )));
                            break;
                        case 3:
                            groundOverlay = googleMap.addGroundOverlay(new GroundOverlayOptions()
                                    .image(BitmapDescriptorFactory.fromResource(R.drawable.third_floor))
                                    .positionFromBounds(new LatLngBounds(
                                            new LatLng(28.544204557084843, 77.27199076000993),
                                            new LatLng(28.544786146043666, 77.27309369835217)
                                    )));
                            break;
                        case 4:
                            groundOverlay = googleMap.addGroundOverlay(new GroundOverlayOptions()
                                    .image(BitmapDescriptorFactory.fromResource(R.drawable.fourth_floor))
                                    .positionFromBounds(new LatLngBounds(
                                            new LatLng(28.544204557084843, 77.27199076000993),
                                            new LatLng(28.544786146043666, 77.27309369835217)
                                    )));
                            break;
                        case 5:
                            groundOverlay = googleMap.addGroundOverlay(new GroundOverlayOptions()
                                    .image(BitmapDescriptorFactory.fromResource(R.drawable.fifth_floor))
                                    .positionFromBounds(new LatLngBounds(
                                            new LatLng(28.544204557084843, 77.27199076000993),
                                            new LatLng(28.544786146043666, 77.27309369835217)
                                    )));
                            break;
                    }
                //}
            }*/
        }
    }

    private void updateOverlay() {

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

    public void plotPath() {
        if (State.smartNavigation) {
            if (listner == null) {
                MarkerOptions options = new MarkerOptions().position(State.path.get(0))
                        .title("Source").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                googleMap.addMarker(options);
            }
            int i;
            for (i = 0; i < State.path.size()-1; i++) {
                googleMap.addPolyline(new PolylineOptions()
                .add(State.path.get(i), State.path.get(i+1))
                                .width(5)
                                .color(Color.BLUE)
                );
            }
            if (listner == null) {
                MarkerOptions options = new MarkerOptions().position(State.path.get(i))
                        .title("Destination").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                googleMap.addMarker(options);
            }
        }
    }
}
