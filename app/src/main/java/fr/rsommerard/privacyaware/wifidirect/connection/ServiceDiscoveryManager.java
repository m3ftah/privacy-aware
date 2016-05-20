package fr.rsommerard.privacyaware.wifidirect.connection;

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
import java.util.concurrent.atomic.AtomicInteger;

import fr.rsommerard.privacyaware.WiFiDirect;
import fr.rsommerard.privacyaware.dao.Device;
import fr.rsommerard.privacyaware.wifidirect.device.DeviceManager;

public class ServiceDiscoveryManager {

    private static final String SERVICE_NAME = "_rsp2p";
    private static final String SERVICE_TYPE = "_presence._tcp";

    private static final int MAX_ERROR = 5;

    private static final int SERVICE_DISCOVERY_INTERVAL = 7000;//60000;

    private static ServiceDiscoveryManager sInstance;

    private final DeviceManager mDeviceManager;
    private final Context mContext;

    private AtomicInteger mErrorCounter;

    private enum State {
        START,
        STOP
    }

    private State mState;

    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel mWifiP2pChannel;

    public static ServiceDiscoveryManager getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new ServiceDiscoveryManager(context);
        }

        return sInstance;
    }

    private ServiceDiscoveryManager(final Context context) {
        mState = State.STOP;
        mErrorCounter = new AtomicInteger(0);

        mContext = context;

        initManagerAndChannel();
        clearLocalServicesAndServiceRequests();

        mDeviceManager = DeviceManager.getInstance(context);

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (mErrorCounter.get() >= MAX_ERROR) {
                    reset();
                } else {
                    stop();
                }
            }
        }, SERVICE_DISCOVERY_INTERVAL, SERVICE_DISCOVERY_INTERVAL, TimeUnit.MILLISECONDS);

        start();
    }

    private void reset() {
        mState = State.STOP;
        mErrorCounter.set(0);
        initManagerAndChannel();
        clearLocalServicesAndServiceRequests();
    }

    private void initManagerAndChannel() {
        mWifiP2pManager = (WifiP2pManager) mContext.getSystemService(Context.WIFI_P2P_SERVICE);

        mWifiP2pChannel = mWifiP2pManager.initialize(mContext, mContext.getMainLooper(), new WifiP2pManager.ChannelListener() {
            @Override
            public void onChannelDisconnected() {
                Log.i(WiFiDirect.TAG, "Channel disconnected");
            }
        });
    }

    private void start() {
        if (mState == State.START)  {
            Log.i(WiFiDirect.TAG, "Discovery already started");
            return;
        }

        Map<String, String> record = new HashMap<>();
        record.put("port", "42224");

        WifiP2pDnsSdServiceInfo wifiP2pDnsSdServiceInfo = WifiP2pDnsSdServiceInfo.newInstance(SERVICE_NAME, SERVICE_TYPE, record);

        mWifiP2pManager.addLocalService(mWifiP2pChannel, wifiP2pDnsSdServiceInfo, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.i(WiFiDirect.TAG, "Local service added");
            }

            @Override
            public void onFailure(int reason) {
                Log.e(WiFiDirect.TAG, "Add local service failed: " +
                        WiFiDirect.getActionListenerFailureName(reason));

                int nbErrors = mErrorCounter.incrementAndGet();
                Log.e(WiFiDirect.TAG, "Error counter: " + nbErrors);
            }
        });

        mWifiP2pManager.setDnsSdResponseListeners(mWifiP2pChannel, new WifiP2pManager.DnsSdServiceResponseListener() {
            @Override
            public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice srcDevice) {
                Log.i(WiFiDirect.TAG, "DnsSdService available");
            }
        }, new WifiP2pManager.DnsSdTxtRecordListener() {
            @Override
            public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> txtRecordMap, WifiP2pDevice srcDevice) {
                Log.i(WiFiDirect.TAG, "DnsSdTxtRecord available");
                Log.i(WiFiDirect.TAG, srcDevice.toString());

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
                Log.i(WiFiDirect.TAG, "Service request added");
            }

            @Override
            public void onFailure(int reason) {
                Log.e(WiFiDirect.TAG, "Add service request failed: " +
                        WiFiDirect.getActionListenerFailureName(reason));

                int nbErrors = mErrorCounter.incrementAndGet();
                Log.e(WiFiDirect.TAG, "Error counter: " + nbErrors);
            }
        });

        mWifiP2pManager.discoverServices(mWifiP2pChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.i(WiFiDirect.TAG, "Discovery started");
                mState = State.START;
            }

            @Override
            public void onFailure(int reason) {
                Log.e(WiFiDirect.TAG, "Discovery failed: " +
                        WiFiDirect.getActionListenerFailureName(reason));

                int nbErrors = mErrorCounter.incrementAndGet();
                Log.e(WiFiDirect.TAG, "Error counter: " + nbErrors);
            }
        });
    }

    private void clearLocalServicesAndServiceRequests() {
        // https://developer.android.com/reference/android/net/wifi/p2p/WifiP2pManager.html
        mWifiP2pManager.clearLocalServices(mWifiP2pChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.i(WiFiDirect.TAG, "Clear local service succeeded");
            }

            @Override
            public void onFailure(int reason) {
                Log.e(WiFiDirect.TAG, "Clear local service failed: " +
                        WiFiDirect.getActionListenerFailureName(reason));

                int nbErrors = mErrorCounter.incrementAndGet();
                Log.e(WiFiDirect.TAG, "Error counter: " + nbErrors);
            }
        });

        mWifiP2pManager.clearServiceRequests(mWifiP2pChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.i(WiFiDirect.TAG, "Clear service requests succeeded");
            }

            @Override
            public void onFailure(int reason) {
                Log.e(WiFiDirect.TAG, "Clear service requests failed: " +
                        WiFiDirect.getActionListenerFailureName(reason));

                int nbErrors = mErrorCounter.incrementAndGet();
                Log.e(WiFiDirect.TAG, "Error counter: " + nbErrors);
            }
        });
    }

    private void stop() {
        clearLocalServicesAndServiceRequests();
        mState = State.STOP;
        start();
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
