package fr.rsommerard.privacyaware.wifidirect;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import fr.rsommerard.privacyaware.WiFiDirect;
import fr.rsommerard.privacyaware.data.DataManager;
import fr.rsommerard.privacyaware.wifidirect.connection.ConnectionManager;
import fr.rsommerard.privacyaware.wifidirect.connection.ServiceDiscoveryManager;
import fr.rsommerard.privacyaware.wifidirect.device.DeviceManager;

public class WiFiDirectManager {

    private final ServiceDiscoveryManager mServiceDiscoveryManager;

    private final WifiManager mWiFiManager;

    private int mNetId;

    public WiFiDirectManager(final Context context) throws IOException {
        WifiP2pManager wiFiP2pManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);

        WifiP2pManager.Channel wiFiP2pChannel = wiFiP2pManager.initialize(context, context.getMainLooper(), new WifiP2pManager.ChannelListener() {
            @Override
            public void onChannelDisconnected() {
                Log.i(WiFiDirect.TAG, "Channel disconnected");
            }
        });


        ServerSocket serverSocket = new ServerSocket(0);
        Log.i(WiFiDirect.TAG, "Server port: " + serverSocket.getLocalPort());


        WiFiDirect.cleanAllGroupsRegistered(wiFiP2pManager, wiFiP2pChannel);


        mWiFiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);


        DeviceManager deviceManager = new DeviceManager(context);


        String serverPort = Integer.toString(serverSocket.getLocalPort());
        mServiceDiscoveryManager = new ServiceDiscoveryManager(wiFiP2pManager, wiFiP2pChannel, deviceManager, serverPort);
    }

    public void start() {
        disconnectWiFi();

        mServiceDiscoveryManager.startDiscovery();
    }

    public void stop() {
        mServiceDiscoveryManager.stopDiscovery();

        reconnectWiFi();
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
}
