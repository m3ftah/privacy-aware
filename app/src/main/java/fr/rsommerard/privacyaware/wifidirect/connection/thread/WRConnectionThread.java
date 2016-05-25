package fr.rsommerard.privacyaware.wifidirect.connection.thread;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import fr.rsommerard.privacyaware.WiFiDirect;
import fr.rsommerard.privacyaware.dao.Data;
import fr.rsommerard.privacyaware.data.DataManager;
import fr.rsommerard.privacyaware.wifidirect.connection.ConnectionManager;

/**
 * Wait and Receive Connection Thread
 */
public class WRConnectionThread extends Thread implements Runnable {

    private final ServerSocket mServerSocket;
    private final DataManager mDataManager;
    private final ConnectionManager mConnectionManager;

    private Socket mSocket;

    public WRConnectionThread(final Context context, final ServerSocket serverSocket) {
        Log.i(WiFiDirect.TAG, "WRConnectionThread(ServerSocket serverSocket)");

        mServerSocket = serverSocket;
        mDataManager = DataManager.getInstance(context);
        mConnectionManager = ConnectionManager.getInstance(context);
    }

    @Override
    public void run() {
        Log.i(WiFiDirect.TAG, "run()");

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
        Log.i(WiFiDirect.TAG, "process()");

        mSocket = mServerSocket.accept();

        ObjectInputStream objectInputStream = new ObjectInputStream(mSocket.getInputStream());
        Data data = (Data) objectInputStream.readObject();

        Log.d(WiFiDirect.TAG, "Received \"" + data.getContent() + "\"");

        data.setId(null);
        mDataManager.addData(data);

        ObjectOutputStream objectOutputStream = new ObjectOutputStream(mSocket.getOutputStream());
        objectOutputStream.writeObject("ACK");
        objectOutputStream.flush();

        sleep(2000);
    }

    private void exitProperly() {
        Log.i(WiFiDirect.TAG, "exitProperly()");

        if (mSocket != null) {
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
}
