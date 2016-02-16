package es.ric.bluetoothgps;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;

import es.ric.bluetoothgps.nmea.BTGPSPosition;
import es.ric.bluetoothgps.nmea.NMEA;


/**
 * Created by Ricardo on 16/12/15.
 */
public class BTConnectThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private final BluetoothAdapter mBluetoothAdapter;
    private BTGPSListener listener;
    private long milliseconds = 500;

    private volatile boolean isON;

    /**
     * Creates a thread that handles bluetooth socket
     * @param device the bluetooth gps device
     * @param mBluetoothAdapter a BluetoothAdapter object
     * @param listener to listen new info updates from bluetooth socket
     * @param milliseconds the time between each update, 500ms by default. Probably you want adjust(less than 500ms)
     *                     this value to catch more values.
     */
    public BTConnectThread(BluetoothDevice device, BluetoothAdapter mBluetoothAdapter, BTGPSListener listener, long milliseconds)
    throws Exception{

        if(listener==null || mBluetoothAdapter==null){
            throw new NullPointerException();
        }

        this.listener = listener;
        this.milliseconds = milliseconds;

        isON = true;

        // Use a temporary object that is later assigned to mmSocket,
        // because mmSocket is final
        BluetoothSocket tmp = null;
        mmDevice = device;
        this.mBluetoothAdapter = mBluetoothAdapter;

        // Get a BluetoothSocket to connect with the given BluetoothDevice
//        try {
            // MY_UUID is the app's UUID string, also used by the server code
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            tmp = mmDevice.createRfcommSocketToServiceRecord(uuid);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
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
            }
            catch (IOException closeException) { }
            if(listener!=null){
                listener.cantReachConnection();
            }
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

                nmeaMessage = br.readLine();

                if(timeElapsed>=this.milliseconds) {
                    startTime = System.currentTimeMillis();
                    BTGPSPosition parsed_position = parser.parse(nmeaMessage);

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
            if(listener!=null){
                listener.abortedConnection();
            }
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