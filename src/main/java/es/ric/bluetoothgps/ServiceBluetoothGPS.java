package es.ric.bluetoothgps;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;

import es.ric.bluetoothgps.nmea.BTGPSPosition;

/**
 * Created by Ricardo on 11/01/16.
 */
public class ServiceBluetoothGPS extends IntentService implements BTGPSListener {

    public static final String ACTION_NEW_LOCATION = "es.ric.bluetoothgps.new_location";
    public static final String ACTION_CONNECTION_SUCCESS = "es.ric.bluetoothgps.success";
    public static final String ACTION_CONNECTION_ERROR = "es.ric.bluetoothgps.error";
    public static final String ACTION_CONNECTION_CLOSE = "es.ric.bluetoothgps.close";
    public static final String ACTION_CONNECTION_CLOSED = "es.ric.bluetoothgps.closed";
    public static final String ACTION_BLUETOOTH_NO_SUPPORTED = "es.ric.bluetoothgps.no_support";
    public static final String ACTION_CANT_REACH_CONNECTION = "es.ric.bluetoothgps.cant_connect";
    public static final String POSICION = "posicion";
    public static final String DEVICE = "device";
    public static final String REFRESH_TIME = "refresh_time";

    private BTConnectThread hilo_bluetooh;
    private boolean first_time = true;
    private BluetoothDevice device;
    private static ServiceBluetoothGPS instance = null;

    public ServiceBluetoothGPS() {
        super("es.ric.ServiceBluetoothGPS");
    }
    @Override
    public void onCreate()
    {
        super.onCreate();
        instance = this;
    }



    public void stopGPS(){
        if(hilo_bluetooh!=null)
            hilo_bluetooh.stopConnection();
    }

    public static ServiceBluetoothGPS getInstance() {
        return instance;
    }
    public boolean isGPSOn() {
        return (hilo_bluetooh!=null) && (hilo_bluetooh.isAlive());
    }

    @Override
    protected void onHandleIntent(Intent myIntent) {


        if (myIntent !=null && myIntent.getExtras()!=null) {
            device = myIntent.getParcelableExtra(DEVICE);
            long refresh_time = myIntent.getLongExtra(REFRESH_TIME, 0);

            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null) {
                // Device does not support Bluetooth
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(ACTION_BLUETOOTH_NO_SUPPORTED);
                this.sendBroadcast(broadcastIntent);
            }

            try {
                hilo_bluetooh = new BTConnectThread(device, mBluetoothAdapter, this, refresh_time);
            }
            catch(Exception e){
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(ACTION_CONNECTION_ERROR);
                this.sendBroadcast(broadcastIntent);
                return;
            }

            hilo_bluetooh.start();

        }

    }

    @Override
    public void update(BTGPSPosition btgpsPosition, String s) {

        if(first_time){
            first_time = false;
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(ACTION_CONNECTION_SUCCESS);
            broadcastIntent.putExtra(DEVICE,device);
            this.sendBroadcast(broadcastIntent);
        }
        else {
            Intent bcIntent = new Intent();
            bcIntent.setAction(ACTION_NEW_LOCATION);
            bcIntent.putExtra(POSICION, btgpsPosition.getLocation());
            sendBroadcast(bcIntent);
        }
    }

    @Override
    public void abortedConnection(){
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(ACTION_CONNECTION_ERROR);
        this.sendBroadcast(broadcastIntent);
    }

    @Override
    public void cantReachConnection(){
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(ACTION_CANT_REACH_CONNECTION);
        this.sendBroadcast(broadcastIntent);
    }

}
