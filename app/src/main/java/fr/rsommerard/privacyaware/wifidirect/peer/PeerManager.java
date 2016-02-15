package fr.rsommerard.privacyaware.wifidirect.peer;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PeerManager {

    private static final String TAG = "PAPM";

    private static PeerManager sInstance;

    private final List<Peer> mPeers;
    private final Random mRand;

    private ScheduledExecutorService mExecutor;

    public static PeerManager getInstance() {
        if (sInstance == null) {
            sInstance = new PeerManager();
        }

        return sInstance;
    }

    private PeerManager() {
        //Log.i(TAG, "PeerManager()");

        mPeers = new ArrayList<>();
        mRand = new Random();

        startCleaningExecutor();
    }

    public void startCleaningExecutor() {
        if (mExecutor != null) {
            return;
        }

        mExecutor = Executors.newSingleThreadScheduledExecutor();
        mExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                //Log.i(TAG, "run()");

                mPeers.clear();
            }
        }, 0, 181000, TimeUnit.MILLISECONDS);
    }

    public void stopCleaningExecutor() {
        if (mExecutor != null) {
            mExecutor.shutdown();
        }
    }

    public void destroy() {
        Log.i(TAG, "destroy()");

        stopCleaningExecutor();
    }

    public Peer getPeer() {
        //Log.i(TAG, "getPeer()");

        if (mPeers.isEmpty()) {
            return null;
        }

        return mPeers.get(mRand.nextInt(mPeers.size()));
    }

    public Peer getPeer(final String address) {
        //Log.i(TAG, "getPeer(String name)");

        Log.d(TAG, address);
        Log.d(TAG, mPeers.toString());

        for (Peer peer : mPeers) {
            //Log.d(TAG, "\"" + peer.getAddress() + "\".equals(\"" + address + "\")");
            if (peer.getAddress().equals(address)) {
                return peer;
            }
        }

        return null;
    }

    public List<Peer> getAllPeers() {
        //Log.i(TAG, "getPeers()");

        return mPeers;
    }

    public boolean hasPeers() {
        return !mPeers.isEmpty();
    }

    public void addPeer(final Peer peer) {
        //Log.i(TAG, "addPeer(Peer peer)");

        if (mPeers.contains(peer)) {
            return;
        }

        mPeers.add(peer);

        Log.d(TAG, "Peers: " + mPeers.toString());
    }
}
