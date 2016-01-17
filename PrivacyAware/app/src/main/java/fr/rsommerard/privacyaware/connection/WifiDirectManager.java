package fr.rsommerard.privacyaware.connection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

public class WifiDirectManager {

    private static final String TAG = "PAWDM";

    private static WifiDirectManager sInstance;

    private boolean mWifiDirectEnabled;

    private final IntentFilter mWifiIntentFilter;
    private final WifiDirectBroadcastReceiver mWifiDirectBroadcastReceiver;

    private final Context mContext;

    public static WifiDirectManager getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new WifiDirectManager(context);
        }

        return sInstance;
    }

    private WifiDirectManager(final Context context) {
        Log.i(TAG, "WifiDirectManager()");

        mContext = context;

        mWifiIntentFilter = new IntentFilter();
        mWifiIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        mWifiDirectBroadcastReceiver = new WifiDirectBroadcastReceiver();
        mContext.registerReceiver(mWifiDirectBroadcastReceiver, mWifiIntentFilter);
    }

    public void purge() {
        Log.i(TAG, "purge()");

        mContext.unregisterReceiver(mWifiDirectBroadcastReceiver);
    }

    public boolean isWifiDirectEnabled() {
        Log.i(TAG, "isWifiDirectEnabled()");

        return mWifiDirectEnabled;
    }

    private class WifiDirectBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            Log.i(TAG, "onReceive()");

            String action = intent.getAction();

            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                int wifiState = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE,
                        WifiP2pManager.WIFI_P2P_STATE_DISABLED);

                mWifiDirectEnabled = (wifiState == WifiP2pManager.WIFI_P2P_STATE_ENABLED);
            }
        }
    }
}
