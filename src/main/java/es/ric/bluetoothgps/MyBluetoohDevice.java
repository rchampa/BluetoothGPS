package es.ric.bluetoothgps;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Ricardo on 16/12/15.
 */
public class MyBluetoohDevice {

    private BluetoothDevice device;

    public MyBluetoohDevice(BluetoothDevice device){
        this.device = device;
    }

    @Override public String toString(){
        return device.getName() + "\n" + device.getAddress();
    }

    public BluetoothDevice getDevice(){
        return device;
    }

}
