package de.florianisme.wakeonlan.ssh;

import androidx.annotation.Nullable;

import de.florianisme.wakeonlan.shutdown.hostkey.RejectedHostKey;

public interface SshCommandListener {

    void onTargetHostReached();

    void onLoginSuccessful();

    void onSessionStartSuccessful();

    void onCommandExecuteSuccessful();

    void onSudoPromptTriggered(SshCommandModel commandModel);

    void onHostKeyChanged(RejectedHostKey rejectedHostKey);

    void onGeneralError(Exception exception, @Nullable SshCommandModel commandModel);

}
