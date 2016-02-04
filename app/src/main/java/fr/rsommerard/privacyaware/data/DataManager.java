package fr.rsommerard.privacyaware.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import fr.rsommerard.privacyaware.dao.DaoMaster;
import fr.rsommerard.privacyaware.dao.DaoMaster.DevOpenHelper;
import fr.rsommerard.privacyaware.dao.DaoSession;
import fr.rsommerard.privacyaware.dao.Data;
import fr.rsommerard.privacyaware.dao.DataDao;

public class DataManager {

    private static final String TAG = "PADM";

    private static DataManager sInstance;

    private final List<Data> mDatas;
    private final Random mRand;
    private final SQLiteDatabase mDb;
    private final DaoMaster mDaoMaster;
    private final DaoSession mDaoSession;
    private final DataDao mDataDao;

    public static DataManager getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new DataManager(context);
        }

        return sInstance;
    }

    private DataManager(final Context context) {
        //Log.i(TAG, "DataManager()");
        DevOpenHelper helper = new DevOpenHelper(context, "privacy-aware-db", null);
        mDb = helper.getWritableDatabase();
        mDaoMaster = new DaoMaster(mDb);
        mDaoSession = mDaoMaster.newSession();
        mDataDao = mDaoSession.getDataDao();

        mDatas = new ArrayList<>();
        mRand = new Random();
    }

    public Data getData() {
        //Log.i(TAG, "getData()");

        if (mDatas.isEmpty()) {
            return null;
        }

        return mDatas.get(mRand.nextInt(mDatas.size()));
    }

    public void removeData(final Data data) {
        //Log.i(TAG, "removeData(Data data)");

        mDataDao.delete(data);
        mDatas.remove(data);

        Log.d(TAG, mDatas.toString());
    }

    public List<Data> getAllData() {
        //Log.i(TAG, "getAllData()");

        return mDatas;
    }

    public void addData(final Data data) {
        //Log.i(TAG, "addData(Data data)");

        mDataDao.insert(data);
        mDatas.add(data);

        Log.d(TAG, "Datas: " + mDatas.toString());
    }

    public boolean hasData() {
        return !mDatas.isEmpty();
    }
}
