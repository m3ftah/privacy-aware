package fr.rsommerard.privacyaware.wifidirect;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import fr.rsommerard.privacyaware.WiFiDirect;
import fr.rsommerard.privacyaware.data.DataAvailableEvent;
import fr.rsommerard.privacyaware.data.DataManager;
import fr.rsommerard.privacyaware.wifidirect.connection.ConnectionManager;
import fr.rsommerard.privacyaware.wifidirect.device.DeviceManager;
import fr.rsommerard.privacyaware.wifidirect.exception.WiFiException;

public class WiFiDirectManager {

    private final ServiceDiscoveryManager mServiceDiscoveryManager;
    private final WifiManager mWiFiManager;
    private final ConnectionManager mConnectionManager;
    private final DeviceManager mDeviceManager;
    private final DataManager mDataManager;
    private final EventBus mEventBus;

    private int mNetId;

    public WiFiDirectManager(final Context context, final EventBus eventBus) throws IOException, WiFiException {
        WifiP2pManager wiFiP2pManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);

        mEventBus = eventBus;

        WifiP2pManager.ChannelListener initializeChannelListener = new CustomChannelListener();
        WifiP2pManager.Channel wiFiP2pChannel = wiFiP2pManager.initialize(context, context.getMainLooper(), initializeChannelListener);


        WiFiDirect.cleanAllGroupsRegistered(wiFiP2pManager, wiFiP2pChannel);


        mWiFiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        if (!mWiFiManager.isWifiEnabled()) {
            throw new WiFiException("The WiFi is not enabled.");
        }

        mDeviceManager = new DeviceManager(context, mEventBus);

        mDataManager = new DataManager(context, mEventBus);
        WiFiDirect.populateDataTable(mDataManager, 5);

        disconnectWiFi();

        mServiceDiscoveryManager = new ServiceDiscoveryManager(wiFiP2pManager, wiFiP2pChannel, mDeviceManager, mEventBus);
        mEventBus.post(new StartDiscoveryEvent());

        mConnectionManager = new ConnectionManager(context, wiFiP2pManager, wiFiP2pChannel, mServiceDiscoveryManager, mDataManager, mEventBus);
    }

    public void process() {
        if (mDeviceManager.hasDevices() && mDataManager.hasData()) {
            mConnectionManager.connect(mDeviceManager.getDevice());
        } else {
            Log.d(WiFiDirect.TAG, "No device or data to send available");
        }
    }

    // To be able to decide if we want to send or receive data. For instance, we can disable WiFi-Direct
    // data sharing when user is in a POI.
    public void stop(final Context context) {
        mConnectionManager.stop(context);
        mServiceDiscoveryManager.stop();

        mDeviceManager.deleteAll();

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

    public void printData() {
        String str = WiFiDirect.dataListToString(mDataManager.getAllData());

        Log.i(WiFiDirect.TAG, str);
    }

    private class CustomChannelListener implements WifiP2pManager.ChannelListener {
        @Override
        public void onChannelDisconnected() {
            Log.i(WiFiDirect.TAG, "Channel disconnected");
        }
    }
}
