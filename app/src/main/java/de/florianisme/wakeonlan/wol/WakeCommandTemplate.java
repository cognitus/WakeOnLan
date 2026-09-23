package de.florianisme.wakeonlan.wol;

import com.google.common.base.Strings;

import de.florianisme.wakeonlan.persistence.models.Device;

/**
 * Builds the command the SSH relay runs to wake a device. Placeholders are replaced by the device's
 * values, each quoted for a POSIX shell because the broadcast address is not validated in the form.
 */
public class WakeCommandTemplate {

    public static final String DEFAULT_TEMPLATE = "wakeonlan -i {broadcast} -p {port} {mac}";

    static final String DEFAULT_BROADCAST_ADDRESS = "255.255.255.255";

    public static String render(String template, Device device) {
        String broadcastAddress = Strings.isNullOrEmpty(device.broadcastAddress) ? DEFAULT_BROADCAST_ADDRESS : device.broadcastAddress;

        return template
                .replace("{mac}", shellQuote(device.macAddress))
                .replace("{broadcast}", shellQuote(broadcastAddress))
                .replace("{port}", shellQuote(String.valueOf(device.port)));
    }

    static String shellQuote(String value) {
        return "'" + Strings.nullToEmpty(value).replace("'", "'\\''") + "'";
    }
}
