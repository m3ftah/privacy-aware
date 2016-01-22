package fr.rsommerard.privacyaware.wifidirect.connection.thread;

import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import fr.rsommerard.privacyaware.wifidirect.connection.ConnectionManager;
import fr.rsommerard.privacyaware.data.Data;
import fr.rsommerard.privacyaware.data.DataManager;
import fr.rsommerard.privacyaware.wifidirect.peer.Peer;

/**
 * Connect and Receive Connection Thread
 */
public class CRConnectionThread extends Thread implements Runnable {

    private static final String TAG = "PACRCT";

    private final DataManager mDataManager;
    private final ConnectionManager mConnectionManager;
    private final Peer mPeer;
    private final Socket mSocket;
    private final boolean mGroupOwner;

    public CRConnectionThread(final ConnectionManager connectionManager, final Peer peer, final boolean groupOwner) {
        Log.i(TAG, "CRConnectionThread(ConnectionManager connectionManager, Peer peer)");

        mConnectionManager = connectionManager;
        mPeer = peer;
        mDataManager = DataManager.getInstance();
        mSocket = new Socket();
        mGroupOwner = groupOwner;
    }

    @Override
    public void run() {
        //Log.i(TAG, "run()");

        sleepBeforeProcess();

        try {
            process();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            exitProperly();
        }
    }

    private void process() throws IOException, ClassNotFoundException {
        //Log.i(TAG, "process()");

        mSocket.bind(null);
        Log.d(TAG, mPeer.getLocalAddress() + ":" + mPeer.getPort());
        mSocket.connect(new InetSocketAddress(mPeer.getLocalAddress(), mPeer.getPort()), 5000);

        ObjectInputStream objectInputStream = new ObjectInputStream(mSocket.getInputStream());
        Data data = (Data) objectInputStream.readObject();

        Log.d(TAG, "Received \"" + data.getContent() + "\"");

        mDataManager.addData(data);

        mSocket.close();
    }

    private void sleepBeforeProcess() {
        //Log.i(TAG, "sleepBeforeProcess()");

        try {
            sleep(11000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void exitProperly() {
        //Log.i(TAG, "exitProperly()");

        if (mSocket.isConnected()) {
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (mGroupOwner) {
            Log.i(TAG, "exitProperly()::disconnect()");
            mConnectionManager.disconnect();
        }
    }
}