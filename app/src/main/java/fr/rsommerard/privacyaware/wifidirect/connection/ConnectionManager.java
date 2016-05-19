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

import fr.rsommerard.privacyaware.WiFiDirect;
import fr.rsommerard.privacyaware.wifidirect.connection.thread.CRConnectionThread;
import fr.rsommerard.privacyaware.wifidirect.connection.thread.CSConnectionThread;
import fr.rsommerard.privacyaware.wifidirect.connection.thread.WRConnectionThread;
import fr.rsommerard.privacyaware.wifidirect.connection.thread.WSConnectionThread;
import fr.rsommerard.privacyaware.wifidirect.device.Device;
import fr.rsommerard.privacyaware.wifidirect.device.DeviceManager;

public class ConnectionManager {

    private static ConnectionManager sInstance;

    private final Context mContext;
    private final ConnectionBroadcastReceiver mConnectionBroadcastReceiver;
    private final WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel mWifiP2pChannel;
    private boolean mInitiator;
    private final DeviceManager mDeviceManager;

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
        Log.i(WiFiDirect.TAG, "ConnectionManager");

        mContext = context;

        mState = ConnectionState.DISCONNECTED;

        mWifiP2pManager = (WifiP2pManager) mContext.getSystemService(Context.WIFI_P2P_SERVICE);
        mWifiP2pChannel = mWifiP2pManager.initialize(mContext, mContext.getMainLooper(), null);

        mDeviceManager = DeviceManager.getInstance();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        mConnectionBroadcastReceiver = new ConnectionBroadcastReceiver();
        mContext.registerReceiver(mConnectionBroadcastReceiver, intentFilter);

        try {
            mServerSocket = new ServerSocket(0);
            Log.i(WiFiDirect.TAG, "Port: " + getPassiveThreadPort());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getPassiveThreadPort() {
        //Log.i(WiFiDirect.TAG, "getPassiveThreadPort()");

        return String.valueOf(mServerSocket.getLocalPort());
    }

    private boolean isConnectingOrConnected() {
        if (mState == ConnectionState.CONNECTING) {
            Log.e(WiFiDirect.TAG, "ConnectionState.CONNECTING");
            return true;
        }

        if (mState == ConnectionState.CONNECTED) {
            Log.e(WiFiDirect.TAG, "ConnectionState.CONNECTED");
            return true;
        }

        return false;
    }

    public void connect(final Device device) {
        Log.i(WiFiDirect.TAG, "connect");

        if (isConnectingOrConnected()) {
            Log.e(WiFiDirect.TAG, "ConnectionState.CONNECTING");
            return;
        }

        WifiP2pConfig wifiP2pConfig = new WifiP2pConfig();
        wifiP2pConfig.deviceAddress = device.getAddress();
        wifiP2pConfig.wps.setup = WpsInfo.PBC;
        wifiP2pConfig.groupOwnerIntent = 0;

        mDeviceManager.stopCleaningExecutor();

        mState = ConnectionState.CONNECTING;

        ServiceDiscoveryManager.getInstance(mContext, getPassiveThreadPort()).stopDiscoveryExecutor();

        mExecutor = Executors.newSingleThreadScheduledExecutor();
        mExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Log.i(WiFiDirect.TAG, "connect::run");

                disconnect();
            }
        }, 61000, 61000, TimeUnit.MILLISECONDS);

        mInitiator = true;

        mWifiP2pManager.connect(mWifiP2pChannel, wifiP2pConfig, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.i(WiFiDirect.TAG, "connect::onSuccess");
            }

            @Override
            public void onFailure(int reason) {
                if (WifiP2pManager.BUSY == reason) {
                    Log.e(WiFiDirect.TAG, "connect::onFailure: BUSY");
                } else if (WifiP2pManager.ERROR == reason) {
                    Log.e(WiFiDirect.TAG, "connect::onFailure: ERROR");
                } else if (WifiP2pManager.P2P_UNSUPPORTED == reason) {
                    Log.e(WiFiDirect.TAG, "connect::onFailure: P2P_UNSUPPORTED");
                } else {
                    Log.e(WiFiDirect.TAG, "connect::onFailure: " + reason);
                }

                reset();
            }
        });
    }

    public void disconnect() {
        Log.i(WiFiDirect.TAG, "disconnect");

        if (mExecutor != null) {
            mExecutor.shutdown();
        }

        mInitiator = false;

        mWifiP2pManager.cancelConnect(mWifiP2pChannel, null);
        mWifiP2pManager.removeGroup(mWifiP2pChannel, null);

        mState = ConnectionState.DISCONNECTED;

        mDeviceManager.startCleaningExecutor();

        ServiceDiscoveryManager.getInstance(mContext, getPassiveThreadPort()).startDiscoveryExecutor();
    }

    private void reset() {
        Log.i(WiFiDirect.TAG, "reset");

        disconnect();

        mWifiP2pChannel = mWifiP2pManager.initialize(mContext, mContext.getMainLooper(), null);
    }

    public void destroy() {
        Log.i(WiFiDirect.TAG, "destroy");

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

                if (wifiP2pInfo == null) {
                    disconnect();
                    return;
                }

                // EXTRA_WIFI_P2P_GROUP need API 18 (Android 4.3)
                // WifiP2pGroup wifiP2pGroup = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_GROUP);

                mWifiP2pManager.requestGroupInfo(mWifiP2pChannel, new WifiP2pManager.GroupInfoListener() {
                    @Override
                    public void onGroupInfoAvailable(WifiP2pGroup wifiP2pGroup) {
                        if (networkInfo.isConnected()) {
                            Log.i(WiFiDirect.TAG, "Devices connected");

                            if (mState == ConnectionState.CONNECTING) {
                                Log.i(WiFiDirect.TAG, "mState: CONNECTING");
                            } else if (mState == ConnectionState.CONNECTED){
                                Log.i(WiFiDirect.TAG, "mState: CONNECTED");
                            } else {
                                Log.i(WiFiDirect.TAG, "mState: DISCONNECTED");
                            }

                            if (mExecutor != null) {
                                mExecutor.shutdown();
                            }

                            mExecutor = Executors.newSingleThreadScheduledExecutor();
                            mExecutor.scheduleAtFixedRate(new Runnable() {
                                @Override
                                public void run() {
                                    Log.i(WiFiDirect.TAG, "requestGroupInfo::run");

                                    disconnect();
                                }
                            }, 61000, 61000, TimeUnit.MILLISECONDS);

                            mState = ConnectionState.CONNECTED;

                            if (wifiP2pGroup == null) {
                                disconnect();
                                return;
                            }

                            WifiP2pDevice groupOwner = wifiP2pGroup.getOwner();

                            Log.d(WiFiDirect.TAG, "isGroupOwner? " + wifiP2pGroup.isGroupOwner());
                            Log.d(WiFiDirect.TAG, "Initiator? " + mInitiator);

                            if (mInitiator) {
                                // A
                                if (wifiP2pInfo.isGroupOwner) {
                                    // wait connection and send
                                    mConnectionThread = new WSConnectionThread(mContext, mServerSocket);
                                    mConnectionThread.start();
                                } else {
                                    // connect and send
                                    Device device = mDeviceManager.getPeer(groupOwner.deviceAddress);

                                    if (device == null) {
                                        disconnect();
                                        return;
                                    }

                                    device.setLocalAddress(wifiP2pInfo.groupOwnerAddress);
                                    mConnectionThread = new CSConnectionThread(mContext, device);
                                    mConnectionThread.start();
                                }
                            } else {
                                // B
                                if (wifiP2pInfo.isGroupOwner) {
                                    // wait connection and receive
                                    mConnectionThread = new WRConnectionThread(mContext, mServerSocket);
                                    mConnectionThread.start();
                                } else {
                                    // connect and receive
                                    Device device = mDeviceManager.getPeer(groupOwner.deviceAddress);

                                    if (device == null) {
                                        disconnect();
                                        return;
                                    }

                                    device.setLocalAddress(wifiP2pInfo.groupOwnerAddress);
                                    mConnectionThread = new CRConnectionThread(mContext, device);
                                    mConnectionThread.start();
                                }
                            }
                        } else {
                            Log.i(WiFiDirect.TAG, "Devices disconnected");

                            disconnect();
                        }
                    }
                });
            }
        }
    }
}
