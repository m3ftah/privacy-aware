package fr.rsommerard.privacyaware;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class PeerManager {

    private final String TAG = PeerManager.class.getSimpleName();

    private static PeerManager sInstance;

    private final int TIMEOUT = 61000;

    private List<Peer> mPeers;
    private Random mRand;
    private Timer mTimer;

    public static PeerManager getInstance() {
        if (sInstance == null) {
            sInstance = new PeerManager();
        }

        return sInstance;
    }

    private PeerManager() {
        mPeers = new ArrayList<>();
        mRand = new Random();

        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                mPeers.clear();
            }
        }, 0, TIMEOUT);
    }

    public Peer getPeer() {
        if (mPeers.isEmpty()) {
            return null;
        }

        return mPeers.get(mRand.nextInt(mPeers.size()));
    }

    public List<Peer> getPeers() {
        return mPeers;
    }

    public void addPeer(Peer peer) {
        if (mPeers.contains(peer)) {
            return;
        }

        mPeers.add(peer);
    }
}
