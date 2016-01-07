package fr.rsommerard.privacyaware.peer;

import android.net.wifi.p2p.WifiP2pDevice;

import java.net.InetAddress;

public class Peer {

    private String mName;
    private String mAddress;
    private InetAddress mLocalAddress;
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

    public InetAddress getLocalAddress() {
        return mLocalAddress;
    }

    public void setLocalAddress(InetAddress localAddress) {
        mLocalAddress = localAddress;
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

    @Override
    public String toString() {
        return mName;
    }

}
