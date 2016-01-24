package fr.rsommerard.privacyaware;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import fr.rsommerard.privacyaware.data.Data;
import fr.rsommerard.privacyaware.data.DataManager;
import fr.rsommerard.privacyaware.wifidirect.WifiDirectManager;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "PAMA";

    private DataAdapter mDataAdapter;
    private List<Data> mDataToDisplay;
    private List<Data> mDataToDisplayTmp;
    private WifiDirectManager mWifiDirectManager;
    private ScheduledExecutorService mExecutor;
    private DataManager mDataManager;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDataManager = DataManager.getInstance();
        mWifiDirectManager = WifiDirectManager.getInstance(this);

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
                Log.i(TAG, "run()");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        List<Data> datas = mDataManager.getAllData();

                        Log.d(TAG, datas.toString());
                        Log.d(TAG, mDataToDisplay.toString());
                        Log.d(TAG, mDataToDisplayTmp.toString());

                        mDataToDisplay.clear();
                        mDataToDisplay.addAll(mDataToDisplayTmp);

                        if (datas.size() < mDataToDisplay.size()) {
                            Data dataRemoved = null;

                            for (Data data : mDataToDisplayTmp) {
                                if (datas.contains(data)) {
                                    continue;
                                }

                                dataRemoved = data;
                                break;
                            }

                            int index = mDataToDisplay.indexOf(dataRemoved);
                            mDataAdapter.setRemovedIndex(index);
                            mDataAdapter.setAddedIndex(-1);
                            mDataToDisplayTmp.remove(dataRemoved);
                        } else if (datas.size() > mDataToDisplay.size()) {
                            Data dataAdded = null;

                            for (Data data : datas) {
                                if (mDataToDisplay.contains(data)) {
                                    continue;
                                }

                                dataAdded = data;
                                break;
                            }

                            mDataToDisplay.clear();
                            mDataToDisplay.addAll(datas);
                            int index = mDataToDisplay.indexOf(dataAdded);
                            mDataAdapter.setAddedIndex(index);
                            mDataAdapter.setRemovedIndex(-1);
                            mDataToDisplayTmp.clear();
                            mDataToDisplayTmp.addAll(datas);
                        } else {
                            mDataAdapter.setAddedIndex(-1);
                            mDataAdapter.setRemovedIndex(-1);
                        }

                        mDataAdapter.notifyDataSetChanged();
                    }
                });
            }
        }, 0, 5000, TimeUnit.MILLISECONDS);
    }

    private void showDataDetails(final Data data) {
        Log.i(TAG, "showDataDetails()");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        String dataDetails = "Content: " + data.getContent();

        builder.setMessage(dataDetails);
        builder.setTitle(R.string.data_details);

        builder.setPositiveButton(R.string.ok, null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mWifiDirectManager.destroy();

        if (mExecutor != null) {
            mExecutor.shutdown();
        }
    }
}
