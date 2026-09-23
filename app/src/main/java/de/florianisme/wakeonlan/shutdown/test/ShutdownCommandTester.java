package de.florianisme.wakeonlan.shutdown.test;

import android.content.Context;

import de.florianisme.wakeonlan.persistence.models.Device;
import de.florianisme.wakeonlan.shutdown.ShutdownExecutor;
import de.florianisme.wakeonlan.shutdown.listener.ShutdownExecutorListener;

public class ShutdownCommandTester {

    private final ShutdownExecutorListener shutdownExecutorListener;

    public ShutdownCommandTester(ShutdownExecutorListener shutdownExecutorListener) {
        this.shutdownExecutorListener = shutdownExecutorListener;
    }

    public void startShutdownCommandTest(Context context, Device device) {
        ShutdownExecutor.shutdownDevice(context, device, shutdownExecutorListener);
    }

}
