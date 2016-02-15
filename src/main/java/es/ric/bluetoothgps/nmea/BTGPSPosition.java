package es.ric.bluetoothgps.nmea;

import android.location.Location;
import android.os.Bundle;

import java.io.Serializable;

/**
 * Created by Ricardo on 17/12/15.
 */
public class BTGPSPosition implements Serializable{

    public static final String QUALITY = "quality";
    public static final String SENTENCE = "sentence";

    public float time = 0.0f;
    public float lat = 0.0f;
    public float lon = 0.0f;
    public boolean fixed = false;
    public int quality = 0;
    public float dir = 0.0f;
    public float altitude = 0.0f;
    public float velocity = 0.0f;
    public String sentence = "";

    public void updatefix() {
        fixed = quality > 0;
    }

    public String toString() {
        return String.format("%s lat: %f, lon: %f, time: %f, Q: %d, dir: %f, alt: %f, vel: %f", sentence, lat, lon, time, quality, dir, altitude, velocity);
    }

    public Location getLocation(){
        Location location = new Location("");
        location.setTime((long) time);
        location.setLatitude(lat);
        location.setLongitude(lon);
        location.setAltitude(altitude);
        location.setSpeed(velocity);
        Bundle extras = new Bundle();
        extras.putInt(QUALITY, quality);
        extras.putString(SENTENCE, sentence);
        location.setExtras(extras);

        return location;
    }
}
