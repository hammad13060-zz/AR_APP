package com.example.hammad13060.ar_app;

/**
 * Created by hammad13060 on 11/03/17.
 */

public class GetIndoorLocationEvent {
    public double x, y, z;
    public int level;

    public GetIndoorLocationEvent(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public GetIndoorLocationEvent(double x, double y, double z, int level) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.level = level;
    }
}
