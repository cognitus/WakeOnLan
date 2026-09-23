package de.florianisme.wakeonlan.wol;

import com.google.common.base.Strings;

import java.util.Optional;

import de.florianisme.wakeonlan.persistence.models.Device;
import de.florianisme.wakeonlan.ssh.SshCommandModel;

public class SshRelayModelFactory {

    private static final int DEFAULT_SSH_PORT = 22;

    public static Optional<SshCommandModel> fromDevice(Device device) {
        if (!device.wakeViaSsh || Strings.isNullOrEmpty(device.relaySshAddress) || Strings.isNullOrEmpty(device.relaySshUsername)
                || Strings.isNullOrEmpty(device.relaySshCommand)) {
            return Optional.empty();
        }

        int port = device.relaySshPort != null && device.relaySshPort > 0 ? device.relaySshPort : DEFAULT_SSH_PORT;
        String password = Strings.nullToEmpty(device.relaySshPassword);
        String command = WakeCommandTemplate.render(device.relaySshCommand, device);

        return Optional.of(new SshCommandModel(device.relaySshAddress, port, device.relaySshUsername, password, command));
    }
}
