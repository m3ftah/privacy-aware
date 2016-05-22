package fr.rsommerard.privacyaware.wifidirect.connection;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.util.Log;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import fr.rsommerard.privacyaware.WiFiDirect;
import fr.rsommerard.privacyaware.dao.Device;
import fr.rsommerard.privacyaware.wifidirect.device.DeviceManager;

// TODO: Explore Handler instead of Executor

public class ServiceDiscoveryManager {

    private static final String SERVICE_NAME = "_rsp2p";
    private static final String SERVICE_TYPE = "_presence._tcp";

    private static final int INITIAL_SERVICE_DISCOVERY_INTERVAL = 7000;
    private static final int MAX_SERVICE_DISCOVERY_INTERVAL = 60000 * 60;

    private static ServiceDiscoveryManager sInstance;

    private final DeviceManager mDeviceManager;
    private ScheduledExecutorService mExecutor;
    private boolean isNewDeviceFoundDuringLastFiveMinutes;
    private int mInterval;

    private enum State {
        START,
        STOP
    }

    private State mState;

    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel mWifiP2pChannel;

    public static ServiceDiscoveryManager getInstance(final Context context,
                                                      final WifiP2pManager manager,
                                                      final WifiP2pManager.Channel channel) {
        if (sInstance == null) {
            sInstance = new ServiceDiscoveryManager(context, manager, channel);
        }

        return sInstance;
    }

    private ServiceDiscoveryManager(final Context context,
                                    final WifiP2pManager manager,
                                    final WifiP2pManager.Channel channel) {
        mWifiP2pManager = manager;
        mWifiP2pChannel = channel;

        mInterval = INITIAL_SERVICE_DISCOVERY_INTERVAL;
        isNewDeviceFoundDuringLastFiveMinutes = true;

        mState = State.STOP;

        mDeviceManager = DeviceManager.getInstance(context);

        clearLocalServicesAndServiceRequests();

        mExecutor = Executors.newSingleThreadScheduledExecutor();
    }

    public void start() {
        if (mState == State.START)  {
            Log.i(WiFiDirect.TAG, "Discovery already started");
            return;
        }

        if (mExecutor != null)
            mExecutor.shutdown();

        mExecutor = Executors.newSingleThreadScheduledExecutor();

        computeNextExecutorInterval();

        isNewDeviceFoundDuringLastFiveMinutes = false;

        Map<String, String> record = new HashMap<>();
        record.put("port", "42224");

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
                        // TODO: To improve
                        Date reference = new Date(System.currentTimeMillis() - MAX_SERVICE_DISCOVERY_INTERVAL);
                        Device d = mDeviceManager.getDevice(device);
                        // Device seen more than five minutes
                        if (Long.parseLong(d.getTimestamp()) <= reference.getTime()) {
                            isNewDeviceFoundDuringLastFiveMinutes = true;
                        }
                        mDeviceManager.updateDevice(device);
                    } else {
                        isNewDeviceFoundDuringLastFiveMinutes = true;
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

        mWifiP2pManager.discoverServices(mWifiP2pChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.i(WiFiDirect.TAG, "Discovery started");
                Date date = new Date(System.currentTimeMillis() + mInterval);
                Log.d(WiFiDirect.TAG, "Next discovery: " + date);

                mExecutor.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        clearLocalServicesAndServiceRequests();
                        mState = State.STOP;
                        start();
                    }
                }, mInterval, mInterval, TimeUnit.MILLISECONDS);
            }

            @Override
            public void onFailure(int reason) {
                Log.e(WiFiDirect.TAG, "Discovery failed: " +
                        WiFiDirect.getActionListenerFailureName(reason));
            }
        });
    }

    private void computeNextExecutorInterval() {
        if (isNewDeviceFoundDuringLastFiveMinutes) {
            mInterval = INITIAL_SERVICE_DISCOVERY_INTERVAL;
            Log.d(WiFiDirect.TAG, "Interval: " + mInterval);
            return;
        }

        if (mInterval * 2 >= 60000 * 60) {
            // Max boundary up to 1 hours
            mInterval = 60000 * 60;
            Log.d(WiFiDirect.TAG, "Interval: " + mInterval);
        } else {
            mInterval *= 2;
            Log.d(WiFiDirect.TAG, "Interval: " + mInterval);
        }
    }

    private void clearLocalServicesAndServiceRequests() {
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

    public void stop() {
        clearLocalServicesAndServiceRequests();
        mState = State.STOP;
        mExecutor.shutdown();
        mExecutor = null;
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
