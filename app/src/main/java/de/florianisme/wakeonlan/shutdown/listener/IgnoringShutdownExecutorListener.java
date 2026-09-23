package de.florianisme.wakeonlan.shutdown.listener;

import de.florianisme.wakeonlan.shutdown.ShutdownModel;
import de.florianisme.wakeonlan.shutdown.hostkey.RejectedHostKey;

public class IgnoringShutdownExecutorListener implements ShutdownExecutorListener {

    @Override
    public void onTargetHostReached() {
        // Ignore
    }

    @Override
    public void onLoginSuccessful() {
        // Ignore
    }

    @Override
    public void onSessionStartSuccessful() {
        // Ignore
    }

    @Override
    public void onCommandExecuteSuccessful() {
        // Ignore
    }

    @Override
    public void onSudoPromptTriggered(ShutdownModel shutdownModel) {
        // Ignore
    }

    @Override
    public void onHostKeyChanged(RejectedHostKey rejectedHostKey) {
        // Ignore
    }

    @Override
    public void onGeneralError(Exception exception, ShutdownModel shutdownModel) {
        // Ignore
    }
}
