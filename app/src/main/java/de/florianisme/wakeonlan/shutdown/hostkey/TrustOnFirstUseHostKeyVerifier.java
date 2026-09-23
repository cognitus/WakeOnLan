package de.florianisme.wakeonlan.shutdown.hostkey;

import androidx.annotation.Nullable;

import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;

import net.schmizz.sshj.common.Buffer;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;

import java.security.PublicKey;
import java.util.Collections;
import java.util.List;

/**
 * Accepts the host key presented on the first connection to a host and rejects any different key afterwards.
 */
public class TrustOnFirstUseHostKeyVerifier implements HostKeyVerifier {

    private final HostKeyStore hostKeyStore;

    @Nullable
    private RejectedHostKey rejectedHostKey;

    public TrustOnFirstUseHostKeyVerifier(HostKeyStore hostKeyStore) {
        this.hostKeyStore = hostKeyStore;
    }

    @Override
    public boolean verify(String hostname, int port, PublicKey key) {
        String fingerprint = fingerprint(key);
        String knownFingerprint = hostKeyStore.getFingerprint(hostname, port);

        if (knownFingerprint == null) {
            hostKeyStore.trust(hostname, port, fingerprint);
            return true;
        }
        if (knownFingerprint.equals(fingerprint)) {
            return true;
        }

        rejectedHostKey = new RejectedHostKey(hostname, port, fingerprint);
        return false;
    }

    @Override
    public List<String> findExistingAlgorithms(String hostname, int port) {
        return Collections.emptyList();
    }

    /**
     * @return the key that did not match the known one, if the last verification failed
     */
    @Nullable
    public RejectedHostKey getRejectedHostKey() {
        return rejectedHostKey;
    }

    /**
     * Same format as {@code ssh-keygen -l}, so users can compare it against the host.
     */
    static String fingerprint(PublicKey key) {
        byte[] keyBlob = new Buffer.PlainBuffer().putPublicKey(key).getCompactData();
        byte[] hash = Hashing.sha256().hashBytes(keyBlob).asBytes();
        return "SHA256:" + BaseEncoding.base64().omitPadding().encode(hash);
    }
}
