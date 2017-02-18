package com.example.hammad13060.ar_app;

/**
 * Created by hammad13060 on 16/02/17.
 */
public class LocationEvent {
    private double latitude, longitude, altitide;

    public LocationEvent(double latitude, double longitude, double altitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitide = altitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getAltitide() {
        return altitide;
    }
}
