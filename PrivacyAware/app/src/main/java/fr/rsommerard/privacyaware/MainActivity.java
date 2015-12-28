package fr.rsommerard.privacyaware;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Handler;
import android.os.Message;
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

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class MainActivity extends AppCompatActivity implements Handler.Callback {

    public final String TAG = MainActivity.class.getSimpleName();

    private final String SERVICE_NAME = "_rsp2p";
    private final String SERVICE_TYPE = "_presence._tcp";

    private boolean mWifiDirectEnable;

    private boolean mConnectToDeviceEnable;

    private Handler mHandler;

    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel mWifiP2pChannel;

    private IntentFilter mWifiIntentFilter;
    private WifiDirectBroadcastReceiver mWifiDirectBroadcastReceiver;

    private WifiP2pDnsSdServiceInfo mWifiP2pDnsSdServiceInfo;
    private WifiP2pDnsSdServiceRequest mWifiP2pDnsSdServiceRequest;

    private ArrayAdapter<Peer> mPeersAdapter;
    private ArrayList<Peer> mPeers;

    private Peer mPeerSelected;

    private Timer mTimer;
    private Button mProcessButton;
    private Button mConnectButton;

    private ServerSocket mServerSocket;

    private PassiveThread mPassiveThread;
    private ActiveThread mActiveThread;

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

    private void setConnectToPeerEnabledOnClick() {
        mConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mConnectToDeviceEnable = true;
                setConnectToPeerDisabledOnClick();
            }
        });

        mConnectButton.setText(R.string.connect_disabled);
    }

    private void setConnectToPeerDisabledOnClick() {
        mConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mConnectToDeviceEnable = false;
                setConnectToPeerEnabledOnClick();
            }
        });

        mConnectButton.setText(R.string.connect_enabled);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProcessButton = (Button) findViewById(R.id.button_process);
        setStartProcessOnClick();

        mConnectButton = (Button) findViewById(R.id.button_connect);
        setConnectToPeerEnabledOnClick();

        mPeers = new ArrayList<>();

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

        disconnectFromPeer();

        mWifiIntentFilter = new IntentFilter();
        mWifiIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mWifiIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

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

            mWifiP2pManager.cancelConnect(mWifiP2pChannel, null);
            mWifiP2pManager.removeGroup(mWifiP2pChannel, null);
        }

        if (mPassiveThread != null && mPassiveThread.isAlive()) {
            mPassiveThread.interrupt();
            mPassiveThread = null;
        }

        if (mActiveThread != null && mActiveThread.isAlive()) {
            mActiveThread.interrupt();
            mActiveThread = null;
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

            mWifiP2pManager.cancelConnect(mWifiP2pChannel, null);
            mWifiP2pManager.removeGroup(mWifiP2pChannel, null);
        }

        if (mPassiveThread != null && mPassiveThread.isAlive()) {
            mPassiveThread.interrupt();
            mPassiveThread = null;
        }

        if (mActiveThread != null && mActiveThread.isAlive()) {
            mActiveThread.interrupt();
            mActiveThread = null;
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

        mHandler = new Handler(this);

        try {
            mServerSocket = new ServerSocket(0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(TAG, String.valueOf(mServerSocket.getLocalPort()));

        Map<String, String> record = new HashMap<>();
        record.put("port", String.valueOf(mServerSocket.getLocalPort()));

        mPassiveThread = new PassiveThread(mServerSocket);
        mPassiveThread.start();

        mWifiP2pDnsSdServiceInfo = WifiP2pDnsSdServiceInfo.newInstance(SERVICE_NAME, SERVICE_TYPE, record);

        mWifiP2pManager.addLocalService(mWifiP2pChannel, mWifiP2pDnsSdServiceInfo, null);

        mWifiP2pManager.setDnsSdResponseListeners(mWifiP2pChannel, null, new SetDnsSdTxtRecordListener());

        mWifiP2pDnsSdServiceRequest = WifiP2pDnsSdServiceRequest.newInstance();

        mWifiP2pManager.addServiceRequest(mWifiP2pChannel, mWifiP2pDnsSdServiceRequest, null);

        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new DiscoverServicesTimerTask(), 0, 11000);
        mTimer.scheduleAtFixedRate(new NotifyPeersAdapterTimerTask(), 3000, 3000);
        mTimer.scheduleAtFixedRate(new ClearPeersTimerTask(), 61000, 61000);
        mTimer.scheduleAtFixedRate(new ConnectToRandomPeerTimerTask(), 37000, 37000);

        setStopProcessOnClick();

        Toast.makeText(this, "Process started", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean handleMessage(Message message) {
        if (message.obj.toString().equals("FINISH")) {
            disconnectFromPeer();
        }

        return true;
    }

    private class DiscoverServicesTimerTask extends TimerTask {

        @Override
        public void run() {
            Log.d(TAG, "DiscoverServicesTimerTask");
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

                mWifiDirectEnable = (wifiState == WifiP2pManager.WIFI_P2P_STATE_ENABLED);
            }

            if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                WifiP2pInfo wifiP2pInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
                Log.d(TAG, wifiP2pInfo.toString());

                NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                Log.d(TAG, networkInfo.toString());

                if (networkInfo.isConnected()) {
                    // we are connected with the other device, request connection
                    // info to find group owner IP
                    Log.d(TAG, "Devices connected");

                    if (wifiP2pInfo.isGroupOwner) {
                        return;
                    }

                    Log.d(TAG, mPeerSelected.toString());
                    Log.d(TAG, wifiP2pInfo.groupOwnerAddress.toString());

                    mActiveThread = new ActiveThread(mPeerSelected, wifiP2pInfo.groupOwnerAddress, mHandler);
                    mActiveThread.start();
                } else {
                    // It's a disconnect
                    Log.d(TAG, "Devices disconnected");
                }
            }
        }
    }

    private class SetDnsSdTxtRecordListener implements WifiP2pManager.DnsSdTxtRecordListener {

        @Override
        public void onDnsSdTxtRecordAvailable(String fullDomainName,
                                              Map<String, String> txtRecordMap,
                                              final WifiP2pDevice peer) {
            Log.d(TAG, "Peer found: " + peer);

            if (txtRecordMap.isEmpty() || !txtRecordMap.containsKey("port")) {
                return;
            }

            if (!fullDomainName.contains(SERVICE_NAME)) {
                return;
            }

            if (peer.deviceName.isEmpty()) {
                return;
            }

            final Peer newPeer = new Peer(peer, Integer.parseInt(txtRecordMap.get("port")));

            if (!mPeers.contains(newPeer)) {
                mPeers.add(newPeer);
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
                    mPeersAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    private class ConnectToRandomPeerTimerTask extends TimerTask {

        @Override
        public void run() {
            Log.d(TAG, "ConnectToRandomPeerTimerTask");

            if (!mConnectToDeviceEnable) {
                Log.i(TAG, "Connect to peer disabled");
                return;
            }

            if (mPeers.isEmpty()) {
                Log.i(TAG, "No peer available");
                return;
            }

            mWifiP2pManager.cancelConnect(mWifiP2pChannel, null);
            mWifiP2pManager.removeGroup(mWifiP2pChannel, null);

            int numberOfPeers = mPeers.size();

            Random rand = new Random();
            int index = rand.nextInt(numberOfPeers);

            mPeerSelected = mPeers.get(index);

            WifiP2pConfig wifiP2pConfig = new WifiP2pConfig();
            wifiP2pConfig.deviceAddress = mPeerSelected.getAddress();
            wifiP2pConfig.wps.setup = WpsInfo.PBC;

            Log.d(TAG, "Peer selected: " + mPeerSelected);

            mWifiP2pManager.connect(mWifiP2pChannel, wifiP2pConfig, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                }

                @Override
                public void onFailure(int reason) {
                    Log.e(TAG, "Connection failed: " + getReasonName(reason));
                }
            });
        }
    }

    private String getReasonName(int reason) {
        switch(reason) {
            case WifiP2pManager.P2P_UNSUPPORTED:
                return "P2P_UNSUPPORTED";
            case WifiP2pManager.BUSY:
                return "BUSY";
            case WifiP2pManager.ERROR:
                return "ERROR";
            default:
                return "UKNOWN_REASON";
        }
    }

    private void disconnectFromPeer() {
        mWifiP2pManager.cancelConnect(mWifiP2pChannel, null);
        mWifiP2pManager.removeGroup(mWifiP2pChannel, null);
    }

    private class ClearPeersTimerTask extends TimerTask {

        @Override
        public void run() {
            Log.d(TAG, "ClearPeersTimerTask");
            mPeers.clear();
        }
    }
}
