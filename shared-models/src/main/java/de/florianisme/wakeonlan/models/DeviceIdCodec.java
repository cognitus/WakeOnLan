package de.florianisme.wakeonlan.models;

import java.nio.ByteBuffer;

/**
 * Encodes the device id sent from the watch to the phone when a device is clicked.
 */
public class DeviceIdCodec {

    private DeviceIdCodec() {
    }

    public static byte[] encode(int deviceId) {
        // Small ids keep the legacy single byte format so phone apps up to 2.0.4 still understand them
        if (deviceId >= 0 && deviceId <= Byte.MAX_VALUE) {
            return new byte[]{(byte) deviceId};
        }
        return ByteBuffer.allocate(Integer.BYTES).putInt(deviceId).array();
    }

    public static int decode(byte[] data) {
        if (data.length == Integer.BYTES) {
            return ByteBuffer.wrap(data).getInt();
        }
        if (data.length == 1) {
            // Watch apps up to 2.0.4 send the id as a single (signed) byte
            return data[0] & 0xFF;
        }
        throw new IllegalArgumentException("Invalid device id payload of " + data.length + " bytes");
    }
}
