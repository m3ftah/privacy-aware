package fr.rsommerard.privacyaware.connection;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import fr.rsommerard.privacyaware.peer.Peer;
import fr.rsommerard.privacyaware.peer.PeerManager;

public class ServiceDiscoveryManager {

    private static final String TAG = "PASDM";

    private static final int DELAY = 17000;

    private static final String SERVICE_NAME = "_rsp2p";
    private static final String SERVICE_TYPE = "_presence._tcp";

    private static ServiceDiscoveryManager sInstance;

    private ScheduledExecutorService mExecutor;

    private final PeerManager mPeerManager;
    private final WifiP2pManager mWifiP2pManager;
    private final WifiP2pManager.Channel mWifiP2pChannel;

    public static ServiceDiscoveryManager getInstance(final Context context, final String port) {
        if (sInstance == null) {
            sInstance = new ServiceDiscoveryManager(context, port);
        }

        return sInstance;
    }

    private ServiceDiscoveryManager(final Context context, final String port) {
        //Log.i(TAG, "ServiceDiscoveryManager(Context context)");

        mWifiP2pManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        mWifiP2pChannel = mWifiP2pManager.initialize(context, context.getMainLooper(), null);

        mPeerManager = PeerManager.getInstance();

        Map<String, String> record = new HashMap<>();
        record.put("port", port);

        WifiP2pDnsSdServiceInfo wifiP2pDnsSdServiceInfo = WifiP2pDnsSdServiceInfo.newInstance(SERVICE_NAME, SERVICE_TYPE, record);
        mWifiP2pManager.addLocalService(mWifiP2pChannel, wifiP2pDnsSdServiceInfo, null);
        mWifiP2pManager.setDnsSdResponseListeners(mWifiP2pChannel, null, new SetDnsSdTxtRecordListener());
        WifiP2pDnsSdServiceRequest wifiP2pDnsSdServiceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        mWifiP2pManager.addServiceRequest(mWifiP2pChannel, wifiP2pDnsSdServiceRequest, null);
    }

    public void startDiscovery() {
        Log.i(TAG, "startDiscovery()");

        mExecutor = Executors.newSingleThreadScheduledExecutor();
        mExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "run()");

                mWifiP2pManager.discoverServices(mWifiP2pChannel, null);
            }
        }, 0, DELAY, TimeUnit.MILLISECONDS);
    }

    public void stopDiscovery() {
        //Log.i(TAG, "stopDiscovery()");

        if (mExecutor != null) {
            mExecutor.shutdown();
        }
    }

    public void destroy() {
        Log.i(TAG, "destroy()");

        if (mExecutor != null) {
            mExecutor.shutdown();
        }

        mWifiP2pManager.clearServiceRequests(mWifiP2pChannel, null);
        mWifiP2pManager.clearLocalServices(mWifiP2pChannel, null);
    }

    private boolean isValidService(final String fullDomainName, final Map<String, String> txtRecordMap, final WifiP2pDevice srcDevice) {
        if (txtRecordMap.isEmpty() || !txtRecordMap.containsKey("port")) {
            return false;
        }

        if (!fullDomainName.contains(SERVICE_NAME)) {
            return false;
        }

        if (srcDevice.deviceAddress == null || srcDevice.deviceAddress.isEmpty()) {
            return false;
        }

        if (srcDevice.deviceName == null || srcDevice.deviceName.isEmpty()) {
            return false;
        }

        return true;
    }

    private class SetDnsSdTxtRecordListener implements WifiP2pManager.DnsSdTxtRecordListener {

        @Override
        public void onDnsSdTxtRecordAvailable(final String fullDomainName, final Map<String, String> txtRecordMap, final WifiP2pDevice srcDevice) {
            Log.i(TAG, "onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> txtRecordMap, WifiP2pDevice srcDevice)");

            if (isValidService(fullDomainName, txtRecordMap, srcDevice)) {
                mPeerManager.addPeer(new Peer(srcDevice, txtRecordMap.get("port")));
            }
        }
    }
}
