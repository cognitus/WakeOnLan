package de.florianisme.wakeonlan.wear;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.florianisme.wakeonlan.models.DeviceIdCodec;

public class DeviceIdCodecTest {

    @Test
    public void testRoundTrip() {
        for (int id : new int[]{0, 1, 127, 128, 255, 256, 70_000, Integer.MAX_VALUE}) {
            assertEquals(id, DeviceIdCodec.decode(DeviceIdCodec.encode(id)));
        }
    }

    @Test
    public void testEncode_smallIdUsesLegacyFormat() {
        assertEquals(1, DeviceIdCodec.encode(127).length);
        assertEquals(4, DeviceIdCodec.encode(128).length);
    }

    @Test
    public void testDecode_legacyPayloadAbove127() {
        // Old watch apps sent (byte) 200, which is negative when read as a signed byte
        assertEquals(200, DeviceIdCodec.decode(new byte[]{(byte) 200}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecode_invalidLength() {
        DeviceIdCodec.decode(new byte[]{1, 2});
    }
}
