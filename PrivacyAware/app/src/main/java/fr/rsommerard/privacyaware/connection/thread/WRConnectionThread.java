package fr.rsommerard.privacyaware.connection.thread;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
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

    public WRConnectionThread(final ConnectionManager connectionManager, final ServerSocket serverSocket) {
        Log.i(TAG, "WRConnectionThread(ServerSocket serverSocket)");

        mConnectionManager = connectionManager;
        mServerSocket = serverSocket;
        mDataManager = DataManager.getInstance();
    }

    @Override
    public void run() {
        Log.i(TAG, "run()");

        try {
            process();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            exitProperly();
        }
    }

    public void process() throws IOException {
        Log.i(TAG, "process()");

        mSocket = mServerSocket.accept();

        InputStream inputStream = mSocket.getInputStream();
        byte[] buffer = new byte[1024];

        int bytes = inputStream.read(buffer);

        if (bytes == -1) {
            mSocket.close();
            return;
        }

        Data data = new Data(new String(buffer, 0, bytes));
        mDataManager.addData(data);

        Log.d(TAG, "Received \"" + data.getContent() + "\"");

        mSocket.close();
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
