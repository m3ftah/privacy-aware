package fr.rsommerard.privacyaware.wifidirect.connection;

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
    private static final String SERVICE_TYPE = "_presence._tcp";

    private static final int SERVICE_DISCOVERY_INTERVAL = 11000;

    private final DeviceManager mDeviceManager;
    private ScheduledExecutorService mExecutor;

    private final WifiP2pManager mWifiP2pManager;
    private final WifiP2pManager.Channel mWifiP2pChannel;

    private final String mServerPort;

    public ServiceDiscoveryManager(final WifiP2pManager manager,
                                   final WifiP2pManager.Channel channel,
                                   final DeviceManager deviceManager,
                                   final String serverPort) {

        mWifiP2pManager = manager;
        mWifiP2pChannel = channel;

        mServerPort = serverPort;

        mDeviceManager = deviceManager;

        clearService();

        initializeService();
    }

    private void initializeService() {
        Map<String, String> record = new HashMap<>();
        record.put("port", mServerPort);

        WifiP2pDnsSdServiceInfo wifiP2pDnsSdServiceInfo = WifiP2pDnsSdServiceInfo.newInstance(SERVICE_NAME, SERVICE_TYPE, record);

        mWifiP2pManager.addLocalService(mWifiP2pChannel, wifiP2pDnsSdServiceInfo, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // Nothing
                Log.i(WiFiDirect.TAG, "Local service added");
            }

            @Override
            public void onFailure(int reason) {
                Log.e(WiFiDirect.TAG, "Add local service failed: " +
                        WiFiDirect.getActionListenerFailureName(reason));
            }
        });

        mWifiP2pManager.setDnsSdResponseListeners(mWifiP2pChannel, new WifiP2pManager.DnsSdServiceResponseListener() {
            @Override
            public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice srcDevice) {
                // Nothing
                Log.i(WiFiDirect.TAG, "DnsSdService available");
            }
        }, new WifiP2pManager.DnsSdTxtRecordListener() {
            @Override
            public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> txtRecordMap, WifiP2pDevice srcDevice) {
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
        });

        WifiP2pDnsSdServiceRequest wifiP2pDnsSdServiceRequest = WifiP2pDnsSdServiceRequest.newInstance();

        mWifiP2pManager.addServiceRequest(mWifiP2pChannel, wifiP2pDnsSdServiceRequest, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // Nothing
                Log.i(WiFiDirect.TAG, "Service request added");
            }

            @Override
            public void onFailure(int reason) {
                Log.e(WiFiDirect.TAG, "Add service request failed: " +
                        WiFiDirect.getActionListenerFailureName(reason));
            }
        });
    }

    public void startDiscovery() {
        if (mExecutor != null) {
            Log.i(WiFiDirect.TAG, "Discovery already started");
            return;
        }

        initializeService();

        mExecutor = Executors.newSingleThreadScheduledExecutor();

        mExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                discover();
            }
        }, SERVICE_DISCOVERY_INTERVAL, SERVICE_DISCOVERY_INTERVAL, TimeUnit.MILLISECONDS);
    }

    private void discover() {
        mWifiP2pManager.discoverServices(mWifiP2pChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.i(WiFiDirect.TAG, "Discovery started");
            }

            @Override
            public void onFailure(int reason) {
                Log.e(WiFiDirect.TAG, "Discovery failed: " +
                        WiFiDirect.getActionListenerFailureName(reason));
            }
        });
    }

    private void clearService() {
        // https://developer.android.com/reference/android/net/wifi/p2p/WifiP2pManager.html
        mWifiP2pManager.clearLocalServices(mWifiP2pChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // Nothing
                Log.i(WiFiDirect.TAG, "Clear local service succeeded");
            }

            @Override
            public void onFailure(int reason) {
                Log.e(WiFiDirect.TAG, "Clear local service failed: " +
                        WiFiDirect.getActionListenerFailureName(reason));
            }
        });

        mWifiP2pManager.clearServiceRequests(mWifiP2pChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // Nothing
                Log.i(WiFiDirect.TAG, "Clear service requests succeeded");
            }

            @Override
            public void onFailure(int reason) {
                Log.e(WiFiDirect.TAG, "Clear service requests failed: " +
                        WiFiDirect.getActionListenerFailureName(reason));
            }
        });
    }

    public void stopDiscovery() {
        if (mExecutor != null)
            mExecutor.shutdown();

        mExecutor = null;

        clearService();

        mWifiP2pManager.stopPeerDiscovery(mWifiP2pChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // Nothing
                Log.i(WiFiDirect.TAG, "Stop peer discovery succeeded");
            }

            @Override
            public void onFailure(int reason) {
                Log.e(WiFiDirect.TAG, "Stop peer discovery failed: " +
                        WiFiDirect.getActionListenerFailureName(reason));
            }
        });
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
}
