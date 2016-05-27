package fr.rsommerard.privacyaware.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.List;
import java.util.Random;

import fr.rsommerard.privacyaware.WiFiDirect;
import fr.rsommerard.privacyaware.dao.DaoMaster;
import fr.rsommerard.privacyaware.dao.DaoMaster.DevOpenHelper;
import fr.rsommerard.privacyaware.dao.DaoSession;
import fr.rsommerard.privacyaware.dao.Data;
import fr.rsommerard.privacyaware.dao.DataDao;

public class DataManager {

    private final Random mRandom;
    private final DataDao mDataDao;

    public DataManager(final Context context) {
        DevOpenHelper helper = new DevOpenHelper(context, "privacy-aware-db", null);
        SQLiteDatabase mDb = helper.getWritableDatabase();
        DaoMaster mDaoMaster = new DaoMaster(mDb);
        DaoSession mDaoSession = mDaoMaster.newSession();
        mDataDao = mDaoSession.getDataDao();

        mDataDao.deleteAll(); // TODO: Delete this line

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
        Log.i(WiFiDirect.TAG, "Insert " + data.toString());
    }

    public boolean hasData() {
        return mDataDao.count() != 0;
    }
}
