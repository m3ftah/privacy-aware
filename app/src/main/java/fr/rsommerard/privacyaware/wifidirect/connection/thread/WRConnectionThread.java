package fr.rsommerard.privacyaware.wifidirect.connection.thread;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import fr.rsommerard.privacyaware.dao.Data;
import fr.rsommerard.privacyaware.data.DataManager;

/**
 * Wait and Receive Connection Thread
 */
public class WRConnectionThread extends Thread implements Runnable {

    private static final String TAG = "PAWRCT";

    private final ServerSocket mServerSocket;
    private final DataManager mDataManager;

    private Socket mSocket;

    public WRConnectionThread(final Context context, final ServerSocket serverSocket) {
        Log.i(TAG, "WRConnectionThread(ServerSocket serverSocket)");

        mServerSocket = serverSocket;
        mDataManager = DataManager.getInstance(context);
    }

    @Override
    public void run() {
        Log.i(TAG, "run()");

        try {
            process();
        } catch (IOException | ClassNotFoundException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            exitProperly();
        }
    }

    private void process() throws IOException, ClassNotFoundException, InterruptedException {
        Log.i(TAG, "process()");

        mSocket = mServerSocket.accept();

        ObjectInputStream objectInputStream = new ObjectInputStream(mSocket.getInputStream());
        Data data = (Data) objectInputStream.readObject();

        Log.d(TAG, "Received \"" + data.getContent() + "\"");

        mDataManager.addData(data);

        ObjectOutputStream objectOutputStream = new ObjectOutputStream(mSocket.getOutputStream());
        objectOutputStream.writeObject("ACK");
        objectOutputStream.flush();

        sleep(2000);
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
    }
}
