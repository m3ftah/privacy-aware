package fr.rsommerard.privacyaware.connection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;

import fr.rsommerard.privacyaware.data.Data;
import fr.rsommerard.privacyaware.peer.Peer;

public class ConnectionManager {

    private final String TAG = ConnectionManager.class.getSimpleName();

    private static ConnectionManager sInstance;

    private enum ConnectionState {
        CONNECTED,
        DISCONNECTED
    }

    private ActiveThread mActiveThread;
    private IntentFilter mIntentFilter;
    private ConnectionBroadcastReceiver mConnectionBroadcastReceiver;
    private ConnectionState mState;
    private PassiveThread mPassiveThread;
    private Peer mPeer;
    private ServerSocket mServerSocket;
    private WifiDirectManager mWifiDirectManager;
    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel mWifiP2pChannel;

    public static ConnectionManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ConnectionManager(context);
        }

        return sInstance;
    }

    private ConnectionManager(Context context) {
        mState = ConnectionState.DISCONNECTED;

        mWifiP2pManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        mWifiP2pChannel = mWifiP2pManager.initialize(context, context.getMainLooper(), null);

        mWifiDirectManager = WifiDirectManager.getInstance(context);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        mConnectionBroadcastReceiver = new ConnectionBroadcastReceiver();
        context.registerReceiver(mConnectionBroadcastReceiver, mIntentFilter);

        try {
            mServerSocket = new ServerSocket(0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mPassiveThread = new PassiveThread(mServerSocket);
        mPassiveThread.start();
    }

    public int getPassiveThreadPort() {
        return mServerSocket.getLocalPort();
    }

    // TODO: What if 2 connection request at the same time (if already connected)
    public void connect(Peer peer) {
        WifiP2pConfig wifiP2pConfig = new WifiP2pConfig();
        wifiP2pConfig.deviceAddress = peer.getAddress();
        wifiP2pConfig.wps.setup = WpsInfo.PBC;

        mPeer = peer;

        mWifiP2pManager.connect(mWifiP2pChannel, wifiP2pConfig, null);
    }

    public void disconnect() {
        if (mActiveThread != null) {
            mActiveThread.interrupt();
            mActiveThread = null;
        }

        mPeer = null;

        // TODO: cancel or remove or both
        mWifiP2pManager.cancelConnect(mWifiP2pChannel, null);
        mWifiP2pManager.removeGroup(mWifiP2pChannel, null);
    }

    private boolean isGroupOwner() {
        WifiP2pDevice ownDevice = mWifiDirectManager.getOwnDevice();

        return ownDevice != null && ownDevice.isGroupOwner();
    }

    private class ConnectionBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, action);

            if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                WifiP2pInfo wifiP2pInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
                Log.d(TAG, wifiP2pInfo.toString());

                NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

                if (networkInfo.isConnected()) {
                    mState = ConnectionState.CONNECTED;
                    Log.d(TAG, "Devices connected");

                    // TODO: fix the fact that group owner is randomly affected
                    // TODO: Fix null exception if group owner is the request initiator
                    if (isGroupOwner()) {
                        return;
                    }

                    mPeer.setLocalAddress(wifiP2pInfo.groupOwnerAddress);

                    mActiveThread = new ActiveThread(ConnectionManager.this, mPeer);
                    mActiveThread.start();
                } else {
                    mState = ConnectionState.DISCONNECTED;
                    Log.d(TAG, "Devices disconnected");
                }
            }
        }
    }
}
