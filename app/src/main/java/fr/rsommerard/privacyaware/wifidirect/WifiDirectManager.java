package fr.rsommerard.privacyaware.wifidirect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import fr.rsommerard.privacyaware.data.DataManager;
import fr.rsommerard.privacyaware.wifidirect.connection.ConnectionManager;
import fr.rsommerard.privacyaware.wifidirect.connection.ServiceDiscoveryManager;
import fr.rsommerard.privacyaware.wifidirect.peer.Peer;
import fr.rsommerard.privacyaware.wifidirect.peer.PeerManager;

public class WifiDirectManager {

    private static final String TAG = "PAWDM";

    private static WifiDirectManager sInstance;

    private final Context mContext;
    private final ScheduledExecutorService mExecutor;
    private final DataManager mDataManager;

    private ConnectionManager mConnectionManager;
    private ServiceDiscoveryManager mServiceDiscoveryManager;
    private PeerManager mPeerManager;

    public static WifiDirectManager getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new WifiDirectManager(context);
        }

        return sInstance;
    }

    private WifiDirectManager(final Context context) {
        //Log.i(TAG, "WifiDirectManager()");

        mContext = context;

        mPeerManager = PeerManager.getInstance();
        mDataManager = DataManager.getInstance();

        mConnectionManager = ConnectionManager.getInstance(mContext);
        mServiceDiscoveryManager =
                ServiceDiscoveryManager.getInstance(mContext, mConnectionManager.getPassiveThreadPort());

        // TODO: is it useful
        Random random = new Random();
        int delay = random.nextInt(181000 - 17000) + 17000;
        Log.d(TAG, "Delay: " + delay);

        mExecutor = Executors.newSingleThreadScheduledExecutor();
        mExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "run()");

                process();
            }
        }, delay, 181000, TimeUnit.MILLISECONDS);
    }

    private void process() {
        Log.i(TAG, "process()");

        if (!mPeerManager.hasPeers()) {
            Log.d(TAG, "No peers available");
            return;
        }

        if (!mDataManager.hasData()) {
            Log.e(TAG, "No data");
            return;
        }

        Peer peer = mPeerManager.getPeer();

        mConnectionManager.connect(peer);
    }

    public void destroy() {
        //Log.i(TAG, "destroy()");

        mConnectionManager.destroy();
        mServiceDiscoveryManager.destroy();

        mPeerManager.destroy();
    }
}
