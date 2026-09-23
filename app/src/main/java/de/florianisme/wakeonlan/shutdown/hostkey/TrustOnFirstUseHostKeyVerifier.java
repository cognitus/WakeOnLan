package de.florianisme.wakeonlan.shutdown.hostkey;

import androidx.annotation.Nullable;

import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;

import net.schmizz.sshj.common.Buffer;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Accepts the host key presented on the first connection to a host and rejects any different key afterwards.
 */
public class TrustOnFirstUseHostKeyVerifier implements HostKeyVerifier {

    // SubjectPublicKeyInfo header for an Ed25519 key (RFC 8410), followed by the 32 byte raw key
    private static final byte[] X509_ED25519_PREFIX = {
            0x30, 0x2a, 0x30, 0x05, 0x06, 0x03, 0x2b, 0x65, 0x70, 0x03, 0x21, 0x00
    };
    private static final int ED25519_KEY_LENGTH = 32;

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
        byte[] hash = Hashing.sha256().hashBytes(sshKeyBlob(key)).asBytes();
        return "SHA256:" + BaseEncoding.base64().omitPadding().encode(hash);
    }

    private static byte[] sshKeyBlob(PublicKey key) {
        byte[] encoded = key.getEncoded();
        if (isX509Ed25519(encoded)) {
            // sshj picks its encoder by algorithm name, which it does not know for Conscrypt's Ed25519 keys (Android 16)
            byte[] rawKey = Arrays.copyOfRange(encoded, X509_ED25519_PREFIX.length, encoded.length);
            return new Buffer.PlainBuffer().putString("ssh-ed25519").putBytes(rawKey).getCompactData();
        }
        return new Buffer.PlainBuffer().putPublicKey(key).getCompactData();
    }

    private static boolean isX509Ed25519(byte[] encoded) {
        return encoded != null && encoded.length == X509_ED25519_PREFIX.length + ED25519_KEY_LENGTH
                && Arrays.equals(Arrays.copyOf(encoded, X509_ED25519_PREFIX.length), X509_ED25519_PREFIX);
    }
}
