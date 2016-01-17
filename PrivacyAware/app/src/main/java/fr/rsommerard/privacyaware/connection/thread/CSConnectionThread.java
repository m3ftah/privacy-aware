package fr.rsommerard.privacyaware.connection.thread;

import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import fr.rsommerard.privacyaware.connection.ConnectionManager;
import fr.rsommerard.privacyaware.data.Data;
import fr.rsommerard.privacyaware.data.DataManager;
import fr.rsommerard.privacyaware.peer.Peer;

/**
 * Connect and Send Connection Thread
 */
public class CSConnectionThread extends Thread implements Runnable {

    private static final String TAG = "PACSCT";

    private final DataManager mDataManager;
    private final ConnectionManager mConnectionManager;
    private final Peer mPeer;
    private final Socket mSocket;

    public CSConnectionThread(final ConnectionManager connectionManager, final Peer peer) {
        Log.i(TAG, "CSConnectionThread(ConnectionManager connectionManager, Peer peer)");

        mConnectionManager = connectionManager;
        mPeer = peer;
        mDataManager = DataManager.getInstance();
        mSocket = new Socket();
    }

    @Override
    public void run() {
        Log.i(TAG, "run()");

        sleepBeforeProcess();

        try {
            process();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            exitProperly();
        }
    }

    private void sleepBeforeProcess() {
        Log.i(TAG, "sleepBeforeProcess()");

        try {
            sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void process() throws IOException {
        Log.i(TAG, "process()");

        mSocket.bind(null);
        mSocket.connect(new InetSocketAddress(mPeer.getLocalAddress(), mPeer.getPort()), 5000);

        OutputStream outputStream = mSocket.getOutputStream();

        Data data = mDataManager.getData();

        Log.d(TAG, "Sending \"" + data.getContent() + "\", to " + mPeer.getName());

        byte[] buffer = data.getContent().getBytes();
        outputStream.write(buffer);

        mSocket.close();

        mDataManager.removeData(data);
    }

    private void exitProperly() {
        Log.i(TAG, "exitProperly()");

        if (mSocket != null) {
            if (mSocket.isConnected()) {
                try {
                    mSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        mConnectionManager.disconnect();
    }
}