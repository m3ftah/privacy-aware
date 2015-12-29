package fr.rsommerard.privacyaware.connection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

public class WifiDirectManager {

    private final String TAG = WifiDirectManager.class.getSimpleName();

    private static WifiDirectManager sInstance;

    private boolean mWifiDirectEnabled;

    private IntentFilter mWifiIntentFilter;
    private WifiP2pDevice mOwnDevice;
    private WifiDirectBroadcastReceiver mWifiDirectBroadcastReceiver;

    public static WifiDirectManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new WifiDirectManager(context);
        }

        return sInstance;
    }

    private WifiDirectManager(Context context) {
        mWifiIntentFilter = new IntentFilter();
        mWifiIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        mWifiDirectBroadcastReceiver = new WifiDirectBroadcastReceiver();
        context.registerReceiver(mWifiDirectBroadcastReceiver, mWifiIntentFilter);
    }

    public boolean isWifiDirectEnabled() {
        return mWifiDirectEnabled;
    }

    public WifiP2pDevice getOwnDevice() {
        return mOwnDevice;
    }

    private class WifiDirectBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, action);

            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                int wifiState = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE,
                        WifiP2pManager.WIFI_P2P_STATE_DISABLED);

                mWifiDirectEnabled = (wifiState == WifiP2pManager.WIFI_P2P_STATE_ENABLED);
            }

            if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                mOwnDevice = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            }
        }
    }
}
