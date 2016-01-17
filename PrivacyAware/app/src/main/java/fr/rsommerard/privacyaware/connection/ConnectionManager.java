package fr.rsommerard.privacyaware.connection;

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

import fr.rsommerard.privacyaware.connection.thread.CSConnectionThread;
import fr.rsommerard.privacyaware.connection.thread.WRConnectionThread;
import fr.rsommerard.privacyaware.data.DataManager;
import fr.rsommerard.privacyaware.peer.Peer;
import fr.rsommerard.privacyaware.peer.PeerManager;

public class ConnectionManager {

    private static final String TAG = "PACM";

    private static final int SOCKET_TIMEOUT = 31000;
    private static final int CONNECTION_TIMEOUT = 181000;

    private static ConnectionManager sInstance;

    private final Context mContext;
    private final IntentFilter mIntentFilter;
    private final ConnectionBroadcastReceiver mConnectionBroadcastReceiver;
    private final WifiP2pManager mWifiP2pManager;
    private final WifiP2pManager.Channel mWifiP2pChannel;
    private final ServiceDiscoveryManager mServiceDiscoveryManager;
    private boolean mInitiator;
    private final DataManager mDataManager;
    private final PeerManager mPeerManager;

    private ConnectionState mState;
    private Thread mConnectionThread;
    private ServerSocket mServerSocket;
    private ScheduledExecutorService mExecutor;

    private enum ConnectionState {
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

        mDataManager = DataManager.getInstance();
        mPeerManager = PeerManager.getInstance();

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        mConnectionBroadcastReceiver = new ConnectionBroadcastReceiver();
        mContext.registerReceiver(mConnectionBroadcastReceiver, mIntentFilter);

        try {
            mServerSocket = new ServerSocket(0);
            mServerSocket.setSoTimeout(SOCKET_TIMEOUT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mServiceDiscoveryManager = ServiceDiscoveryManager.getInstance(mContext, String.valueOf(getPassiveThreadPort()));
    }

    public int getPassiveThreadPort() {
        Log.i(TAG, "getPassiveThreadPort()");

        return mServerSocket.getLocalPort();
    }

    private boolean isConnected() {
        return mState == ConnectionState.CONNECTED;
    }

    public void connect(final Peer peer) {
        Log.i(TAG, "connect()");

        if (isConnected()) {
            Log.e(TAG, "isConnected() == true");
            return;
        }

        if (!mDataManager.hasDatas()) {
            Log.e(TAG, "No data");
            return;
        }

        WifiP2pConfig wifiP2pConfig = new WifiP2pConfig();
        wifiP2pConfig.deviceAddress = peer.getAddress();
        wifiP2pConfig.wps.setup = WpsInfo.PBC;
        wifiP2pConfig.groupOwnerIntent = 0;

        mInitiator = true;

        mPeerManager.stopCleaningExecutor();

        mServiceDiscoveryManager.stopDiscovery();
        mWifiP2pManager.connect(mWifiP2pChannel, wifiP2pConfig, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.i(TAG, "onSuccess()");
            }

            @Override
            public void onFailure(int reason) {
                Log.e(TAG, "onFailure(): " + reason);
                disconnect();
            }
        });
    }

    public void disconnect() {
        Log.i(TAG, "disconnect()");

        if (mExecutor != null) {
            mExecutor.shutdown();
        }

        mInitiator = false;

        mWifiP2pManager.cancelConnect(mWifiP2pChannel, null);
        mWifiP2pManager.removeGroup(mWifiP2pChannel, null);

        mState = ConnectionState.DISCONNECTED;

        mPeerManager.startCleaningExecutor();
        mServiceDiscoveryManager.startDiscovery();
    }

    public void destroy() {
        Log.i(TAG, "destroy()");

        mContext.unregisterReceiver(mConnectionBroadcastReceiver);
    }

    private class ConnectionBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            Log.i(TAG, "onReceive(Context context, Intent intent)");

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

                            mState = ConnectionState.CONNECTED;

                            mExecutor = Executors.newSingleThreadScheduledExecutor();
                            mExecutor.scheduleAtFixedRate(new Runnable() {
                                @Override
                                public void run() {
                                    Log.i(TAG, "run()");

                                    disconnect();
                                }
                            }, CONNECTION_TIMEOUT, CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);

                            WifiP2pDevice groupOwner = wifiP2pGroup.getOwner();

                            Log.d(TAG, "isGroupOwner? " + wifiP2pGroup.isGroupOwner());
                            Log.d(TAG, "Initiator: " + mInitiator);

                            if (mInitiator) {
                                // A
                                if (wifiP2pInfo.isGroupOwner) {
                                    // wait connection and send
                                    //mConnectionThread = new WSConnectionThread();
                                } else {
                                    // connect and send
                                    Peer peer = mPeerManager.getPeer(groupOwner.deviceAddress);

                                    if (peer != null) {
                                        peer.setLocalAddress(wifiP2pInfo.groupOwnerAddress);
                                    }

                                    mConnectionThread = new CSConnectionThread(ConnectionManager.this, peer);
                                    mConnectionThread.start();
                                }
                            } else {
                                // B
                                if (wifiP2pInfo.isGroupOwner) {
                                    // wait connection and receive
                                    mConnectionThread = new WRConnectionThread(ConnectionManager.this, mServerSocket);
                                    mConnectionThread.start();
                                } else {
                                    // connect and receive
                                    //mConnectionThread = new CRConnectionThread();
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
