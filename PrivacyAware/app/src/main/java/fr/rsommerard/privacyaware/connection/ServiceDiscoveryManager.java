package fr.rsommerard.privacyaware.connection;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import fr.rsommerard.privacyaware.peer.Peer;
import fr.rsommerard.privacyaware.peer.PeerManager;

public class ServiceDiscoveryManager {

    private final int TIMEOUT = 11000;

    private final String SERVICE_NAME = "_rsp2p";
    private final String SERVICE_TYPE = "_presence._tcp";

    private static ServiceDiscoveryManager sInstance;

    private Timer mTimer;
    private PeerManager mPeerManager;
    private WifiP2pDnsSdServiceInfo mWifiP2pDnsSdServiceInfo;
    private WifiP2pDnsSdServiceRequest mWifiP2pDnsSdServiceRequest;
    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel mWifiP2pChannel;

    public static ServiceDiscoveryManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ServiceDiscoveryManager(context);
        }

        return sInstance;
    }

    private ServiceDiscoveryManager(Context context) {
        mWifiP2pManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        mWifiP2pChannel = mWifiP2pManager.initialize(context, context.getMainLooper(), null);

        mPeerManager = PeerManager.getInstance();

        ConnectionManager connectionManager = ConnectionManager.getInstance(context);

        Map<String, String> record = new HashMap<>();
        record.put("port", String.valueOf(connectionManager.getPassiveThreadPort()));

        mWifiP2pDnsSdServiceInfo = WifiP2pDnsSdServiceInfo.newInstance(SERVICE_NAME, SERVICE_TYPE, record);
        mWifiP2pManager.addLocalService(mWifiP2pChannel, mWifiP2pDnsSdServiceInfo, null);
        mWifiP2pManager.setDnsSdResponseListeners(mWifiP2pChannel, null, new SetDnsSdTxtRecordListener());
        mWifiP2pDnsSdServiceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        mWifiP2pManager.addServiceRequest(mWifiP2pChannel, mWifiP2pDnsSdServiceRequest, null);

        mTimer = new Timer();
    }

    public void startDiscovery() {
        mTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                mWifiP2pManager.discoverServices(mWifiP2pChannel, null);
            }
        }, 0, TIMEOUT);
    }

    public void stopDiscovery() {
        mTimer.cancel();
        mTimer.purge();
    }

    private class SetDnsSdTxtRecordListener implements WifiP2pManager.DnsSdTxtRecordListener {

        @Override
        public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> txtRecordMap, WifiP2pDevice srcDevice) {
            if (txtRecordMap.isEmpty() || !txtRecordMap.containsKey("port")) {
                return;
            }

            if (!fullDomainName.contains(SERVICE_NAME)) {
                return;
            }

            if (srcDevice.deviceName.isEmpty()) {
                return;
            }

            mPeerManager.addPeer(new Peer(srcDevice, txtRecordMap.get("port")));
        }
    }
}
