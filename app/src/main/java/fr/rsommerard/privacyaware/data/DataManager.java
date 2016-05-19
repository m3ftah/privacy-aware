package fr.rsommerard.privacyaware.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;
import java.util.Random;

import fr.rsommerard.privacyaware.dao.DaoMaster;
import fr.rsommerard.privacyaware.dao.DaoMaster.DevOpenHelper;
import fr.rsommerard.privacyaware.dao.DaoSession;
import fr.rsommerard.privacyaware.dao.Data;
import fr.rsommerard.privacyaware.dao.DataDao;

public class DataManager {

    private static DataManager sInstance;

    private final Random mRandom;
    private final DataDao mDataDao;

    public static DataManager getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new DataManager(context);
        }

        return sInstance;
    }

    private DataManager(final Context context) {
        DevOpenHelper helper = new DevOpenHelper(context, "privacy-aware-db", null);
        SQLiteDatabase mDb = helper.getWritableDatabase();
        DaoMaster mDaoMaster = new DaoMaster(mDb);
        DaoSession mDaoSession = mDaoMaster.newSession();
        mDataDao = mDaoSession.getDataDao();

        mRandom = new Random();
    }

    public Data getData() {
        List<Data> data = mDataDao.loadAll();
        return data.get(mRandom.nextInt(data.size()));
    }

    public void removeData(final Data data) {
        mDataDao.delete(data);
    }

    public List<Data> getAllData() {
        return mDataDao.loadAll();
    }

    public void addData(final Data data) {
        mDataDao.insert(data);
    }

    public boolean hasData() {
        return mDataDao.count() != 0;
    }
}
