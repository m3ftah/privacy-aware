package fr.rsommerard.privacyaware.wifidirect.device;

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
public class DeviceTest {

    private WifiP2pDevice mDevice;

    @Before
    public void setup() {
        mDevice = mock(WifiP2pDevice.class);
        mDevice.deviceAddress = "ce:ae:bb:27:57:7e";
        mDevice.deviceName = "Android_19e4";
    }

    @Test
    public void testPeerWithIntegerPort() {
        Device device = createPeer();

        assertNotNull("should not be null", device);
    }

    @Test
    public void testPeerWithStringPort() {
        Device device = new Device(mDevice, "42");

        assertNotNull("should not be null", device);
    }

    @Test
    public void testEquals() {
        Device device1 = createPeer();
        Device device2 = createPeer();

        Device device3 = createOtherPeer();

        assertEquals("should be equals", device1, device2);
        assertEquals("should be equals", device2, device1);
        assertEquals("should be equals", device3, device3);
        assertNotEquals("should not be equals", device1, device3);
        assertNotEquals("should not be equals", device3, device2);
    }

    @Test
    public void testLocalAddress() throws UnknownHostException {
        Device device = createPeer();

        assertNull("should be null", device.getLocalAddress());

        InetAddress inetAddress = InetAddress.getByName("192.168.0.1");
        device.setLocalAddress(inetAddress);

        assertEquals("should be equals", device.getLocalAddress(), inetAddress);
    }

    @Test
    public void testGetName() {
        Device device = createPeer();

        assertEquals("should be equals", "Android_19e4", device.getName());
    }

    @Test
    public void testGetPort() {
        Device device = createPeer();

        assertEquals("should be equals", 42, device.getPort());
    }

    @Test
    public void testToString() {
        Device device = createPeer();

        assertEquals("should be equals", "Android_19e4", device.toString());
    }

    private Device createPeer() {
        return new Device(mDevice, 42);
    }

    private Device createOtherPeer() {
        WifiP2pDevice device = mock(WifiP2pDevice.class);
        device.deviceAddress = "7e:27:57:ae:57:ce";
        device.deviceName = "Android_bea6";

        return new Device(device, 1001);
    }
}
