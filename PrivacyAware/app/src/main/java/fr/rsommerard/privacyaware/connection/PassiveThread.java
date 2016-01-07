package fr.rsommerard.privacyaware.connection;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import fr.rsommerard.privacyaware.data.Data;
import fr.rsommerard.privacyaware.data.DataManager;

public class PassiveThread extends Thread implements Runnable {

    private final String TAG = "PAPT";

    private ServerSocket mServerSocket;
    private DataManager mDataManager;
    private Socket mSocket;

    public PassiveThread(ServerSocket serverSocket) {
        super();
        Log.i(TAG, "PassiveThread()");

        mServerSocket = serverSocket;
        mDataManager = DataManager.getInstance();
    }

    @Override
    public void run() {
        super.run();
        Log.i(TAG, "run()");

        try {
            while(true) {
                mSocket = mServerSocket.accept();

                InputStream inputStream = mSocket.getInputStream();
                byte[] buffer = new byte[1024];

                int bytes = inputStream.read(buffer);

                if (bytes == -1) {
                    mSocket.close();
                    continue;
                }

                Data data = new Data(new String(buffer, 0, bytes));
                mDataManager.addData(data);

                Log.d(TAG, "Received \"" + data.getContent() + "\"");

                mSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
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
}
