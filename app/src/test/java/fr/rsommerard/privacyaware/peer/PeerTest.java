package fr.rsommerard.privacyaware.wifidirect.peer;

import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Build;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.net.InetAddress;
import java.net.UnknownHostException;

import fr.rsommerard.privacyaware.BuildConfig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class PeerTest {

    private WifiP2pDevice mDevice;

    @Before
    public void setup() {
        mDevice = mock(WifiP2pDevice.class);
        mDevice.deviceAddress = "ce:ae:bb:27:57:7e";
        mDevice.deviceName = "Android_19e4";
    }

    @Test
    public void testPeerWithIntegerPort() {
        Peer peer = createPeer();

        assertNotNull("should not be null", peer);
    }

    @Test
    public void testPeerWithStringPort() {
        Peer peer = new Peer(mDevice, "42");

        assertNotNull("should not be null", peer);
    }

    @Test
    public void testEquals() {
        Peer peer1 = createPeer();
        Peer peer2 = createPeer();

        Peer peer3 = createOtherPeer();

        assertEquals("should be equals", peer1, peer2);
        assertEquals("should be equals", peer2, peer1);
        assertEquals("should be equals", peer3, peer3);
        assertNotEquals("should not be equals", peer1, peer3);
        assertNotEquals("should not be equals", peer3, peer2);
    }

    @Test
    public void testLocalAddress() throws UnknownHostException {
        Peer peer = createPeer();

        assertNull("should be null", peer.getLocalAddress());

        InetAddress inetAddress = InetAddress.getByName("192.168.0.1");
        peer.setLocalAddress(inetAddress);

        assertEquals("should be equals", peer.getLocalAddress(), inetAddress);
    }

    @Test
    public void testGetName() {
        Peer peer = createPeer();

        assertEquals("should be equals", "Android_19e4", peer.getName());
    }

    @Test
    public void testGetPort() {
        Peer peer = createPeer();

        assertEquals("should be equals", 42, peer.getPort());
    }

    @Test
    public void testToString() {
        Peer peer = createPeer();

        assertEquals("should be equals", "Android_19e4", peer.toString());
    }

    private Peer createPeer() {
        return new Peer(mDevice, 42);
    }

    private Peer createOtherPeer() {
        WifiP2pDevice device = mock(WifiP2pDevice.class);
        device.deviceAddress = "7e:27:57:ae:57:ce";
        device.deviceName = "Android_bea6";

        return new Peer(device, 1001);
    }
}
