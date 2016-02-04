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
import fr.rsommerard.privacyaware.wifidirect.peer.Peer;

/**
 * Connect and Send Connection Thread
 */
public class CSConnectionThread extends Thread implements Runnable {

    private static final String TAG = "PACSCT";

    private final DataManager mDataManager;
    private final Peer mPeer;
    private final Socket mSocket;

    public CSConnectionThread(final Context context, final Peer peer) {
        Log.i(TAG, "CSConnectionThread(ConnectionManager connectionManager, Peer peer)");

        mPeer = peer;
        mDataManager = DataManager.getInstance(context);
        mSocket = new Socket();
    }

    @Override
    public void run() {
        Log.i(TAG, "run()");

        sleepBeforeProcess();

        try {
            process();
        } catch (IOException | ClassNotFoundException e) {
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

    private void process() throws IOException, ClassNotFoundException {
        Log.i(TAG, "process()");

        mSocket.bind(null);
        Log.d(TAG, mPeer.getLocalAddress() + ":" + mPeer.getPort());
        mSocket.connect(new InetSocketAddress(mPeer.getLocalAddress(), mPeer.getPort()), 0);

        Data data = mDataManager.getData();

        Log.d(TAG, "Sending \"" + data.getContent() + "\"");

        ObjectOutputStream objectOutputStream = new ObjectOutputStream(mSocket.getOutputStream());
        objectOutputStream.writeObject(data);
        objectOutputStream.flush();

        ObjectInputStream objectInputStream = new ObjectInputStream(mSocket.getInputStream());
        String ack = (String) objectInputStream.readObject();

        if ("ACK".equals(ack)) {
            Log.d(TAG, "ACK received");
            mDataManager.removeData(data);
        }
    }

    private void exitProperly() {
        Log.i(TAG, "exitProperly()");

        if (mSocket.isConnected()) {
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}