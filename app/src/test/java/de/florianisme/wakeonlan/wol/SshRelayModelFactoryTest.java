package de.florianisme.wakeonlan.wol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.florianisme.wakeonlan.persistence.models.Device;
import de.florianisme.wakeonlan.ssh.SshCommandModel;

public class SshRelayModelFactoryTest {

    @Test
    public void testDefaultsForPortAndPassword() {
        Device device = relayDevice();
        device.relaySshPort = -1;
        device.relaySshPassword = null;

        SshCommandModel model = SshRelayModelFactory.fromDevice(device).orElseThrow();

        assertEquals("192.168.0.2", model.getSshAddress());
        assertEquals(22, model.getSshPort());
        assertEquals("pi", model.getUsername());
        assertEquals("", model.getPassword());
        assertEquals("wakeonlan 'aa:bb:cc:01:02:03'", model.getCommand());
    }

    @Test
    public void testConfiguredPort() {
        Device device = relayDevice();
        device.relaySshPort = 2222;

        assertEquals(2222, SshRelayModelFactory.fromDevice(device).orElseThrow().getSshPort());
    }

    @Test
    public void testEmptyWhenDisabled() {
        Device device = relayDevice();
        device.wakeViaSsh = false;

        assertTrue(SshRelayModelFactory.fromDevice(device).isEmpty());
    }

    @Test
    public void testEmptyWhenRequiredFieldIsMissing() {
        Device noAddress = relayDevice();
        noAddress.relaySshAddress = "";
        Device noUsername = relayDevice();
        noUsername.relaySshUsername = null;
        Device noCommand = relayDevice();
        noCommand.relaySshCommand = "";

        assertTrue(SshRelayModelFactory.fromDevice(noAddress).isEmpty());
        assertTrue(SshRelayModelFactory.fromDevice(noUsername).isEmpty());
        assertTrue(SshRelayModelFactory.fromDevice(noCommand).isEmpty());
    }

    private static Device relayDevice() {
        Device device = new Device();
        device.macAddress = "aa:bb:cc:01:02:03";
        device.port = 9;
        device.wakeViaSsh = true;
        device.relaySshAddress = "192.168.0.2";
        device.relaySshUsername = "pi";
        device.relaySshCommand = "wakeonlan {mac}";
        return device;
    }
}
