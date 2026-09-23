package de.florianisme.wakeonlan.shutdown.listener;

import androidx.annotation.Nullable;

import de.florianisme.wakeonlan.shutdown.ShutdownModel;
import de.florianisme.wakeonlan.shutdown.hostkey.RejectedHostKey;

public interface ShutdownExecutorListener {

    void onTargetHostReached();

    void onLoginSuccessful();

    void onSessionStartSuccessful();

    void onCommandExecuteSuccessful();

    void onSudoPromptTriggered(ShutdownModel shutdownModel);

    void onHostKeyChanged(RejectedHostKey rejectedHostKey);

    void onGeneralError(Exception exception, @Nullable ShutdownModel shutdownModel);

}
