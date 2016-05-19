package fr.rsommerard.privacyaware.wifidirect.device;

import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Build;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import fr.rsommerard.privacyaware.BuildConfig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class DeviceManagerTest {

    private static DeviceManager sDeviceManager;

    @BeforeClass
    public static void setup() {
        sDeviceManager = DeviceManager.getInstance();
    }

    @AfterClass
    public static void cleanup() {
        sDeviceManager.destroy();
    }

    @Test
    public void testSingleton() {
        DeviceManager deviceManager = DeviceManager.getInstance();

        assertEquals("should be equals", sDeviceManager, deviceManager);
    }

    @Test
    public void testAddPeer() {
        Device device1 = createPeer1();

        sDeviceManager.addPeer(device1);
        List<Device> devices = sDeviceManager.getAllPeers();

        int nbPeers = devices.size();
        sDeviceManager.addPeer(device1);
        devices = sDeviceManager.getAllPeers();

        assertEquals("should be the same number of devices", nbPeers, devices.size());

        nbPeers = devices.size();

        Device device2 = createPeer2();

        sDeviceManager.addPeer(device2);
        devices = sDeviceManager.getAllPeers();

        assertEquals("should be nbPeers + 1", nbPeers + 1, devices.size());
    }

    @Test
    public void testGetPeer() {
        Device device = createPeer1();

        sDeviceManager.addPeer(device);

        assertNotNull("should return a device", sDeviceManager.getPeer());
    }

    @Test
    public void testGetPeerNullNamedWithAddress() {
        Device device = createNullNamedPeer();

        sDeviceManager.addPeer(device);

        assertEquals("should return the same device", device, sDeviceManager.getPeer(device.getAddress()));
    }

    @Test
    public void testGetPeerWithAddress() {
        Device device = createPeer3();

        sDeviceManager.addPeer(device);

        assertEquals("should return the same device", device, sDeviceManager.getPeer(device.getAddress()));
    }

    @Test
    public void testHasPeers() {
        Device device = createPeer1();

        sDeviceManager.addPeer(device);

        assertTrue("should be true", sDeviceManager.hasPeers());
    }

    private Device createPeer1() {
        WifiP2pDevice device = mock(WifiP2pDevice.class);
        device.deviceAddress = "7e:27:57:ae:57:ce";
        device.deviceName = "Android_bea6";

        return new Device(device, 1001);
    }

    private Device createPeer2() {
        WifiP2pDevice device = mock(WifiP2pDevice.class);
        device.deviceAddress = "7e:45:57:ae:43:ce";
        device.deviceName = "Android_ffa6";

        return new Device(device, 11);
    }

    private Device createPeer3() {
        WifiP2pDevice device = mock(WifiP2pDevice.class);
        device.deviceAddress = "66:45:57:fe:43:ce";
        device.deviceName = "Android_ea94";

        return new Device(device, 8920);
    }

    private Device createNullNamedPeer() {
        WifiP2pDevice device = mock(WifiP2pDevice.class);
        device.deviceAddress = "7e:27:57:ae:57:ce";
        device.deviceName = null;

        return new Device(device, 12455);
    }
}
