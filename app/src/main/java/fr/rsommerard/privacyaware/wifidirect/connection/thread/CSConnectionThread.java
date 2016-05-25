package fr.rsommerard.privacyaware.wifidirect.connection.thread;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import fr.rsommerard.privacyaware.WiFiDirect;
import fr.rsommerard.privacyaware.dao.Data;
import fr.rsommerard.privacyaware.dao.Device;
import fr.rsommerard.privacyaware.data.DataManager;
import fr.rsommerard.privacyaware.wifidirect.connection.ConnectionManager;

/**
 * Connect and Send Connection Thread
 */
public class CSConnectionThread extends Thread implements Runnable {

    private final DataManager mDataManager;
    private final Device mDevice;
    private final Socket mSocket;
    private final ConnectionManager mConnectionManager;

    public CSConnectionThread(final Context context, final Device device) {
        Log.i(WiFiDirect.TAG, "CSConnectionThread(ConnectionManager connectionManager, Device peer)");

        mDevice = device;
        mDataManager = DataManager.getInstance(context);
        mConnectionManager = ConnectionManager.getInstance(context);
        mSocket = new Socket();
    }

    @Override
    public void run() {
        Log.i(WiFiDirect.TAG, "run()");

        sleepBeforeProcess();

        try {
            process();
        } catch (Exception e) {
            e.printStackTrace();
            mConnectionManager.disconnect();
        } finally {
            exitProperly();
        }
    }

    private void sleepBeforeProcess() {
        Log.i(WiFiDirect.TAG, "sleepBeforeProcess()");

        try {
            sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void process() throws Exception {
        Log.i(WiFiDirect.TAG, "process()");

        /*mSocket.bind(null);
        Log.d(WiFiDirect.TAG, mDevice.getLocalAddress() + ":" + mDevice.getPort());
        mSocket.connect(new InetSocketAddress(mDevice.getLocalAddress(), mDevice.getPort()), 0);

        Data data = mDataManager.getData();

        Log.d(WiFiDirect.TAG, "Sending \"" + data.getContent() + "\"");

        if (data == null) {
            return;
        }

        ObjectOutputStream objectOutputStream = new ObjectOutputStream(mSocket.getOutputStream());
        objectOutputStream.writeObject(data);
        objectOutputStream.flush();

        ObjectInputStream objectInputStream = new ObjectInputStream(mSocket.getInputStream());
        String ack = (String) objectInputStream.readObject();

        if ("ACK".equals(ack)) {
            Log.d(WiFiDirect.TAG, "ACK received");
            mDataManager.removeData(data);
        }

        mConnectionManager.disconnect();*/
    }

    private void exitProperly() {
        Log.i(WiFiDirect.TAG, "exitProperly()");

        if (mSocket.isConnected()) {
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
                mConnectionManager.disconnect();
            }
        }
    }
}