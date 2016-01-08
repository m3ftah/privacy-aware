package fr.rsommerard.privacyaware;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import fr.rsommerard.privacyaware.connection.ConnectionManager;
import fr.rsommerard.privacyaware.connection.ServiceDiscoveryManager;
import fr.rsommerard.privacyaware.connection.WifiDirectManager;
import fr.rsommerard.privacyaware.peer.Peer;
import fr.rsommerard.privacyaware.peer.PeerManager;

public class MainActivity extends AppCompatActivity {

    public final String TAG = "PAMA";

    private final int DELAY = 3000;

    private ArrayAdapter<Peer> mPeersAdapter;
    private Button mProcessButton;
    private Button mConnectButton;
    private List<Peer> mPeersToDisplay;
    private PeerManager mPeerManager;
    private ConnectionManager mConnectionManager;
    private ServiceDiscoveryManager mServiceDiscoveryManager;
    private ScheduledExecutorService mExecutor;
    private WifiDirectManager mWifiDirectManager;

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

        mConnectButton = (Button) findViewById(R.id.button_connect);
        mConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectToPeer();
            }
        });

        mPeerManager = PeerManager.getInstance();
        mConnectionManager = ConnectionManager.getInstance(this);
        mServiceDiscoveryManager = ServiceDiscoveryManager.getInstance(this);
        mWifiDirectManager = WifiDirectManager.getInstance(this);

        mPeersToDisplay = new ArrayList<>();
        mPeersAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, mPeersToDisplay);

        ListView peersListView = (ListView) findViewById(R.id.listview_peers);
        peersListView.setAdapter(mPeersAdapter);

        peersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Peer peer = (Peer) parent.getItemAtPosition(position);
                showPeerDetails(peer);
            }
        });
    }

    private void connectToPeer() {
        Log.i(TAG, "connectToPeer()");

        if (!mPeerManager.hasPeers()) {
            Log.d(TAG, "No peers available");
            return;
        }

        Peer peer = mPeerManager.getPeer();

        mConnectionManager.connect(peer);
    }

    private void showPeerDetails(Peer peer) {
        Log.i(TAG, "showPeerDetails()");

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

        mServiceDiscoveryManager.stopDiscovery();

        mConnectionManager.disconnect();
        mConnectionManager.destroy();

        mPeerManager.destroy();

        mExecutor.shutdown();
    }

    private void stopProcess() {
        Log.i(TAG, "stopProcess()");

        mPeersToDisplay.clear();
        mPeersAdapter.notifyDataSetChanged();

        mServiceDiscoveryManager.stopDiscovery();

        mConnectionManager.disconnect();

        mExecutor.shutdown();

        setStartProcessOnClick();
    }

    private void startProcess() {
        Log.i(TAG, "startProcess()");

        mPeersAdapter.notifyDataSetChanged();

        mExecutor = Executors.newSingleThreadScheduledExecutor();
        mExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                //Log.i(TAG, "run()");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPeersToDisplay.clear();
                        mPeersToDisplay.addAll(mPeerManager.getPeers());
                        mPeersAdapter.notifyDataSetChanged();
                    }
                });
            }
        }, DELAY, DELAY, TimeUnit.MILLISECONDS);

        mServiceDiscoveryManager.startDiscovery();

        setStopProcessOnClick();
    }
}
