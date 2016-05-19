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
 * Wait and Send Connection Thread
 */
public class WSConnectionThread extends Thread implements Runnable {

    private final ServerSocket mServerSocket;
    private final DataManager mDataManager;
    private final ConnectionManager mConnectionManager;

    private Socket mSocket;

    public WSConnectionThread(final Context context, final ServerSocket serverSocket) {
        Log.i(WiFiDirect.TAG, "WSConnectionThread(ServerSocket serverSocket)");

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

        mConnectionManager.disconnect();
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
