package fr.rsommerard.privacyaware.wifidirect.connection.thread;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import fr.rsommerard.privacyaware.dao.Data;
import fr.rsommerard.privacyaware.data.DataManager;
import fr.rsommerard.privacyaware.wifidirect.connection.ConnectionManager;
import fr.rsommerard.privacyaware.wifidirect.peer.Peer;

/**
 * Connect and Receive Connection Thread
 */
public class CRConnectionThread extends Thread implements Runnable {

    private static final String TAG = "PACRCT";

    private final DataManager mDataManager;
    private final Peer mPeer;
    private final Socket mSocket;
    private final ConnectionManager mConnectionManager;

    public CRConnectionThread(final Context context, final Peer peer) {
        Log.i(TAG, "CRConnectionThread(ConnectionManager connectionManager, Peer peer)");

        mPeer = peer;
        mDataManager = DataManager.getInstance(context);
        mConnectionManager = ConnectionManager.getInstance(context);
        mSocket = new Socket();
    }

    @Override
    public void run() {
        Log.i(TAG, "run()");

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

    private void process() throws Exception {
        Log.i(TAG, "process()");

        mSocket.bind(null);
        Log.d(TAG, mPeer.getLocalAddress() + ":" + mPeer.getPort());
        mSocket.connect(new InetSocketAddress(mPeer.getLocalAddress(), mPeer.getPort()), 0);

        ObjectInputStream objectInputStream = new ObjectInputStream(mSocket.getInputStream());
        Data data = (Data) objectInputStream.readObject();

        Log.d(TAG, "Received \"" + data.getContent() + "\"");

        data.setId(null);
        mDataManager.addData(data);

        ObjectOutputStream objectOutputStream = new ObjectOutputStream(mSocket.getOutputStream());
        objectOutputStream.writeObject("ACK");
        objectOutputStream.flush();

        sleep(2000);
    }

    private void sleepBeforeProcess() {
        Log.i(TAG, "sleepBeforeProcess()");

        try {
            sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void exitProperly() {
        Log.i(TAG, "exitProperly()");

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