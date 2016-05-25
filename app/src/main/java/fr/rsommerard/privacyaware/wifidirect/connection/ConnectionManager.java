package fr.rsommerard.privacyaware.wifidirect.connection;

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

import fr.rsommerard.privacyaware.WiFiDirect;
import fr.rsommerard.privacyaware.dao.Device;
import fr.rsommerard.privacyaware.wifidirect.CustomActionListener;

public class ConnectionManager {

    private static final int CONNECTION_TIMEOUT = 120000;

    private final WifiP2pManager mWifiP2pManager;
    private final WifiP2pManager.Channel mWifiP2pChannel;
    private final ConnectionBroadcastReceiver mConnectionBroadcastReceiver;
    private final ServerSocket mServerSocket;

    private NetworkInfo mNetworkInfo;

    private long mLastConnectionTimestamp;
    private Device mDevice;

    public ConnectionManager(final WifiP2pManager manager,
                             final WifiP2pManager.Channel channel) throws IOException {

        mWifiP2pManager = manager;
        mWifiP2pChannel = channel;

        mServerSocket = new ServerSocket(0);
        Log.i(WiFiDirect.TAG, "ServerSocket port: " + getServerSocketPort());

        mConnectionBroadcastReceiver = new ConnectionBroadcastReceiver();
    }

    public String getServerSocketPort() {
        return Integer.toString(mServerSocket.getLocalPort());
    }

    public void start(final Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        context.registerReceiver(mConnectionBroadcastReceiver, intentFilter);
    }

    public void stop(final Context context) {
        disconnect();

        try {
            context.unregisterReceiver(mConnectionBroadcastReceiver);
        } catch(IllegalArgumentException e) {
            // Nothing: BroadcastReceiver is just not registered
        }
    }

    // TODO: remove this method, replace it by sendDataToDevice(Date, Device)
    public void connect(final Device device) {
        if (mNetworkInfo == null) {
            Log.e(WiFiDirect.TAG, "Network info not yet available");
            return;
        }

        if (mNetworkInfo.isConnectedOrConnecting()) {
            Log.e(WiFiDirect.TAG, "Device connected or connecting");
            return;
        }

        mDevice = device;

        WifiP2pConfig wifiP2pConfig = new WifiP2pConfig();
        wifiP2pConfig.deviceAddress = device.getAddress();
        wifiP2pConfig.wps.setup = WpsInfo.PBC;
        wifiP2pConfig.groupOwnerIntent = 0;

        // TODO: see if discovery interfere

        mWifiP2pManager.connect(mWifiP2pChannel, wifiP2pConfig, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.i(WiFiDirect.TAG, "Connect succeeded");
            }

            @Override
            public void onFailure(int reason) {
                Log.e(WiFiDirect.TAG, "Connect failed: " +
                        WiFiDirect.getActionListenerFailureName(reason));
                disconnect();
            }
        });
    }

//    public void sendDataToDevice(final Data data, final Device device) {
//        if (mState == ConnectionState.CONNECTED || mState == ConnectionState.CONNECTING) {
//            return;
//            long limit = new Date(System.currentTimeMillis() - CONNECTION_TIMEOUT).getTime();
//            if (mLastConnectionTimestamp < limit) {
//                disconnect();
//                return;
//            }
//        }
//
//        WifiP2pConfig wifiP2pConfig = new WifiP2pConfig();
//        wifiP2pConfig.deviceAddress = device.getAddress();
//        wifiP2pConfig.wps.setup = WpsInfo.PBC;
//        wifiP2pConfig.groupOwnerIntent = 0;
//
//        // TODO: see if discovery interfere
//
//        mWifiP2pManager.connect(mWifiP2pChannel, wifiP2pConfig, new WifiP2pManager.ActionListener() {
//            @Override
//            public void onSuccess() {
//                Log.i(WiFiDirect.TAG, "Connect succeeded");
//                mState = ConnectionState.CONNECTING;
//            }
//
//            @Override
//            public void onFailure(int reason) {
//                Log.e(WiFiDirect.TAG, "Connect failed: " +
//                        WiFiDirect.getActionListenerFailureName(reason));
//                disconnect();
//            }
//        });
//    }

    // TODO: change to private
    public void disconnect() {
        WifiP2pManager.ActionListener cancelConnectActionListener = new CustomActionListener("Cancel connect succeeded", "Cancel connect failed: ");
        mWifiP2pManager.cancelConnect(mWifiP2pChannel, cancelConnectActionListener);

        WifiP2pManager.ActionListener removeGroupActionListener = new CustomActionListener("Remove group succeeded", "Remove group failed: ");
        mWifiP2pManager.removeGroup(mWifiP2pChannel, removeGroupActionListener);
    }

    private class ConnectionBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();

            if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                mNetworkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

                WifiP2pInfo wifiP2pInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);

                Log.d(WiFiDirect.TAG, wifiP2pInfo.toString());
                Log.d(WiFiDirect.TAG, mNetworkInfo.toString());

                if (!mNetworkInfo.isConnected()) {
                    mDevice = null;
                    return;
                }
            }
        }
    }
}
