package fr.rsommerard.privacyaware.wifidirect.connection;

import java.net.InetAddress;

public class ConnectedEvent {

    public final InetAddress groupOwnerAddress;

    public ConnectedEvent(final InetAddress groupOwnerAddress) {
        this.groupOwnerAddress = groupOwnerAddress;
    }
}
