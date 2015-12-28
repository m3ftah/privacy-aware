package fr.rsommerard.privacyaware;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class PassiveThread extends Thread implements Runnable {

    private final String TAG = PassiveThread.class.getSimpleName();

    private ServerSocket mServerSocket;

    public PassiveThread(ServerSocket serverSocket) {
        super();

        mServerSocket = serverSocket;
    }

    @Override
    public void run() {
        super.run();

        Log.i(TAG, "run()");
        Log.i(TAG, mServerSocket.getInetAddress().toString());
        Log.i(TAG, String.valueOf(mServerSocket.getLocalPort()));

        try {
            while(true) {
                Socket socket = mServerSocket.accept();

                InputStream inputStream = socket.getInputStream();
                byte[] buffer = new byte[1024];

                int bytes = inputStream.read(buffer);

                Log.i(TAG, new String(buffer, 0, bytes));

                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
