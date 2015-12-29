package fr.rsommerard.privacyaware.connection;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import fr.rsommerard.privacyaware.data.Data;
import fr.rsommerard.privacyaware.data.DataManager;

public class PassiveThread extends Thread implements Runnable {

    private final String TAG = PassiveThread.class.getSimpleName();

    private ServerSocket mServerSocket;
    private DataManager mDataManager;

    public PassiveThread(ServerSocket serverSocket) {
        super();

        mServerSocket = serverSocket;
        mDataManager = DataManager.getInstance();
    }

    @Override
    public void run() {
        super.run();

        try {
            while(true) {
                Socket socket = mServerSocket.accept();

                InputStream inputStream = socket.getInputStream();
                byte[] buffer = new byte[1024];

                int bytes = inputStream.read(buffer);

                Data data = new Data(new String(buffer, 0, bytes));
                mDataManager.addData(data);

                Log.d(TAG, "Received \"" + data.getContent() + "\"");

                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
