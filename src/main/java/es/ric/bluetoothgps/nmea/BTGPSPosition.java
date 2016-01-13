package es.ric.bluetoothgps.nmea;

import java.io.Serializable;

/**
 * Created by Ricardo on 17/12/15.
 */
public class BTGPSPosition implements Serializable{
    public float time = 0.0f;
    public float lat = 0.0f;
    public float lon = 0.0f;
    public boolean fixed = false;
    public int quality = 0;
    public float dir = 0.0f;
    public float altitude = 0.0f;
    public float velocity = 0.0f;

    public void updatefix() {
        fixed = quality > 0;
    }

    public String toString() {
        return String.format("lat: %f, lon: %f, time: %f, Q: %d, dir: %f, alt: %f, vel: %f", lat, lon, time, quality, dir, altitude, velocity);
    }
}
