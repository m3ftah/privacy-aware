package fr.rsommerard.privacyaware.wifidirect.device;

import android.net.wifi.p2p.WifiP2pDevice;

import java.net.InetAddress;

public class Device {

    private final String mName;
    private final String mAddress;
    private InetAddress mLocalAddress;
    private final int mPort;

    public Device(final WifiP2pDevice peer, final int port) {
        mName = peer.deviceName;
        mAddress = peer.deviceAddress;
        mPort = port;
    }

    public Device(final WifiP2pDevice peer, final String port) {
        this(peer, Integer.parseInt(port));
    }

    @Override
    public boolean equals(final Object obj) {
        final Device device = (Device) obj;

        return device.getAddress().equals(mAddress);
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
