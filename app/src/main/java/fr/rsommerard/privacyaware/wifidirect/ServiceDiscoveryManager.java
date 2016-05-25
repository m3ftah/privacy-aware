package fr.rsommerard.privacyaware.wifidirect;

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

import fr.rsommerard.privacyaware.WiFiDirect;
import fr.rsommerard.privacyaware.dao.Device;
import fr.rsommerard.privacyaware.wifidirect.device.DeviceManager;

public class ServiceDiscoveryManager {

    private static final String SERVICE_NAME = "_rsp2p";
    private static final String SERVICE_TYPE = "_tcp";

    private static final int SERVICE_DISCOVERY_INTERVAL =  11000;

    private final DeviceManager mDeviceManager;
    private ScheduledExecutorService mExecutor;

    private final WifiP2pManager mWiFiP2pManager;
    private final WifiP2pManager.Channel mWiFiP2pChannel;

    private final String mServerPort;

    public ServiceDiscoveryManager(final WifiP2pManager manager,
                                   final WifiP2pManager.Channel channel,
                                   final DeviceManager deviceManager,
                                   final String serverPort) {

        mWiFiP2pManager = manager;
        mWiFiP2pChannel = channel;

        mServerPort = serverPort;

        mDeviceManager = deviceManager;

        clearService();
    }

    public void startDiscovery() {
        if (mExecutor != null) {
            Log.i(WiFiDirect.TAG, "Discovery already started");
            return;
        }

        Map<String, String> record = new HashMap<>();
        record.put("port", mServerPort);

        WifiP2pDnsSdServiceInfo wifiP2pDnsSdServiceInfo = WifiP2pDnsSdServiceInfo.newInstance(SERVICE_NAME, SERVICE_TYPE, record);

        WifiP2pManager.ActionListener addLocalServiceActionListener = new CustomActionListener(null, "Add local service failed: ");
        mWiFiP2pManager.addLocalService(mWiFiP2pChannel, wifiP2pDnsSdServiceInfo, addLocalServiceActionListener);

        WifiP2pManager.DnsSdServiceResponseListener dnsSdServiceResponseListener = new CustomDnsSdServiceResponseListener();
        CustomDnsSdTxtRecordListener dnsSdTxtRecordListener = new CustomDnsSdTxtRecordListener();
        mWiFiP2pManager.setDnsSdResponseListeners(mWiFiP2pChannel, dnsSdServiceResponseListener, dnsSdTxtRecordListener);

        WifiP2pDnsSdServiceRequest wifiP2pDnsSdServiceRequest = WifiP2pDnsSdServiceRequest.newInstance();

        WifiP2pManager.ActionListener addServiceRequestActionListener = new CustomActionListener(null, "Add service request failed: ");
        mWiFiP2pManager.addServiceRequest(mWiFiP2pChannel, wifiP2pDnsSdServiceRequest, addServiceRequestActionListener);

        mExecutor = Executors.newSingleThreadScheduledExecutor();

        mExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                discover();
            }
        }, SERVICE_DISCOVERY_INTERVAL, SERVICE_DISCOVERY_INTERVAL, TimeUnit.MILLISECONDS);
    }

    private void discover() {
        WifiP2pManager.ActionListener discoverServicesActionListener = new CustomActionListener(null, "Discovery failed: ");
        mWiFiP2pManager.discoverServices(mWiFiP2pChannel, discoverServicesActionListener);
    }

    private void clearService() {
        // https://developer.android.com/reference/android/net/wifi/p2p/WifiP2pManager.html
        WifiP2pManager.ActionListener clearLocalServicesActionListener = new CustomActionListener(null, "Clear local service failed: ");
        mWiFiP2pManager.clearLocalServices(mWiFiP2pChannel, clearLocalServicesActionListener );

        WifiP2pManager.ActionListener clearServiceRequestsActionListener = new CustomActionListener(null, "Clear service requests failed: ");
        mWiFiP2pManager.clearServiceRequests(mWiFiP2pChannel, clearServiceRequestsActionListener);
    }

    public void stopDiscovery() {
        if (mExecutor != null)
            mExecutor.shutdown();

        mExecutor = null;

        clearService();

        WifiP2pManager.ActionListener stopPeerDiscoveryActionListener = new CustomActionListener(null, "Stop peer discovery failed: ");
        mWiFiP2pManager.stopPeerDiscovery(mWiFiP2pChannel, stopPeerDiscoveryActionListener);
    }

    private boolean isValidDnsSdTxtRecord(final String fullDomainName, final Map<String, String> txtRecordMap, final WifiP2pDevice srcDevice) {
        if (fullDomainName == null ||
                !fullDomainName.contains(SERVICE_NAME + "." + SERVICE_TYPE)) {
            return false;
        }

        if (srcDevice.deviceAddress == null ||
                srcDevice.deviceAddress.isEmpty()) {
            return false;
        }

        if (srcDevice.deviceName == null ||
                srcDevice.deviceName.isEmpty()) {
            return false;
        }

        if (txtRecordMap == null ||
                !txtRecordMap.containsKey("port") ||
                txtRecordMap.get("port") == null) {
            return false;
        }

        return true;
    }

    private class CustomDnsSdServiceResponseListener implements WifiP2pManager.DnsSdServiceResponseListener {
        @Override
        public void onDnsSdServiceAvailable(final String instanceName, final String registrationType, final WifiP2pDevice srcDevice) {
            // Nothing
            //Log.i(WiFiDirect.TAG, "DnsSdService available");
        }
    }

    private class CustomDnsSdTxtRecordListener implements WifiP2pManager.DnsSdTxtRecordListener {
        @Override
        public void onDnsSdTxtRecordAvailable(final String fullDomainName, final Map<String, String> txtRecordMap, final WifiP2pDevice srcDevice) {
            // Log.i(WiFiDirect.TAG, "DnsSdTxtRecord available");
            // Log.i(WiFiDirect.TAG, srcDevice.toString());
            Log.i(WiFiDirect.TAG, srcDevice.deviceName + " available");

            if (isValidDnsSdTxtRecord(fullDomainName, txtRecordMap, srcDevice)) {
                Device device = new Device();
                device.setName(srcDevice.deviceName);
                device.setAddress(srcDevice.deviceAddress);
                device.setPort(txtRecordMap.get("port"));
                device.setTimestamp(Long.toString(System.currentTimeMillis()));

                if (mDeviceManager.containDevice(device)) {
                    mDeviceManager.updateDevice(device);
                } else {
                    mDeviceManager.addDevice(device);
                }
            }
        }
    }
}
