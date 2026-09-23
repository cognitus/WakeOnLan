package de.florianisme.wakeonlan.wol;

import android.content.Context;
import android.util.Log;

import java.util.Optional;

import de.florianisme.wakeonlan.R;
import de.florianisme.wakeonlan.persistence.models.Device;
import de.florianisme.wakeonlan.ssh.SshCommandExecutor;
import de.florianisme.wakeonlan.ssh.SshCommandListener;
import de.florianisme.wakeonlan.ssh.SshCommandModel;
import de.florianisme.wakeonlan.ssh.SshFailureNotifier;

/**
 * Wakes a device either directly with a magic packet or, if configured, by having an SSH relay in the
 * device's network send it.
 */
public class WakeDispatcher {

    public static void wake(Context context, Device device) {
        if (!device.wakeViaSsh) {
            WolSender.sendWolPacket(device);
            return;
        }

        wakeViaSsh(context, device, new SshFailureNotifier(context, device, R.string.ssh_failure_notification_title_wake));
    }

    public static void wakeViaSsh(Context context, Device device, SshCommandListener listener) {
        Optional<SshCommandModel> relayModel = SshRelayModelFactory.fromDevice(device);

        if (relayModel.isEmpty()) {
            Log.w(WakeDispatcher.class.getSimpleName(), "Can not wake device via SSH. Not all required fields were set");
            listener.onGeneralError(new IllegalArgumentException("Can not wake device via SSH. Not all required fields were set"), null);
            return;
        }

        // A relay that drops the connection did not necessarily send the packet
        SshCommandExecutor.execute(context, relayModel.get(), false, listener);
    }
}
