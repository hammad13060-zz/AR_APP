package com.example.hammad13060.ar_app;

import com.google.android.gms.maps.model.LatLng;

import org.unbescape.java.JavaEscape;

/**
 * Created by hammad13060 on 04/03/17.
 */


 // Inspired from http://www.jstott.me.uk/jscoord/

public class UTMRef {
    private double easting, northing, lngZone;
    private String latZone;

    UTMRef(double easting, double northing, String latZone, double lngZone) {
        this.easting = easting;
        this.northing = northing;
        this.latZone = latZone;
        this.lngZone = lngZone;
    }

    private int ord(String x) {
        char c = x.charAt(0);
        int i;
        for (i = 0; i < 256; i++) {
            String h = Integer.toHexString(i);
            if (h.length() == 1) {
                h = "0" + h;
            }
            h = "%" + h;
            // TODO: UNESCAPE
            h = JavaEscape.escapeJava(h);
            if (h == String.valueOf(c)) {
                break;
            }
        }
        return i;
    }
    public LatLng toLatLng() {
        RefEll wgs84 = new RefEll(6378137.0, 6356752.314);

        double UTM_F0   = 0.9996;
        double a = wgs84.maj;
        double eSquared = wgs84.ecc;
        double ePrimeSquared = eSquared / (1.0 - eSquared);
        double e1 = (1 - Math.sqrt(1 - eSquared)) / (1 + Math.sqrt(1 - eSquared));
        double x = this.easting - 500000.0;;
        double y = this.northing;
        double zoneNumber = this.lngZone;
        String zoneLetter = this.latZone;

        double longitudeOrigin = (zoneNumber - 1.0) * 6.0 - 180.0 + 3.0;

        // Correct y for southern hemisphere
        if ((ord(zoneLetter) - ord("N")) < 0) {
            y -= 10000000.0;
        }

        double m = y / UTM_F0;
        double mu =
                m
                        / (a
                        * (1.0
                        - eSquared / 4.0
                        - 3.0 * eSquared * eSquared / 64.0
                        - 5.0
                        * Math.pow(eSquared, 3.0)
                        / 256.0));

        double phi1Rad =
                mu
                        + (3.0 * e1 / 2.0 - 27.0 * Math.pow(e1, 3.0) / 32.0) * Math.sin(2.0 * mu)
                        + (21.0 * e1 * e1 / 16.0 - 55.0 * Math.pow(e1, 4.0) / 32.0)
                        * Math.sin(4.0 * mu)
                        + (151.0 * Math.pow(e1, 3.0) / 96.0) * Math.sin(6.0 * mu);

        double n =
                a
                        / Math.sqrt(1.0 - eSquared * Math.sin(phi1Rad) * Math.sin(phi1Rad));
        double t = Math.tan(phi1Rad) * Math.tan(phi1Rad);
        double c = ePrimeSquared * Math.cos(phi1Rad) * Math.cos(phi1Rad);
        double r =
                a
                        * (1.0 - eSquared)
                        / Math.pow(
                        1.0 - eSquared * Math.sin(phi1Rad) * Math.sin(phi1Rad),
                        1.5);
        double d = x / (n * UTM_F0);

        double latitude = (
                phi1Rad
                        - (n * Math.tan(phi1Rad) / r)
                        * (d * d / 2.0
                        - (5.0
                        + (3.0 * t)
                        + (10.0 * c)
                        - (4.0 * c * c)
                        - (9.0 * ePrimeSquared))
                        * Math.pow(d, 4.0)
                        / 24.0
                        + (61.0
                        + (90.0 * t)
                        + (298.0 * c)
                        + (45.0 * t * t)
                        - (252.0 * ePrimeSquared)
                        - (3.0 * c * c))
                        * Math.pow(d, 6.0)
                        / 720.0)) * (180.0 / Math.PI);

        double longitude = longitudeOrigin + (
                (d
                        - (1.0 + 2.0 * t + c) * Math.pow(d, 3.0) / 6.0
                        + (5.0
                        - (2.0 * c)
                        + (28.0 * t)
                        - (3.0 * c * c)
                        + (8.0 * ePrimeSquared)
                        + (24.0 * t * t))
                        * Math.pow(d, 5.0)
                        / 120.0)
                        / Math.cos(phi1Rad)) * (180.0 / Math.PI);

        return new LatLng(latitude, longitude);
    }
}

class RefEll {
    public double ecc, maj, min;
    RefEll(double maj, double min) {
        this.maj = maj;
        this.min = min;
        this.ecc = ((maj * maj) - (min * min)) / (maj * maj);
    }
}
