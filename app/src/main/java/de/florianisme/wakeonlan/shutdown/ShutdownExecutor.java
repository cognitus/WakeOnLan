package de.florianisme.wakeonlan.shutdown;

import android.content.Context;
import android.util.Log;

import java.util.Optional;

import de.florianisme.wakeonlan.R;
import de.florianisme.wakeonlan.persistence.models.Device;
import de.florianisme.wakeonlan.ssh.SshCommandExecutor;
import de.florianisme.wakeonlan.ssh.SshCommandListener;
import de.florianisme.wakeonlan.ssh.SshCommandModel;
import de.florianisme.wakeonlan.ssh.SshFailureNotifier;

public class ShutdownExecutor {

    public static void shutdownDevice(Context context, Device device, SshCommandListener listener) {
        Optional<SshCommandModel> optionalShutdownModel = ShutdownModelFactory.fromDevice(device);

        if (optionalShutdownModel.isEmpty()) {
            Log.w(ShutdownExecutor.class.getSimpleName(), "Can not shutdown device. Not all required fields were set");
            listener.onGeneralError(new IllegalArgumentException("Can not shutdown device. Not all required fields were set"), null);
            return;
        }

        // The target usually drops the connection while shutting down
        SshCommandExecutor.execute(context, optionalShutdownModel.get(), true, listener);
    }

    /**
     * Shuts the device down without a UI waiting for the result. Failures are reported as a notification.
     */
    public static void shutdownDevice(Context context, Device device) {
        shutdownDevice(context, device, new SshFailureNotifier(context, device, R.string.ssh_failure_notification_title_shutdown));
    }
}
