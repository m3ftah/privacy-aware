package fr.rsommerard.privacyaware.connection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;

import fr.rsommerard.privacyaware.peer.Peer;

public class ConnectionManager {

    private final String TAG = "PACM";

    private static ConnectionManager sInstance;

    private Context mContext;
    private IntentFilter mIntentFilter;
    private ConnectionBroadcastReceiver mConnectionBroadcastReceiver;
    private ServerSocket mServerSocket;
    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel mWifiP2pChannel;
    private Peer mPeer;
    private ConnectionState mState;

    private enum ConnectionState {
        CONNECTED,
        DISCONNECTED
    }

    public static ConnectionManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ConnectionManager(context);
        }

        return sInstance;
    }

    private ConnectionManager(Context context) {
        Log.i(TAG, "ConnectionManager()");

        mContext = context;

        mState = ConnectionState.DISCONNECTED;

        mWifiP2pManager = (WifiP2pManager) mContext.getSystemService(Context.WIFI_P2P_SERVICE);
        mWifiP2pChannel = mWifiP2pManager.initialize(mContext, mContext.getMainLooper(), null);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        mConnectionBroadcastReceiver = new ConnectionBroadcastReceiver();
        mContext.registerReceiver(mConnectionBroadcastReceiver, mIntentFilter);

        try {
            mServerSocket = new ServerSocket(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getPassiveThreadPort() {
        Log.i(TAG, "getPassiveThreadPort()");

        return mServerSocket.getLocalPort();
    }

    private boolean isConnected() {
        return mState == ConnectionState.CONNECTED;
    }

    public void connect(Peer peer) {
        Log.i(TAG, "connect()");

        if (isConnected()) {
            Log.d(TAG, "isConnected() == true");
            return;
        }

        WifiP2pConfig wifiP2pConfig = new WifiP2pConfig();
        wifiP2pConfig.deviceAddress = peer.getAddress();
        wifiP2pConfig.wps.setup = WpsInfo.PBC;
        wifiP2pConfig.groupOwnerIntent = 0;

        mPeer = peer;

        mWifiP2pManager.connect(mWifiP2pChannel, wifiP2pConfig, null);
    }

    public void disconnect() {
        Log.i(TAG, "disconnect()");

        mPeer = null;

        mWifiP2pManager.cancelConnect(mWifiP2pChannel, null);
        mWifiP2pManager.removeGroup(mWifiP2pChannel, null);

        mState = ConnectionState.DISCONNECTED;
    }

    public void destroy() {
        Log.i(TAG, "destroy()");

        mContext.unregisterReceiver(mConnectionBroadcastReceiver);
    }

    private class ConnectionBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive()");

            String action = intent.getAction();

            if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                WifiP2pInfo wifiP2pInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
                Log.d(TAG, wifiP2pInfo.toString());

                NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

                if (networkInfo.isConnected()) {
                    Log.d(TAG, "Devices connected");

                    mState = ConnectionState.CONNECTED;

                    if (wifiP2pInfo.isGroupOwner) {
                        //disconnect();
                    }
                } else {
                    Log.d(TAG, "Devices disconnected");

                    disconnect();
                }
            }
        }
    }
}