package fr.rsommerard.privacyaware.wifidirect.connection;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import fr.rsommerard.privacyaware.WiFiDirect;

public class ServiceDiscoveryManager {

    private static final String SERVICE_NAME = "_rsp2p";
    private static final String SERVICE_TYPE = "_presence._tcp";

    private static ServiceDiscoveryManager sInstance;

    private final WifiP2pManager mWifiP2pManager;
    private final WifiP2pManager.Channel mWifiP2pChannel;
    private final WifiP2pDnsSdServiceInfo mWifiP2pDnsSdServiceInfo;

    public static ServiceDiscoveryManager getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new ServiceDiscoveryManager(context);
        }

        return sInstance;
    }

    private ServiceDiscoveryManager(final Context context) {
        mWifiP2pManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        mWifiP2pChannel = mWifiP2pManager.initialize(context, context.getMainLooper(), null);

        Map<String, String> record = new HashMap<>();
        record.put("port", "42224");

        mWifiP2pDnsSdServiceInfo = WifiP2pDnsSdServiceInfo.newInstance(SERVICE_NAME, SERVICE_TYPE, record);
        mWifiP2pManager.addLocalService(mWifiP2pChannel, mWifiP2pDnsSdServiceInfo, null);
        mWifiP2pManager.setDnsSdResponseListeners(mWifiP2pChannel, null, new SetDnsSdTxtRecordListener());
        WifiP2pDnsSdServiceRequest wifiP2pDnsSdServiceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        mWifiP2pManager.addServiceRequest(mWifiP2pChannel, wifiP2pDnsSdServiceRequest, null);
    }

    public void start() {
        mWifiP2pManager.discoverServices(mWifiP2pChannel, null);
    }

    public void stop() {
        // https://developer.android.com/reference/android/net/wifi/p2p/WifiP2pManager.html
        mWifiP2pManager.removeLocalService(mWifiP2pChannel, mWifiP2pDnsSdServiceInfo, null);
        mWifiP2pManager.clearLocalServices(mWifiP2pChannel, null);

        mWifiP2pManager.clearServiceRequests(mWifiP2pChannel, null);
    }

    /*private boolean isValidService(final String fullDomainName, final Map<String, String> txtRecordMap, final WifiP2pDevice srcDevice) {
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
    }*/

    private class SetDnsSdTxtRecordListener implements WifiP2pManager.DnsSdTxtRecordListener {

        @Override
        public void onDnsSdTxtRecordAvailable(final String fullDomainName, final Map<String, String> txtRecordMap, final WifiP2pDevice srcDevice) {
            Log.i(WiFiDirect.TAG, srcDevice.toString());

            /*if (isValidService(fullDomainName, txtRecordMap, srcDevice)) {
                Device device = new Device();
                device.setName(srcDevice.deviceName);
                device.setAddress(srcDevice.deviceAddress);
                device.setPort(txtRecordMap.get("port"));
                device.setTimestamp(Long.toString(System.currentTimeMillis()));

                mDeviceManager.addDevice(device);
            }*/
        }
    }
}
