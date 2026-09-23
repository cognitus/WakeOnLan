package de.florianisme.wakeonlan.wol;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.net.DatagramPacket;
import java.net.UnknownHostException;
import java.util.Arrays;

public class PacketBuilderTest {

    private static final byte[] MAC = {(byte) 0xAA, (byte) 0xBB, (byte) 0xCC, 0x01, 0x02, 0x03};

    @Test
    public void testBuildMagicPacket_withoutPassword() throws UnknownHostException {
        DatagramPacket packet = PacketBuilder.buildMagicPacket("192.168.0.255", "aa:bb:cc:01:02:03", 9, null);

        byte[] data = packet.getData();
        assertEquals(102, packet.getLength());
        assertEquals(9, packet.getPort());
        assertEquals("192.168.0.255", packet.getAddress().getHostAddress());
        for (int i = 0; i < 6; i++) {
            assertEquals((byte) 0xFF, data[i]);
        }
        for (int i = 0; i < 16; i++) {
            assertArrayEquals(MAC, Arrays.copyOfRange(data, 6 + i * 6, 12 + i * 6));
        }
    }

    @Test
    public void testBuildMagicPacket_withDashSeparatedMac() throws UnknownHostException {
        DatagramPacket packet = PacketBuilder.buildMagicPacket("192.168.0.255", "AA-BB-CC-01-02-03", 9, "");

        assertEquals(102, packet.getLength());
        assertArrayEquals(MAC, Arrays.copyOfRange(packet.getData(), 6, 12));
    }

    @Test
    public void testBuildMagicPacket_withIpPassword() throws UnknownHostException {
        DatagramPacket packet = PacketBuilder.buildMagicPacket("192.168.0.255", "aa:bb:cc:01:02:03", 9, "192.168.0.255");

        assertEquals(106, packet.getLength());
        assertArrayEquals(new byte[]{(byte) 192, (byte) 168, 0, (byte) 255}, Arrays.copyOfRange(packet.getData(), 102, 106));
    }

    @Test
    public void testBuildMagicPacket_withMacPassword() throws UnknownHostException {
        DatagramPacket packet = PacketBuilder.buildMagicPacket("192.168.0.255", "aa:bb:cc:01:02:03", 9, "aa:bb:cc:01:02:03");

        assertEquals(108, packet.getLength());
        assertArrayEquals(MAC, Arrays.copyOfRange(packet.getData(), 102, 108));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildMagicPacket_withIpPasswordOctetOutOfRange() throws UnknownHostException {
        PacketBuilder.buildMagicPacket("192.168.0.255", "aa:bb:cc:01:02:03", 9, "192.168.0.256");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildMagicPacket_withInvalidPassword() throws UnknownHostException {
        PacketBuilder.buildMagicPacket("192.168.0.255", "aa:bb:cc:01:02:03", 9, "secret");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildMagicPacket_withInvalidMac() throws UnknownHostException {
        PacketBuilder.buildMagicPacket("192.168.0.255", "aa:bb:cc:01:02", 9, null);
    }
}
