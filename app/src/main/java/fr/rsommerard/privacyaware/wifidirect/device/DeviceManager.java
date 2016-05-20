package fr.rsommerard.privacyaware.wifidirect.device;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import de.greenrobot.dao.query.Query;
import de.greenrobot.dao.query.QueryBuilder;
import fr.rsommerard.privacyaware.WiFiDirect;
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

        mDeviceDao.deleteAll(); // TODO: Delete this line

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

    public boolean containDevice(Device device) {
        QueryBuilder<Device> qBuilder = mDeviceDao.queryBuilder();
        qBuilder.where(DeviceDao.Properties.Address.eq(device.getAddress()));

        Query<Device> query = qBuilder.build();

        return query.unique() != null;
    }

    public void updateDevice(Device device) {
        QueryBuilder<Device> qBuilder = mDeviceDao.queryBuilder();
        qBuilder.where(DeviceDao.Properties.Address.eq(device.getAddress()));

        Query<Device> query = qBuilder.build();

        Device d = query.unique();
        device.setId(d.getId());

        mDeviceDao.update(device);
        Log.i(WiFiDirect.TAG, "Update " + d.toString() + " to " + device.toString());
    }

    public void addDevice(final Device device) {
        mDeviceDao.insert(device);
        Log.i(WiFiDirect.TAG, "Insert " + device.toString());
    }
}
