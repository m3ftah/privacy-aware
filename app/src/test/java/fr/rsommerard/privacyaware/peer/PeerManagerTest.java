package fr.rsommerard.privacyaware.peer;

import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Build;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import fr.rsommerard.privacyaware.BuildConfig;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class PeerManagerTest {

    private static PeerManager sPeerManager;

    @BeforeClass
    public static void setup() {
        sPeerManager = PeerManager.getInstance();
    }

    @AfterClass
    public static void cleanup() {
        sPeerManager.destroy();
    }

    @Test
    public void testSingleton() {
        PeerManager peerManager = PeerManager.getInstance();

        assertEquals("should be equals", sPeerManager, peerManager);
    }

    @Test
    public void testAddPeer() {
        Peer peer1 = createPeer1();

        sPeerManager.addPeer(peer1);
        List<Peer> peers = sPeerManager.getPeers();

        int nbPeers = peers.size();
        sPeerManager.addPeer(peer1);
        peers = sPeerManager.getPeers();

        assertEquals("should be the same number of peers", nbPeers, peers.size());

        nbPeers = peers.size();

        Peer peer2 = createPeer2();

        sPeerManager.addPeer(peer2);
        peers = sPeerManager.getPeers();

        assertEquals("should be nbPeers + 1", nbPeers + 1, peers.size());
    }

    @Test
    public void testGetPeer() {
        Peer peer = createPeer1();

        sPeerManager.addPeer(peer);

        assertNotNull("should return a peer", sPeerManager.getPeer());
    }

    @Test
    public void testGetPeerNullNamedWithAddress() {
        Peer peer = createNullNamedPeer();

        sPeerManager.addPeer(peer);

        assertEquals("should return the same peer", peer, sPeerManager.getPeer(peer.getAddress()));
    }

    @Test
    public void testGetPeerWithAddress() {
        Peer peer = createPeer3();

        sPeerManager.addPeer(peer);

        assertEquals("should return the same peer", peer, sPeerManager.getPeer(peer.getAddress()));
    }

    @Test
    public void testHasPeers() {
        Peer peer = createPeer1();

        sPeerManager.addPeer(peer);

        assertTrue("should be true", sPeerManager.hasPeers());
    }

    @Test
    public void testCleaningPeers() {
        Peer peer = createPeer1();

        sPeerManager.addPeer(peer);

        try {
            sleep(185000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertFalse("should be cleaned", sPeerManager.hasPeers());
    }

    private Peer createPeer1() {
        WifiP2pDevice device = mock(WifiP2pDevice.class);
        device.deviceAddress = "7e:27:57:ae:57:ce";
        device.deviceName = "Android_bea6";

        return new Peer(device, 1001);
    }

    private Peer createPeer2() {
        WifiP2pDevice device = mock(WifiP2pDevice.class);
        device.deviceAddress = "7e:45:57:ae:43:ce";
        device.deviceName = "Android_ffa6";

        return new Peer(device, 11);
    }

    private Peer createPeer3() {
        WifiP2pDevice device = mock(WifiP2pDevice.class);
        device.deviceAddress = "66:45:57:fe:43:ce";
        device.deviceName = "Android_ea94";

        return new Peer(device, 8920);
    }

    private Peer createNullNamedPeer() {
        WifiP2pDevice device = mock(WifiP2pDevice.class);
        device.deviceAddress = "7e:27:57:ae:57:ce";
        device.deviceName = null;

        return new Peer(device, 12455);
    }
}
