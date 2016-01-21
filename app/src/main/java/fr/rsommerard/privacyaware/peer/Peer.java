package fr.rsommerard.privacyaware.peer;

import android.net.wifi.p2p.WifiP2pDevice;

import java.net.InetAddress;

public class Peer {

    private final String mName;
    private final String mAddress;
    private InetAddress mLocalAddress;
    private final int mPort;

    public Peer(final WifiP2pDevice peer, final int port) {
        mName = peer.deviceName;
        mAddress = peer.deviceAddress;
        mPort = port;
    }

    public Peer(final WifiP2pDevice peer, final String port) {
        this(peer, Integer.parseInt(port));
    }

    @Override
    public boolean equals(final Object obj) {
        final Peer peer = (Peer) obj;

        return peer.getAddress().equals(mAddress);
    }

    public InetAddress getLocalAddress() {
        return mLocalAddress;
    }

    public void setLocalAddress(final InetAddress localAddress) {
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
