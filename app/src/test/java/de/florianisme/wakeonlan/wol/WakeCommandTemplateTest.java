package de.florianisme.wakeonlan.wol;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.florianisme.wakeonlan.persistence.models.Device;

public class WakeCommandTemplateTest {

    @Test
    public void testDefaultTemplate() {
        assertEquals("wakeonlan -i '192.168.0.255' -p '9' 'aa:bb:cc:01:02:03'",
                WakeCommandTemplate.render(WakeCommandTemplate.DEFAULT_TEMPLATE, device("192.168.0.255")));
    }

    @Test
    public void testRepeatedPlaceholders() {
        assertEquals("echo 'aa:bb:cc:01:02:03' 'aa:bb:cc:01:02:03'",
                WakeCommandTemplate.render("echo {mac} {mac}", device("192.168.0.255")));
    }

    @Test
    public void testTemplateWithoutPlaceholders() {
        assertEquals("/usr/local/bin/wake-pc", WakeCommandTemplate.render("/usr/local/bin/wake-pc", device("192.168.0.255")));
    }

    @Test
    public void testMissingBroadcastAddressFallsBackToGlobalBroadcast() {
        assertEquals("etherwake -b '255.255.255.255'", WakeCommandTemplate.render("etherwake -b {broadcast}", device("")));
        assertEquals("etherwake -b '255.255.255.255'", WakeCommandTemplate.render("etherwake -b {broadcast}", device(null)));
    }

    @Test
    public void testValuesAreQuotedForTheShell() {
        assertEquals("wakeonlan -i '1.2.3.4'\\''; reboot; '\\''' 'aa:bb:cc:01:02:03'",
                WakeCommandTemplate.render("wakeonlan -i {broadcast} {mac}", device("1.2.3.4'; reboot; '")));
    }

    private static Device device(String broadcastAddress) {
        Device device = new Device();
        device.macAddress = "aa:bb:cc:01:02:03";
        device.broadcastAddress = broadcastAddress;
        device.port = 9;
        return device;
    }
}
