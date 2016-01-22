package fr.rsommerard.privacyaware.wifidirect.connection.thread;

import android.util.Log;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import fr.rsommerard.privacyaware.wifidirect.connection.ConnectionManager;
import fr.rsommerard.privacyaware.data.Data;
import fr.rsommerard.privacyaware.data.DataManager;

/**
 * Wait and Send Connection Thread
 */
public class WSConnectionThread extends Thread implements Runnable {

    private static final String TAG = "PAWSCT";

    private final ServerSocket mServerSocket;
    private final DataManager mDataManager;
    private final ConnectionManager mConnectionManager;
    private final boolean mGroupOwner;

    private Socket mSocket;

    public WSConnectionThread(final ConnectionManager connectionManager, final ServerSocket serverSocket, final boolean groupOwner) {
        Log.i(TAG, "WSConnectionThread(ServerSocket serverSocket)");

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
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            exitProperly();
        }
    }

    private void process() throws IOException {
        //Log.i(TAG, "process()");

        mSocket = mServerSocket.accept();

        Data data = mDataManager.getData();

        Log.d(TAG, "Sending \"" + data.getContent() + "\"");

        ObjectOutputStream objectOutputStream = new ObjectOutputStream(mSocket.getOutputStream());
        objectOutputStream.writeObject(data);
        objectOutputStream.flush();

        mSocket.close();

        mDataManager.removeData(data);
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
