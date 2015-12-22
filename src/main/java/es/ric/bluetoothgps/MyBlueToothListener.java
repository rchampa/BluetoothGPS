package es.ric.bluetoothgps;


import es.ric.bluetoothgps.nmea.GPSPosition;

/**
 * Created by Ricardo on 17/12/15.
 */
public interface MyBlueToothListener {

    public void update(GPSPosition position, String nmea_message);
}
