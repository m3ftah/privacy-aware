package fr.rsommerard.privacyaware.wifidirect;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import fr.rsommerard.privacyaware.WiFiDirect;
import fr.rsommerard.privacyaware.data.DataManager;
import fr.rsommerard.privacyaware.wifidirect.connection.ConnectionManager;
import fr.rsommerard.privacyaware.wifidirect.connection.ServiceDiscoveryManager;
import fr.rsommerard.privacyaware.wifidirect.device.DeviceManager;

public class WiFiDirectManager {

    private static WiFiDirectManager sInstance;

    private final Context mContext;
    private final DataManager mDataManager;
    private final WifiP2pManager mWifiP2pManager;
    private final WifiP2pManager.Channel mWifiP2pChannel;

    private ConnectionManager mConnectionManager;
    private ServiceDiscoveryManager mServiceDiscoveryManager;
    private DeviceManager mDeviceManager;
    private WifiManager mWifi;
    private int mNetId;

    public static WiFiDirectManager getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new WiFiDirectManager(context);
        }

        return sInstance;
    }

    private WiFiDirectManager(final Context context) {
        mContext = context;

        mDeviceManager = DeviceManager.getInstance(mContext);
        mDataManager = DataManager.getInstance(mContext);

        mWifiP2pManager = (WifiP2pManager) mContext.getSystemService(Context.WIFI_P2P_SERVICE);

        mWifiP2pChannel = mWifiP2pManager.initialize(mContext, mContext.getMainLooper(), new WifiP2pManager.ChannelListener() {
            @Override
            public void onChannelDisconnected() {
                Log.i(WiFiDirect.TAG, "Channel disconnected");
            }
        });

        mServiceDiscoveryManager = ServiceDiscoveryManager.getInstance(mContext, mWifiP2pManager, mWifiP2pChannel);

        //mConnectionManager = ConnectionManager.getInstance(mContext);
        //mServiceDiscoveryManager = ServiceDiscoveryManager.getInstance(mContext);

        // TODO: is it useful
        /*Random random = new Random();
        int delay = random.nextInt(181000 - 17000) + 17000;
        Log.d(WiFiDirect.TAG, "Delay: " + delay);

        mExecutor = Executors.newSingleThreadScheduledExecutor();
        mExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Log.i(WiFiDirect.TAG, "run()");

                process();
            }
        }, delay, 181000, TimeUnit.MILLISECONDS);*/
    }

    public void start() {

        WiFiDirect.cleanAllGroupsRegistered(mWifiP2pManager, mWifiP2pChannel);

        mWifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        mNetId = mWifi.getConnectionInfo().getNetworkId();

        if (mNetId != -1)
            mWifi.disableNetwork(mNetId);

        mServiceDiscoveryManager.start();

        /*if (!mDeviceManager.hasDevices()) {
            Log.d(WiFiDirect.TAG, "No peers available");
            return;
        }

        if (!mDataManager.hasData()) {
            Log.e(WiFiDirect.TAG, "No data");
            return;
        }

        Device device = mDeviceManager.getPeer();

        mConnectionManager.connect(device);*/
    }

    public void stop() {
        mServiceDiscoveryManager.stop();
        if (mNetId != -1)
            mWifi.enableNetwork(mNetId, true);
    }
}
