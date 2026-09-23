package de.florianisme.wakeonlan.ssh;

import android.content.Context;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import de.florianisme.wakeonlan.shutdown.hostkey.HostKeyStore;

public class SshCommandExecutor {

    private static final Executor executor = Executors.newSingleThreadExecutor();

    static {
        // Override Android's BC implementation with official BC Provider
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
    }

    public static void execute(Context context, SshCommandModel commandModel, boolean disconnectMeansSuccess, SshCommandListener listener) {
        executor.execute(new SshCommandRunnable(commandModel, new HostKeyStore(context.getApplicationContext()), disconnectMeansSuccess, listener));
    }
}
