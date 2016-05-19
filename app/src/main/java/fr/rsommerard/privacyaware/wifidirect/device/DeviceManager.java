package fr.rsommerard.privacyaware.wifidirect.device;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import fr.rsommerard.privacyaware.dao.DaoMaster;
import fr.rsommerard.privacyaware.dao.DaoMaster.DevOpenHelper;
import fr.rsommerard.privacyaware.dao.DaoSession;

import java.util.List;
import java.util.Random;

import fr.rsommerard.privacyaware.dao.Device;
import fr.rsommerard.privacyaware.dao.DeviceDao;

public class DeviceManager {

    private static DeviceManager sInstance;

    private final Random mRandom;
    private final DeviceDao mDeviceDao;

    public static DeviceManager getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new DeviceManager(context);
        }

        return sInstance;
    }

    private DeviceManager(final Context context) {
        DevOpenHelper helper = new DevOpenHelper(context, "privacy-aware-db", null);
        SQLiteDatabase mDb = helper.getWritableDatabase();
        DaoMaster mDaoMaster = new DaoMaster(mDb);
        DaoSession mDaoSession = mDaoMaster.newSession();
        mDeviceDao = mDaoSession.getDeviceDao();

        mRandom = new Random();
    }

    public Device getDevice() {
        List<Device> devices = mDeviceDao.loadAll();
        return devices.get(mRandom.nextInt(devices.size()));
    }

    public List<Device> getAllDevices() {
        return mDeviceDao.loadAll();
    }

    public boolean hasDevices() {
        return mDeviceDao.count() != 0;
    }

    public void addDevice(final Device device) {
        mDeviceDao.insert(device);
    }
}
