package es.ric.bluetoothgps;


import es.ric.bluetoothgps.nmea.BTGPSPosition;

/**
 * Created by Ricardo on 17/12/15.
 */
public interface BTGPSListener {

    public void update(BTGPSPosition position, String nmea_message);
}
