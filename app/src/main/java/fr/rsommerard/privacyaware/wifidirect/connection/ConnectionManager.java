package fr.rsommerard.privacyaware.wifidirect.connection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import fr.rsommerard.privacyaware.wifidirect.connection.thread.CRConnectionThread;
import fr.rsommerard.privacyaware.wifidirect.connection.thread.CSConnectionThread;
import fr.rsommerard.privacyaware.wifidirect.connection.thread.WRConnectionThread;
import fr.rsommerard.privacyaware.wifidirect.connection.thread.WSConnectionThread;
import fr.rsommerard.privacyaware.data.DataManager;
import fr.rsommerard.privacyaware.wifidirect.peer.Peer;
import fr.rsommerard.privacyaware.wifidirect.peer.PeerManager;

public class ConnectionManager {

    private static final String TAG = "PACM";

    private static ConnectionManager sInstance;

    private final Context mContext;
    private final ConnectionBroadcastReceiver mConnectionBroadcastReceiver;
    private final WifiP2pManager mWifiP2pManager;
    private final WifiP2pManager.Channel mWifiP2pChannel;
    private boolean mInitiator;
    private final PeerManager mPeerManager;

    private ConnectionState mState;
    private Thread mConnectionThread;
    private ServerSocket mServerSocket;
    private ScheduledExecutorService mExecutor;

    private enum ConnectionState {
        CONNECTING,
        CONNECTED,
        DISCONNECTED
    }

    public static ConnectionManager getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new ConnectionManager(context);
        }

        return sInstance;
    }

    private ConnectionManager(final Context context) {
        Log.i(TAG, "ConnectionManager()");

        mContext = context;

        mState = ConnectionState.DISCONNECTED;

        mWifiP2pManager = (WifiP2pManager) mContext.getSystemService(Context.WIFI_P2P_SERVICE);
        mWifiP2pChannel = mWifiP2pManager.initialize(mContext, mContext.getMainLooper(), null);

        mPeerManager = PeerManager.getInstance();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        mConnectionBroadcastReceiver = new ConnectionBroadcastReceiver();
        mContext.registerReceiver(mConnectionBroadcastReceiver, intentFilter);

        try {
            mServerSocket = new ServerSocket(0);
            mServerSocket.setSoTimeout(31000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getPassiveThreadPort() {
        //Log.i(TAG, "getPassiveThreadPort()");

        return String.valueOf(mServerSocket.getLocalPort());
    }

    private boolean isConnectingOrConnected() {
        return (mState == ConnectionState.CONNECTING || mState == ConnectionState.CONNECTED);
    }

    public void connect(final Peer peer) {
        //Log.i(TAG, "connect()");

        if (isConnectingOrConnected()) {
            Log.e(TAG, "ConnectionState.CONNECTING");
            return;
        }

        WifiP2pConfig wifiP2pConfig = new WifiP2pConfig();
        wifiP2pConfig.deviceAddress = peer.getAddress();
        wifiP2pConfig.wps.setup = WpsInfo.PBC;
        wifiP2pConfig.groupOwnerIntent = 0;

        mInitiator = true;

        mPeerManager.stopCleaningExecutor();

        mState = ConnectionState.CONNECTING;

        mExecutor = Executors.newSingleThreadScheduledExecutor();
        mExecutor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                //Log.i(TAG, "run()");

                disconnect();
            }
        }, 0, 31000, TimeUnit.MILLISECONDS);

        mWifiP2pManager.connect(mWifiP2pChannel, wifiP2pConfig, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.i(TAG, "connect()::onSuccess()");
            }

            @Override
            public void onFailure(int reason) {
                Log.e(TAG, "connect()::onFailure(): " + reason);
                disconnect();
            }
        });
    }

    public void disconnect() {
        //Log.i(TAG, "disconnect()");

        if (mExecutor != null) {
            mExecutor.shutdown();
        }

        mInitiator = false;

        mWifiP2pManager.cancelConnect(mWifiP2pChannel, null);
        mWifiP2pManager.removeGroup(mWifiP2pChannel, null);

        mState = ConnectionState.DISCONNECTED;

        mPeerManager.startCleaningExecutor();
    }

    public void destroy() {
        Log.i(TAG, "destroy()");

        disconnect();

        mContext.unregisterReceiver(mConnectionBroadcastReceiver);
    }

    private class ConnectionBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            //Log.i(TAG, "onReceive(Context context, Intent intent)");

            String action = intent.getAction();

            if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                final WifiP2pInfo wifiP2pInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
                final NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

                // EXTRA_WIFI_P2P_GROUP need API 18 (Android 4.3)
                // WifiP2pGroup wifiP2pGroup = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_GROUP);

                mWifiP2pManager.requestGroupInfo(mWifiP2pChannel, new WifiP2pManager.GroupInfoListener() {
                    @Override
                    public void onGroupInfoAvailable(WifiP2pGroup wifiP2pGroup) {
                        if (networkInfo.isConnected()) {
                            Log.i(TAG, "Devices connected");

                            if (mExecutor != null) {
                                mExecutor.shutdown();
                            }

                            mState = ConnectionState.CONNECTED;

                            mExecutor = Executors.newSingleThreadScheduledExecutor();
                            mExecutor.scheduleWithFixedDelay(new Runnable() {
                                @Override
                                public void run() {
                                    //Log.i(TAG, "run()");

                                    disconnect();
                                }
                            }, 0, 61000, TimeUnit.MILLISECONDS);

                            WifiP2pDevice groupOwner = wifiP2pGroup.getOwner();

                            Log.d(TAG, "isGroupOwner? " + wifiP2pGroup.isGroupOwner());
                            Log.d(TAG, "Initiator? " + mInitiator);

                            if (mInitiator) {
                                // A
                                if (wifiP2pInfo.isGroupOwner) {
                                    // wait connection and send
                                    mConnectionThread = new WSConnectionThread(ConnectionManager.this, mServerSocket, wifiP2pInfo.isGroupOwner);
                                    mConnectionThread.start();
                                } else {
                                    // connect and send
                                    Peer peer = mPeerManager.getPeer(groupOwner.deviceAddress);

                                    if (peer != null) {
                                        peer.setLocalAddress(wifiP2pInfo.groupOwnerAddress);
                                    }

                                    mConnectionThread = new CSConnectionThread(ConnectionManager.this, peer, wifiP2pInfo.isGroupOwner);
                                    mConnectionThread.start();
                                }
                            } else {
                                // B
                                if (wifiP2pInfo.isGroupOwner) {
                                    // wait connection and receive
                                    mConnectionThread = new WRConnectionThread(ConnectionManager.this, mServerSocket, wifiP2pInfo.isGroupOwner);
                                    mConnectionThread.start();
                                } else {
                                    // connect and receive
                                    Peer peer = mPeerManager.getPeer(groupOwner.deviceAddress);

                                    if (peer != null) {
                                        peer.setLocalAddress(wifiP2pInfo.groupOwnerAddress);
                                    }

                                    mConnectionThread = new CRConnectionThread(ConnectionManager.this, peer, wifiP2pInfo.isGroupOwner);
                                    mConnectionThread.start();
                                }
                            }
                        } else {
                            Log.i(TAG, "Devices disconnected");

                            disconnect();
                        }
                    }
                });
            }
        }
    }
}
