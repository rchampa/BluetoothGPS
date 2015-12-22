package es.ric.bluetoothgps;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;

import es.ric.bluetoothgps.nmea.GPSPosition;
import es.ric.bluetoothgps.nmea.NMEA;


/**
 * Created by Ricardo on 16/12/15.
 */
public class ConnectThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private final BluetoothAdapter mBluetoothAdapter;
    private MyBlueToothListener listener;
    private long milliseconds;

    private volatile boolean isON;

    public ConnectThread(BluetoothDevice device,  BluetoothAdapter mBluetoothAdapter, MyBlueToothListener listener, long milliseconds) {

        this.listener = listener;
        this.milliseconds = milliseconds;

        isON = true;

        // Use a temporary object that is later assigned to mmSocket,
        // because mmSocket is final
        BluetoothSocket tmp = null;
        mmDevice = device;
        this.mBluetoothAdapter = mBluetoothAdapter;

        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            // MY_UUID is the app's UUID string, also used by the server code
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            tmp = device.createRfcommSocketToServiceRecord(uuid);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        mmSocket = tmp;
    }

    public void stopConnection(){
        isON = false;
    }

    public void run() {
        // Cancel discovery because it will slow down the connection
        mBluetoothAdapter.cancelDiscovery();

        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            mmSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and get out
            try {
                mmSocket.close();
            } catch (IOException closeException) { }
            return;
        }

        // Do work to manage the connection (in a separate thread)
        manageConnectedSocket(mmSocket);
    }


    private void manageConnectedSocket(BluetoothSocket socket){

        InputStream in=null;
        InputStreamReader isr=null;
        BufferedReader br=null;

        try {
            in = socket.getInputStream();
            isr = new InputStreamReader(in);
            br = new BufferedReader(isr);
            NMEA parser = new NMEA();
            String nmeaMessage;

            long startTime=0;
            long endTime=0;

            long timeElapsed = this.milliseconds;

            while (isON) {

                if(timeElapsed>=this.milliseconds) {
                    startTime = System.currentTimeMillis();

                    nmeaMessage = br.readLine();
                    GPSPosition parsed_position = parser.parse(nmeaMessage);
//                    Log.d("GPS", nmeaMessage);
                    if (listener != null) {
                        listener.update(parsed_position, nmeaMessage);
                    }

                }
                endTime  = System.currentTimeMillis();
                timeElapsed = endTime - startTime;


            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally {
            try {
                br.close();
                isr.close();
                in.close();
                mmSocket.close();
            }
            catch (Exception e){}

        }
    }
}