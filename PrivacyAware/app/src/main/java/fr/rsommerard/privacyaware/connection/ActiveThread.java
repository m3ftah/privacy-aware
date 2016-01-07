package fr.rsommerard.privacyaware.connection;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import fr.rsommerard.privacyaware.data.Data;
import fr.rsommerard.privacyaware.data.DataManager;
import fr.rsommerard.privacyaware.peer.Peer;

public class ActiveThread extends Thread implements Runnable {

    private final String TAG = "PAAT";

    private ConnectionManager mConnectionManager;
    private Peer mPeer;
    private DataManager mDataManager;

    public ActiveThread(ConnectionManager connectionManager, Peer peer) {
        super();
        Log.i(TAG, "ActiveThread()");

        mPeer = peer;
        mDataManager = DataManager.getInstance();
        mConnectionManager = connectionManager;
    }

    @Override
    public void run() {
        super.run();
        Log.i(TAG, "run()");

        if (!mDataManager.hasDatas()) {
            return;
        }

        Socket socket = new Socket();

        try {
            Log.d(TAG, "Connect to: " + mPeer.getLocalAddress() + ":" + mPeer.getPort());

            // TODO: try to decrease sleep time
            try {
                sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            socket.bind(null);
            socket.connect(new InetSocketAddress(mPeer.getLocalAddress(), mPeer.getPort()), 5000);

            OutputStream outputStream = socket.getOutputStream();

            Data data = mDataManager.getData();

            Log.d(TAG, "Sending \"" + data.getContent() + "\", to " + mPeer.getName());

            byte[] buffer = data.getContent().getBytes();
            outputStream.write(buffer);

            socket.close();

            mConnectionManager.disconnect();
            mDataManager.removeData(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (socket != null) {
                if (socket.isConnected()) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            mConnectionManager.disconnect();
        }
    }
}
