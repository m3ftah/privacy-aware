package fr.rsommerard.privacyaware;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    public final String TAG = MainActivity.class.getSimpleName();

    private final String SERVICE_NAME = "_rsp2p";
    private final String SERVICE_TYPE = "_presence._tcp";

    private boolean mWifiDirectEnable;

    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel mWifiP2pChannel;

    private IntentFilter mWifiIntentFilter;
    private WifiDirectBroadcastReceiver mWifiDirectBroadcastReceiver;

    private WifiP2pDnsSdServiceInfo mWifiP2pDnsSdServiceInfo;
    private WifiP2pDnsSdServiceRequest mWifiP2pDnsSdServiceRequest;

    private ArrayAdapter<Peer> mPeersAdapter;
    private ArrayList<Peer> mPeers;
    private ArrayList<Peer> mTempPeers;

    private Timer mTimer;
    private Button mProcessButton;

    private void setStartProcessOnClick() {
        mProcessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startProcess();
            }
        });

        mProcessButton.setText(R.string.start_process);
    }

    private void setStopProcessOnClick() {
        mProcessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopProcess();
            }
        });

        mProcessButton.setText(R.string.stop_process);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProcessButton = (Button) findViewById(R.id.button_process);
        setStartProcessOnClick();

        mPeers = new ArrayList<>();
        mTempPeers = new ArrayList<>();

        mPeersAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, mPeers);

        ListView peersListView = (ListView) findViewById(R.id.listview_peers);
        peersListView.setAdapter(mPeersAdapter);

        peersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Peer peer = (Peer) parent.getItemAtPosition(position);
                showPeerDetails(peer);
            }
        });

        mWifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mWifiP2pChannel = mWifiP2pManager.initialize(this, getMainLooper(), null);

        mWifiIntentFilter = new IntentFilter();
        mWifiIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        mWifiIntentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);

        mWifiDirectBroadcastReceiver = new WifiDirectBroadcastReceiver();
        registerReceiver(mWifiDirectBroadcastReceiver, mWifiIntentFilter);
    }

    private void showPeerDetails(Peer peer) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        String peerDetails = "Name: " + peer.getName();
        peerDetails += "\n" + "Address: " + peer.getAddress();
        peerDetails += "\n" + "Port: " + peer.getPort();

        builder.setMessage(peerDetails);
        builder.setTitle(R.string.peer_details);

        builder.setPositiveButton(R.string.ok, null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mWifiDirectBroadcastReceiver);

        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
        }

        if (mWifiP2pManager != null) {
            mWifiP2pManager.clearLocalServices(mWifiP2pChannel, null);
            mWifiP2pManager.removeLocalService(mWifiP2pChannel, mWifiP2pDnsSdServiceInfo, null);
            mWifiP2pManager.clearServiceRequests(mWifiP2pChannel, null);
            mWifiP2pManager.removeServiceRequest(mWifiP2pChannel, mWifiP2pDnsSdServiceRequest, null);
        }
    }

    private void stopProcess() {
        unregisterReceiver(mWifiDirectBroadcastReceiver);

        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
        }

        mPeers.clear();
        mPeersAdapter.notifyDataSetChanged();

        if (mWifiP2pManager != null) {
            mWifiP2pManager.clearLocalServices(mWifiP2pChannel, null);
            mWifiP2pManager.removeLocalService(mWifiP2pChannel, mWifiP2pDnsSdServiceInfo, null);
            mWifiP2pManager.clearServiceRequests(mWifiP2pChannel, null);
            mWifiP2pManager.removeServiceRequest(mWifiP2pChannel, mWifiP2pDnsSdServiceRequest, null);
            mWifiP2pManager.stopPeerDiscovery(mWifiP2pChannel, null);
        }

        setStartProcessOnClick();

        Toast.makeText(this, "Process stopped", Toast.LENGTH_SHORT).show();
    }

    private void startProcess() {
        if (!mWifiDirectEnable) {
            Toast.makeText(this, "Can't process", Toast.LENGTH_SHORT).show();
            return;
        }

        mPeers.clear();
        mPeersAdapter.notifyDataSetChanged();

        registerReceiver(mWifiDirectBroadcastReceiver, mWifiIntentFilter);

        Map<String, String> record = new HashMap<>();
        record.put("port", "42");

        mWifiP2pDnsSdServiceInfo = WifiP2pDnsSdServiceInfo.newInstance(SERVICE_NAME, SERVICE_TYPE, record);

        mWifiP2pManager.addLocalService(mWifiP2pChannel, mWifiP2pDnsSdServiceInfo, null);

        mWifiP2pManager.setDnsSdResponseListeners(mWifiP2pChannel, null, new SetDnsSdTxtRecordListener());

        mWifiP2pDnsSdServiceRequest = WifiP2pDnsSdServiceRequest.newInstance();

        mWifiP2pManager.addServiceRequest(mWifiP2pChannel, mWifiP2pDnsSdServiceRequest, null);

        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new DiscoverServicesTimerTask(), 0, 5000);
        mTimer.scheduleAtFixedRate(new NotifyPeersAdapterTimerTask(), 0, 7000);

        setStopProcessOnClick();

        Toast.makeText(this, "Process started", Toast.LENGTH_SHORT).show();
    }

    private class DiscoverServicesTimerTask extends TimerTask {

        @Override
        public void run() {
            Log.d(TAG, "DiscoverServicesTimerTask");
            mTempPeers.clear();
            mWifiP2pManager.discoverServices(mWifiP2pChannel, null);
        }
    }

    private class WifiDirectBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, action);

            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                int wifiState = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE,
                        WifiP2pManager.WIFI_P2P_STATE_DISABLED);

                if (wifiState == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    mWifiDirectEnable = true;
                } else {
                    mWifiDirectEnable = false;
                }
            }
        }
    }

    private class SetDnsSdTxtRecordListener implements WifiP2pManager.DnsSdTxtRecordListener {

        @Override
        public void onDnsSdTxtRecordAvailable(String fullDomainName,
                                              Map<String, String> txtRecordMap,
                                              final WifiP2pDevice peer) {
            Log.d(TAG, "Peer found: " + peer.deviceName);

            if (txtRecordMap.isEmpty() || !txtRecordMap.containsKey("port")) {
                return;
            }

            if (!fullDomainName.contains(SERVICE_NAME)) {
                return;
            }

            final Peer newPeer = new Peer(peer, txtRecordMap.get("port"));

            if (!mTempPeers.contains(newPeer)) {
                mTempPeers.add(newPeer);
            }
        }
    }

    private class NotifyPeersAdapterTimerTask extends TimerTask {

        @Override
        public void run() {
            Log.d(TAG, "NotifyPeersAdapterTimerTask");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mPeers.clear();
                    mPeers.addAll(mTempPeers);
                    mPeersAdapter.notifyDataSetChanged();
                }
            });
        }
    }
}
