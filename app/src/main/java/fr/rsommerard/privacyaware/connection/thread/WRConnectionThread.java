package fr.rsommerard.privacyaware.connection.thread;

import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import fr.rsommerard.privacyaware.connection.ConnectionManager;
import fr.rsommerard.privacyaware.data.Data;
import fr.rsommerard.privacyaware.data.DataManager;

/**
 * Wait and Receive Connection Thread
 */
public class WRConnectionThread extends Thread implements Runnable {

    private static final String TAG = "PAWRCT";

    private final ServerSocket mServerSocket;
    private final DataManager mDataManager;
    private final ConnectionManager mConnectionManager;

    private Socket mSocket;
    private final boolean mGroupOwner;

    public WRConnectionThread(final ConnectionManager connectionManager, final ServerSocket serverSocket, final boolean groupOwner) {
        Log.i(TAG, "WRConnectionThread(ServerSocket serverSocket)");

        mConnectionManager = connectionManager;
        mServerSocket = serverSocket;
        mDataManager = DataManager.getInstance();
        mGroupOwner = groupOwner;
    }

    @Override
    public void run() {
        //Log.i(TAG, "run()");

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

        mSocket = mServerSocket.accept();

        ObjectInputStream objectInputStream = new ObjectInputStream(mSocket.getInputStream());
        Data data = (Data) objectInputStream.readObject();

        Log.d(TAG, "Received \"" + data.getContent() + "\"");

        mDataManager.addData(data);

        mSocket.close();
    }

    private void exitProperly() {
        //Log.i(TAG, "exitProperly()");

        if (mSocket != null) {
            if (mSocket.isConnected()) {
                try {
                    mSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (mGroupOwner) {
            Log.i(TAG, "exitProperly()::disconnect()");
            mConnectionManager.disconnect();
        }
    }
}
