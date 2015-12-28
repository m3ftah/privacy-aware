package fr.rsommerard.privacyaware;

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

public class ActiveThread extends Thread implements Runnable {

    private final String TAG = ActiveThread.class.getSimpleName();

    private InetAddress mLocalAddress;
    private Handler mHandler;
    private Peer mPeer;
    private DataManager mDataManager;

    public ActiveThread(Peer peer, InetAddress localAddress, Handler handler) {
        super();

        mPeer = peer;
        mHandler = handler;
        mLocalAddress = localAddress;
        mDataManager = DataManager.getInstance();
    }

    @Override
    public void run() {
        super.run();

        Log.i(TAG, "run()");

        Socket socket = new Socket();

        try {
            Log.d(TAG, "Connect to: " + mLocalAddress + ":" + mPeer.getPort());

            try {
                sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            socket.connect(new InetSocketAddress(mLocalAddress, mPeer.getPort()), 5000);

            OutputStream outputStream = socket.getOutputStream();

            Data data = mDataManager.getData();

            Log.d(TAG, "Sending \"" + data.getContent() + "\", to " + mPeer.getName());

            byte[] buffer = data.getContent().getBytes();
            outputStream.write(buffer);

            socket.close();

            Message handlerMessage = new Message();
            handlerMessage.obj = "FINISH";
            mHandler.sendMessage(handlerMessage);

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
        }
    }
}
