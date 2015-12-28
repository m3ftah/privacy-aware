package fr.rsommerard.privacyaware;

import android.net.wifi.p2p.WifiP2pDevice;

public class Peer {

    private String mName;
    private String mAddress;
    private int mPort;

    public Peer(WifiP2pDevice peer, int port) {
        mName = peer.deviceName;
        mAddress = peer.deviceAddress;
        mPort = port;
    }

    public Peer(WifiP2pDevice peer, String port) {
        this(peer, Integer.parseInt(port));
    }

    @Override
    public boolean equals(Object obj) {
        Peer peer = (Peer) obj;

        if (peer.getAddress().equals(mAddress)) {
            return true;
        }

        return false;
    }

    public String getAddress() {
        return mAddress;
    }

    public String getName() {
        return mName;
    }

    public int getPort() {
        return mPort;
    }

    public String toString() {
        return mName;
    }
}
