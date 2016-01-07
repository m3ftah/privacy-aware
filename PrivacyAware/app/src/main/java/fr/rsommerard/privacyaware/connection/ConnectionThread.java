package fr.rsommerard.privacyaware.connection;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import fr.rsommerard.privacyaware.data.Data;
import fr.rsommerard.privacyaware.data.DataManager;
import fr.rsommerard.privacyaware.peer.Peer;

public class ConnectionThread extends Thread implements Runnable {

    private final String TAG = "PACT";

    private ServerSocket mServerSocket;
    private Peer mPeer;
    private boolean mDataSender;
    private Socket mSocket;
    private ConnectionManager mConnectionManager;
    private DataManager mDataManager;

    public ConnectionThread(ConnectionManager connectionManager, ServerSocket serverSocket, Peer peer, boolean dataSender) {
        mServerSocket = serverSocket;
        mPeer = peer;
        mDataSender = dataSender;
        mConnectionManager = connectionManager;
        mDataManager = DataManager.getInstance();
    }


    @Override
    public void run() {
        super.run();

        try {
            if (mDataSender) {
                sendData();
            } else {
                receiveData();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
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

    private void receiveData() throws IOException {
        Socket socket = mServerSocket.accept();
        InputStream inputStream = socket.getInputStream();
        byte[] buffer = new byte[1024];

        int bytes = inputStream.read(buffer);

        if (bytes == -1) {
            socket.close();
            return;
        }

        Data data = new Data(new String(buffer, 0, bytes));
        mDataManager.addData(data);

        Log.d(TAG, "Received \"" + data.getContent() + "\"");

        socket.close();
    }

    private void sendData() throws IOException {
        mSocket = new Socket();

        try {
            sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mSocket.bind(null);
        mSocket.connect(new InetSocketAddress(mPeer.getLocalAddress(), mPeer.getPort()), 5000);

        OutputStream outputStream = mSocket.getOutputStream();

        Data data = mDataManager.getData();

        Log.d(TAG, "Sending \"" + data.getContent() + "\", to " + mPeer.getName());

        byte[] buffer = data.getContent().getBytes();
        outputStream.write(buffer);

        mSocket.close();

        mDataManager.removeData(data);
        mConnectionManager.disconnect();
    }
}
