package fr.rsommerard.privacyaware.demo;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import fr.rsommerard.privacyaware.R;
import fr.rsommerard.privacyaware.WiFiDirect;
import fr.rsommerard.privacyaware.dao.Data;
import fr.rsommerard.privacyaware.data.DataManager;
import fr.rsommerard.privacyaware.wifidirect.WiFiDirectManager;
import fr.rsommerard.privacyaware.wifidirect.device.Device;
import fr.rsommerard.privacyaware.wifidirect.device.DeviceManager;

public class DemoActivity extends AppCompatActivity {

    private DemoDataAdapter mDemoDataAdapter;
    private List<Data> mDataToDisplay;
    private List<Data> mDataToDisplayTmp;
    private WiFiDirectManager mWiFiDirectManager;
    private ScheduledExecutorService mExecutor;
    private DataManager mDataManager;
    private DeviceManager mDeviceManager;
    private Button mSendDataButton;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        mDataManager = DataManager.getInstance(this);
        mDeviceManager = DeviceManager.getInstance();
        mWiFiDirectManager = WiFiDirectManager.getInstance(this);

        populateData();

        mDataToDisplay = new ArrayList<>();
        mDataToDisplayTmp = new ArrayList<>();
        mDemoDataAdapter = new DemoDataAdapter(this,
                android.R.layout.simple_list_item_1, mDataToDisplay);

        ListView dataListView = (ListView) findViewById(R.id.listview_data);
        dataListView.setAdapter(mDemoDataAdapter);

        dataListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Data data = (Data) parent.getItemAtPosition(position);
                showDataDetails(data);
            }
        });

        mSendDataButton = (Button) findViewById(R.id.button_send_data);
        mSendDataButton.setEnabled(false);
        mSendDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWiFiDirectManager.process();
            }
        });

        Button showPeersButton = (Button) findViewById(R.id.button_show_peers);
        showPeersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPeers();
            }
        });

        mDataToDisplay.addAll(mDataManager.getAllData());
        mDataToDisplayTmp.addAll(mDataToDisplay);
        mDemoDataAdapter.notifyDataSetChanged();

        mExecutor = Executors.newSingleThreadScheduledExecutor();
        mExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Log.i(WiFiDirect.TAG, "scheduleAtFixedRate::run()");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mDeviceManager.hasPeers()) {
                            enableSendButton();
                        } else {
                            disableSendButton();
                        }

                        printData();
                    }
                });
            }
        }, 0, 7000, TimeUnit.MILLISECONDS);
    }

    private void disableSendButton() {
        mSendDataButton.setEnabled(false);
    }

    private void enableSendButton() {
        mSendDataButton.setEnabled(true);
    }

    private void printDataRemoved(List<Data> dataList) {
        Data dataRemoved = null;

        for (Data data : mDataToDisplay) {
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
        mDemoDataAdapter.setRemovedIndex(index);
        mDemoDataAdapter.setAddedIndex(-1);
        mDataToDisplayTmp.clear();
        mDataToDisplayTmp.addAll(dataList);
    }

    private void printDataAdded(List<Data> dataList) {
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
        mDemoDataAdapter.setAddedIndex(index);
        mDemoDataAdapter.setRemovedIndex(-1);
        mDataToDisplayTmp.clear();
        mDataToDisplayTmp.addAll(dataList);
    }

    private void printData() {
        Log.d(WiFiDirect.TAG, "printData()");
        List<Data> dataList = mDataManager.getAllData();

        mDataToDisplay.clear();
        mDataToDisplay.addAll(mDataToDisplayTmp);

        Log.d(WiFiDirect.TAG, "dataList: " + dataList);
        Log.d(WiFiDirect.TAG, "mDataToDisplay: " + mDataToDisplay);

        if (dataList.size() < mDataToDisplay.size()) {
            printDataRemoved(dataList);
        } else if (dataList.size() > mDataToDisplay.size()) {
            printDataAdded(dataList);
        } else {
            mDemoDataAdapter.setAddedIndex(-1);
            mDemoDataAdapter.setRemovedIndex(-1);
        }

        mDemoDataAdapter.notifyDataSetChanged();
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

    private void showPeers() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        List<Device> devices = mDeviceManager.getAllPeers();

        String peersToDisplay = "";

        for (Device device : devices) {
            peersToDisplay += "\n" + device.toString();
        }

        if (devices.isEmpty()) {
            peersToDisplay += "None";
        }

        builder.setMessage(peersToDisplay);
        builder.setTitle(R.string.peers_available);

        builder.setPositiveButton(R.string.ok, null);

        AlertDialog dialog = builder.create();
        dialog.show();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mWiFiDirectManager.destroy();

        if (mExecutor != null) {
            mExecutor.shutdown();
        }
    }
}
