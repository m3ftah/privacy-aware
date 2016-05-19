package fr.rsommerard.privacyaware.wifidirect.device;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import fr.rsommerard.privacyaware.WiFiDirect;

public class DeviceManager {

    private static DeviceManager sInstance;

    private final List<Device> mDevices;
    private final Random mRand;

    private ScheduledExecutorService mExecutor;

    public static DeviceManager getInstance() {
        if (sInstance == null) {
            sInstance = new DeviceManager();
        }

        return sInstance;
    }

    private DeviceManager() {
        //Log.i(WiFiDirect.TAG, "DeviceManager()");

        mDevices = new ArrayList<>();
        mRand = new Random();

        startCleaningExecutor();
    }

    public void startCleaningExecutor() {
        if (mExecutor != null) {
            return;
        }

        mExecutor = Executors.newSingleThreadScheduledExecutor();
        mExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                //Log.i(WiFiDirect.TAG, "run()");

                mDevices.clear();
            }
        }, 0, 181000, TimeUnit.MILLISECONDS);
    }

    public void stopCleaningExecutor() {
        if (mExecutor != null) {
            mExecutor.shutdown();
        }
    }

    public void destroy() {
        Log.i(WiFiDirect.TAG, "destroy()");

        stopCleaningExecutor();
    }

    public Device getPeer() {
        //Log.i(WiFiDirect.TAG, "getPeer()");

        if (mDevices.isEmpty()) {
            return null;
        }

        return mDevices.get(mRand.nextInt(mDevices.size()));
    }

    public Device getPeer(final String address) {
        //Log.i(WiFiDirect.TAG, "getPeer(String name)");

        Log.d(WiFiDirect.TAG, address);
        Log.d(WiFiDirect.TAG, mDevices.toString());

        for (Device device : mDevices) {
            //Log.d(TAG, "\"" + device.getAddress() + "\".equals(\"" + address + "\")");
            if (device.getAddress().equals(address)) {
                return device;
            }
        }

        return null;
    }

    public List<Device> getAllPeers() {
        //Log.i(WiFiDirect.TAG, "getPeers()");

        return mDevices;
    }

    public boolean hasPeers() {
        return !mDevices.isEmpty();
    }

    public void addPeer(final Device device) {
        //Log.i(WiFiDirect.TAG, "addPeer(Device device)");

        if (mDevices.contains(device)) {
            return;
        }

        mDevices.add(device);

        Log.d(WiFiDirect.TAG, "Peers: " + mDevices.toString());
    }
}
