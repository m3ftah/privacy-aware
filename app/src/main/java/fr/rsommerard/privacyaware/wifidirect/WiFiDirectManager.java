package fr.rsommerard.privacyaware.wifidirect;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import fr.rsommerard.privacyaware.WiFiDirect;
import fr.rsommerard.privacyaware.wifidirect.connection.ConnectionManager;
import fr.rsommerard.privacyaware.wifidirect.device.DeviceManager;
import fr.rsommerard.privacyaware.wifidirect.exception.NotStartedException;

public class WiFiDirectManager {

    private static final int PROCESS_INTERVAL = 60000;

    private final ServiceDiscoveryManager mServiceDiscoveryManager;
    private final WifiManager mWiFiManager;
    private final ConnectionManager mConnectionManager;
    private final DeviceManager mDeviceManager;
    private boolean mIsStarted;

//    private ScheduledExecutorService mExecutor;

    private int mNetId;

    public WiFiDirectManager(final Context context) throws IOException {
        WifiP2pManager wiFiP2pManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);

        WifiP2pManager.ChannelListener initializeChannelListener = new CustomChannelListener();
        WifiP2pManager.Channel wiFiP2pChannel = wiFiP2pManager.initialize(context, context.getMainLooper(), initializeChannelListener);


        WiFiDirect.cleanAllGroupsRegistered(wiFiP2pManager, wiFiP2pChannel);


        mWiFiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);


        mDeviceManager = new DeviceManager(context);


        mConnectionManager = new ConnectionManager(wiFiP2pManager, wiFiP2pChannel);

        String port = mConnectionManager.getServerSocketPort();
        mServiceDiscoveryManager = new ServiceDiscoveryManager(wiFiP2pManager, wiFiP2pChannel, mDeviceManager, port);

        mIsStarted = false;
    }

    // To be able to decide if we want to send or receive data. For instance, we can disable WiFi-Direct
    // data sharing when user is in a POI.
    public void start(final Context context) {
        if (mIsStarted) {
            return;
        }

        disconnectWiFi();

        mServiceDiscoveryManager.startDiscovery();
        mConnectionManager.start(context);

//        mExecutor = Executors.newSingleThreadScheduledExecutor();
//
//        mExecutor.scheduleAtFixedRate(new Runnable() {
//            @Override
//            public void run() {
//                //process();
//            }
//        }, PROCESS_INTERVAL, PROCESS_INTERVAL, TimeUnit.MILLISECONDS);

        mIsStarted = true;
    }

    // TODO: make it private and rename it to process()
    public void connect() throws NotStartedException {
        if (!mIsStarted) {
            throw new NotStartedException();
        }

        if (mDeviceManager.hasDevices()) {
            mConnectionManager.connect(mDeviceManager.getDevice());
        } else {
            Log.d(WiFiDirect.TAG, "No device available");
        }
    }

    public void disconnect() {
        mConnectionManager.disconnect();
    }

    public void stop(final Context context) {
        mConnectionManager.stop(context);
        mServiceDiscoveryManager.stopDiscovery();

        reconnectWiFi();

        mIsStarted = false;
    }

    private void disconnectWiFi() {
        mNetId = mWiFiManager.getConnectionInfo().getNetworkId();

        if (mNetId != -1)
            mWiFiManager.disableNetwork(mNetId);
    }

    private void reconnectWiFi() {
        if (mNetId != -1)
            mWiFiManager.enableNetwork(mNetId, true);
    }

    private class CustomChannelListener implements WifiP2pManager.ChannelListener {
        @Override
        public void onChannelDisconnected() {
            Log.i(WiFiDirect.TAG, "Channel disconnected");
        }
    }
}
