package fr.rsommerard.privacyaware.data;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DataManager {

    private final String TAG = "PADM";

    private static DataManager sInstance;

    private List<Data> mDatas;
    private Random mRand;

    public static DataManager getInstance() {
        if (sInstance == null) {
            sInstance = new DataManager();
        }

        return sInstance;
    }

    private DataManager() {
        Log.i(TAG, "DataManager()");

        mDatas = new ArrayList<>();
        mRand = new Random();

        populateDatas();
    }

    private void populateDatas() {
        Log.i(TAG, "populateDatas()");

        Random rand = new Random();

        int nbData = rand.nextInt(5);
        for (int i = 0; i < nbData; i++) {
            mDatas.add(new Data(String.valueOf(rand.nextInt(1000))));
        }

        Log.d(TAG, mDatas.toString());
    }

    public Data getData() {
        Log.i(TAG, "getData()");

        if (mDatas.isEmpty()) {
            return null;
        }

        return mDatas.get(mRand.nextInt(mDatas.size()));
    }

    public void removeData(Data data) {
        Log.i(TAG, "removeData()");

        mDatas.remove(data);

        Log.d(TAG, mDatas.toString());
    }

    public List<Data> getDatas() {
        Log.i(TAG, "getDatas()");

        return mDatas;
    }

    public void addData(Data data) {
        Log.i(TAG, "addData()");

        if (mDatas.contains(data)) {
            return;
        }

        mDatas.add(data);

        Log.d(TAG, mDatas.toString());
    }

    public boolean hasDatas() {
        return !mDatas.isEmpty();
    }
}
