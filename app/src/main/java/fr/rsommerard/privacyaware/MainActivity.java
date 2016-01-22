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

    private ArrayAdapter<Data> mDataAdapter;
    private List<Data> mDataToDisplay;
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
        mDataAdapter = new ArrayAdapter<>(this,
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

        mDataAdapter.notifyDataSetChanged();

        mExecutor = Executors.newSingleThreadScheduledExecutor();
        mExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                //Log.i(TAG, "run()");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mDataToDisplay.clear();
                        mDataToDisplay.addAll(mDataManager.getAllData());
                        mDataAdapter.notifyDataSetChanged();
                    }
                });
            }
        }, 3000, 3000, TimeUnit.MILLISECONDS);
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
