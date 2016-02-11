package fr.rsommerard.privacyaware;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import fr.rsommerard.privacyaware.dao.Data;
import fr.rsommerard.privacyaware.data.DataManager;
import fr.rsommerard.privacyaware.demo.Demo;
import fr.rsommerard.privacyaware.wifidirect.WifiDirectManager;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "PAMA";

    private DataAdapter mDataAdapter;
    private List<Data> mDataToDisplay;
    private List<Data> mDataToDisplayTmp;
    private WifiDirectManager mWifiDirectManager;
    private ScheduledExecutorService mExecutor;
    private DataManager mDataManager;

    private ScheduledExecutorService mExecutorData;
    private Random mRandom;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRandom = new Random();

        mDataManager = DataManager.getInstance(this);
        mWifiDirectManager = WifiDirectManager.getInstance(this);

        populateData();

        mDataToDisplay = new ArrayList<>();
        mDataToDisplayTmp = new ArrayList<>();
        mDataAdapter = new DataAdapter(this,
                android.R.layout.simple_list_item_1, mDataToDisplay);

        ListView dataListView = (ListView) findViewById(R.id.listview_data);
        dataListView.setAdapter(mDataAdapter);

        dataListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Data data = (Data) parent.getItemAtPosition(position);
                showDataDetails(data);
            }
        });

        mDataToDisplay.addAll(mDataManager.getAllData());
        mDataToDisplayTmp.addAll(mDataToDisplay);
        mDataAdapter.notifyDataSetChanged();

        mExecutor = Executors.newSingleThreadScheduledExecutor();
        mExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                //Log.i(TAG, "run()");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        printData();
                    }
                });
            }
        }, 0, 5000, TimeUnit.MILLISECONDS);

        mExecutorData = Executors.newSingleThreadScheduledExecutor();
        mExecutorData.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (mRandom.nextBoolean() || mDataManager.getAllData().isEmpty()) {
                    generateData();
                } else {
                    deleteData();
                }
            }
        }, 0, 17000, TimeUnit.MILLISECONDS);
    }

    private void printDataRemoved(List<Data> dataList) {
        Data dataRemoved = null;

        for (Data data : mDataToDisplayTmp) {
            if (dataList.contains(data)) {
                continue;
            }

            dataRemoved = data;
            break;
        }

        if (dataRemoved == null) {
            return;
        }

        int index = mDataToDisplay.indexOf(dataRemoved);
        mDataAdapter.setRemovedIndex(index);
        mDataAdapter.setAddedIndex(-1);
        mDataToDisplayTmp.remove(dataRemoved);
    }

    public void printDataAdded(List<Data> dataList) {
        Data dataAdded = null;

        for (Data data : dataList) {
            if (mDataToDisplay.contains(data)) {
                continue;
            }

            dataAdded = data;
            break;
        }

        if (dataAdded == null) {
            return;
        }

        mDataToDisplay.clear();
        mDataToDisplay.addAll(dataList);
        int index = mDataToDisplay.indexOf(dataAdded);
        mDataAdapter.setAddedIndex(index);
        mDataAdapter.setRemovedIndex(-1);
        mDataToDisplayTmp.clear();
        mDataToDisplayTmp.addAll(dataList);
    }

    private void printData() {
        List<Data> dataList = mDataManager.getAllData();

        Log.d(TAG, dataList.toString());
        Log.d(TAG, mDataToDisplay.toString());
        Log.d(TAG, mDataToDisplayTmp.toString());

        mDataToDisplay.clear();
        mDataToDisplay.addAll(mDataToDisplayTmp);

        if (dataList.size() < mDataToDisplay.size()) {
            printDataRemoved(dataList);
        } else if (dataList.size() > mDataToDisplay.size()) {
            printDataAdded(dataList);
        } else {
            mDataAdapter.setAddedIndex(-1);
            mDataAdapter.setRemovedIndex(-1);
            printDataRemoved(dataList);
            printDataAdded(dataList);
        }

        mDataAdapter.notifyDataSetChanged();
    }

    private void populateData() {
        //Log.i(TAG, "populateData()");

        int dataColor = Demo.getRandomColor();

        for (int i = 0; i < 3; i++) {
            Data data = new Data();
            data.setContent(Demo.getRandomContent());
            data.setColor(dataColor);
            mDataManager.addData(data);
        }
    }

    private void showDataDetails(final Data data) {
        //Log.i(TAG, "showDataDetails()");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        String dataDetails = "Content: " + data.getContent();

        builder.setMessage(dataDetails);
        builder.setTitle(R.string.data_details);

        builder.setPositiveButton(R.string.ok, null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void generateData() {
        Data data = new Data(null, Demo.getRandomContent(), Demo.getRandomColor());
        mDataManager.addData(data);
    }

    private void deleteData() {
        List<Data> datas = mDataManager.getAllData();
        Data data = datas.get(mRandom.nextInt(datas.size()));
        mDataManager.removeData(data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mWifiDirectManager.destroy();

        if (mExecutor != null) {
            mExecutor.shutdown();
        }

        if (mExecutorData != null) {
            mExecutorData.shutdown();
        }
    }
}
