package fr.rsommerard.privacyaware;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DataManager {

    private final String TAG = PeerManager.class.getSimpleName();

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
        mDatas = new ArrayList<>();
        mRand = new Random();

        populateDatas();
    }

    private void populateDatas() {
        mDatas.add(new Data("1"));
        mDatas.add(new Data("2"));
        mDatas.add(new Data("3"));
        mDatas.add(new Data("4"));
        mDatas.add(new Data("5"));
    }

    public Data getData() {
        if (mDatas.isEmpty()) {
            return null;
        }

        return mDatas.get(mRand.nextInt(mDatas.size()));
    }

    public List<Data> getDatas() {
        return mDatas;
    }

    public void addData(Data data) {
        if (mDatas.contains(data)) {
            return;
        }

        mDatas.add(data);

        Log.d(TAG, mDatas.toString());
    }
}
