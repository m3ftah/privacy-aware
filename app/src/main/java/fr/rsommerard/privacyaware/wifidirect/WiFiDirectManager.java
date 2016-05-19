package fr.rsommerard.privacyaware.wifidirect;

import android.content.Context;
import android.util.Log;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import fr.rsommerard.privacyaware.WiFiDirect;
import fr.rsommerard.privacyaware.dao.Device;
import fr.rsommerard.privacyaware.data.DataManager;
import fr.rsommerard.privacyaware.wifidirect.connection.ConnectionManager;
import fr.rsommerard.privacyaware.wifidirect.connection.ServiceDiscoveryManager;
import fr.rsommerard.privacyaware.wifidirect.device.DeviceManager;

public class WiFiDirectManager {

    private static WiFiDirectManager sInstance;

    private final Context mContext;
    private final DataManager mDataManager;

    private ConnectionManager mConnectionManager;
    private ServiceDiscoveryManager mServiceDiscoveryManager;
    private DeviceManager mDeviceManager;

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

        //mConnectionManager = ConnectionManager.getInstance(mContext);
        mServiceDiscoveryManager = ServiceDiscoveryManager.getInstance(mContext);

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
}
