package de.florianisme.wakeonlan.ssh;

import android.util.Log;

import com.google.common.base.Throwables;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.TransportException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import de.florianisme.wakeonlan.shutdown.exception.CommandExecuteException;
import de.florianisme.wakeonlan.shutdown.hostkey.HostKeyStore;
import de.florianisme.wakeonlan.shutdown.hostkey.RejectedHostKey;
import de.florianisme.wakeonlan.shutdown.hostkey.TrustOnFirstUseHostKeyVerifier;

public class SshCommandRunnable implements Runnable {

    private static final int CONNECT_TIMEOUT = 5000;
    private static final int EXECUTE_TIMEOUT = 5000;
    private static final int OUTPUT_POLL_INTERVAL = 50;

    // sudo keeps this prefix when it translates its default prompt ("[sudo] password for", "[sudo] contraseña para", ...)
    private static final String SUDO_PROMPT_PREFIX = "[sudo] ";

    private final SshCommandModel commandModel;
    private final HostKeyStore hostKeyStore;
    private final boolean disconnectMeansSuccess;
    private final SshCommandListener listener;

    /**
     * @param disconnectMeansSuccess treat a connection dropped after the command was sent as success,
     *                               as happens when the command shuts the target down
     */
    public SshCommandRunnable(SshCommandModel commandModel, HostKeyStore hostKeyStore, boolean disconnectMeansSuccess, SshCommandListener listener) {
        this.commandModel = commandModel;
        this.hostKeyStore = hostKeyStore;
        this.disconnectMeansSuccess = disconnectMeansSuccess;
        this.listener = listener;
    }

    @Override
    public void run() {
        TrustOnFirstUseHostKeyVerifier hostKeyVerifier = new TrustOnFirstUseHostKeyVerifier(hostKeyStore);
        boolean commandSent = false;

        try (SSHClient sshClient = new SSHClient()) {
            sshClient.addHostKeyVerifier(hostKeyVerifier);
            sshClient.setConnectTimeout(CONNECT_TIMEOUT);
            sshClient.connect(commandModel.getSshAddress(), commandModel.getSshPort());
            listener.onTargetHostReached();

            sshClient.authPassword(commandModel.getUsername(), commandModel.getPassword());
            listener.onLoginSuccessful();

            Session session = sshClient.startSession();
            listener.onSessionStartSuccessful();

            session.allocateDefaultPTY();
            Session.Command exec = session.exec(commandModel.getCommand());
            commandSent = true;

            long deadline = System.currentTimeMillis() + EXECUTE_TIMEOUT;
            if (awaitSudoPromptOrClose(exec, deadline)) {
                // Nobody can answer the prompt, so waiting for the timeout would only delay the error
                Log.w(SshCommandRunnable.class.getSimpleName(), "Command triggered a sudo password prompt");
                listener.onSudoPromptTriggered(commandModel);
                return;
            }

            exec.join(Math.max(deadline - System.currentTimeMillis(), 1), TimeUnit.MILLISECONDS);
            Integer exitStatus = exec.getExitStatus();
            if (exitStatus == null) {
                throw new IllegalStateException("Command finished without reporting an exit status");
            }
            if (exitStatus != 0) {
                throw new CommandExecuteException("Command exited with status code " + exitStatus, exitStatus);
            }

            listener.onCommandExecuteSuccessful();
        } catch (Exception e) {
            if (disconnectMeansSuccess && commandSent && Throwables.getRootCause(e) instanceof TransportException) {
                listener.onCommandExecuteSuccessful();
                return;
            }

            RejectedHostKey rejectedHostKey = hostKeyVerifier.getRejectedHostKey();
            if (rejectedHostKey != null) {
                Log.w(SshCommandRunnable.class.getSimpleName(), "Host key of " + rejectedHostKey.getHost() + " changed to " + rejectedHostKey.getFingerprint());
                listener.onHostKeyChanged(rejectedHostKey);
                return;
            }

            Log.e(SshCommandRunnable.class.getSimpleName(), "Error during SSH execution", e);
            listener.onGeneralError(e, commandModel);
        }
    }

    /**
     * Reads the command output on this thread until the channel closes, so a sudo prompt is seen as soon
     * as it arrives. sshj's blocking read has no timeout, hence the polling on {@link InputStream#available()}.
     *
     * @return true if the output contains a sudo password prompt
     */
    private boolean awaitSudoPromptOrClose(Session.Command exec, long deadline) throws IOException, InterruptedException, ConnectionException {
        InputStream output = exec.getInputStream();
        ByteArrayOutputStream received = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];

        while (true) {
            // Checked before reading so that output arriving together with the close is still drained
            boolean closed = !exec.isOpen();

            int available;
            while ((available = output.available()) > 0) {
                int read = output.read(buffer, 0, Math.min(available, buffer.length));
                received.write(buffer, 0, read);
            }
            if (containsSudoPrompt(received)) {
                return true;
            }

            if (closed) {
                return false;
            }
            if (System.currentTimeMillis() >= deadline) {
                throw new ConnectionException("Command did not finish in time", new TimeoutException());
            }
            Thread.sleep(OUTPUT_POLL_INTERVAL);
        }
    }

    private static boolean containsSudoPrompt(ByteArrayOutputStream received) {
        return new String(received.toByteArray(), StandardCharsets.UTF_8).contains(SUDO_PROMPT_PREFIX);
    }
}
