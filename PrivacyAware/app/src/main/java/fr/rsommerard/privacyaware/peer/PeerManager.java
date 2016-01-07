package fr.rsommerard.privacyaware.peer;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PeerManager {

    private final String TAG = "PAPM";

    private static PeerManager sInstance;

    private final int TIMEOUT = 61000;

    private List<Peer> mPeers;
    private Random mRand;
    private ScheduledExecutorService mExecutor;

    public static PeerManager getInstance() {
        if (sInstance == null) {
            sInstance = new PeerManager();
        }

        return sInstance;
    }

    private PeerManager() {
        Log.i(TAG, "PeerManager()");

        mPeers = new ArrayList<>();
        mRand = new Random();

        mExecutor = Executors.newSingleThreadScheduledExecutor();
        mExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                //Log.i(TAG, "run()");

                mPeers.clear();
            }
        }, 0, TIMEOUT, TimeUnit.MILLISECONDS);
    }

    public void purge() {
        Log.i(TAG, "purge()");

        mExecutor.shutdown();
    }

    public Peer getPeer() {
        Log.i(TAG, "getPeer()");

        if (mPeers.isEmpty()) {
            return null;
        }

        return mPeers.get(mRand.nextInt(mPeers.size()));
    }

    public Peer getPeer(String address) {
        Log.i(TAG, "getPeer(String name)");

        for (Peer peer : mPeers) {
            if (peer.getAddress().equals(address)) {
                return peer;
            }
        }

        return null;
    }

    public List<Peer> getPeers() {
        //Log.i(TAG, "getPeers()");

        return mPeers;
    }

    public void addPeer(Peer peer) {
        //Log.i(TAG, "addPeer()");

        if (mPeers.contains(peer)) {
            return;
        }

        mPeers.add(peer);

        Log.d(TAG, mPeers.toString());
    }
}
