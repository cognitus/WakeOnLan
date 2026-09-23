package de.florianisme.wakeonlan.shutdown.hostkey;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.io.BaseEncoding;

import net.schmizz.sshj.common.Buffer;

import org.junit.Test;

import java.security.PublicKey;

public class TrustOnFirstUseHostKeyVerifierTest {

    // Generated with ssh-keygen -t ed25519, fingerprints taken from ssh-keygen -lf
    private static final String KEY_ONE = "AAAAC3NzaC1lZDI1NTE5AAAAIOSseGc9LNYJ+LIkKfleq/9lmb9CgQKhbLMbG3aZb2Zw";
    private static final String KEY_ONE_FINGERPRINT = "SHA256:GCfMLyL/1SjrZZ6RyXHQthEEYLRpuAgG18vWGWwuSoU";
    private static final String KEY_TWO = "AAAAC3NzaC1lZDI1NTE5AAAAIJqBTFBTMTfZ5R6uxi0e0jLGsk7SjrTdhtSKZugFfOrz";
    private static final String KEY_TWO_FINGERPRINT = "SHA256:wgQxBDsbVaeBLo15j4ajqorfK6/dX85Ic484OORTXIE";

    @Test
    public void testFingerprint_matchesOpenSsh() throws Exception {
        assertEquals(KEY_ONE_FINGERPRINT, TrustOnFirstUseHostKeyVerifier.fingerprint(publicKey(KEY_ONE)));
        assertEquals(KEY_TWO_FINGERPRINT, TrustOnFirstUseHostKeyVerifier.fingerprint(publicKey(KEY_TWO)));
    }

    @Test
    public void testVerify_unknownHostIsTrusted() throws Exception {
        HostKeyStore store = mock(HostKeyStore.class);
        TrustOnFirstUseHostKeyVerifier verifier = new TrustOnFirstUseHostKeyVerifier(store);

        assertTrue(verifier.verify("host", 22, publicKey(KEY_ONE)));
        verify(store).trust("host", 22, KEY_ONE_FINGERPRINT);
        assertNull(verifier.getRejectedHostKey());
    }

    @Test
    public void testVerify_knownKeyIsAccepted() throws Exception {
        HostKeyStore store = mock(HostKeyStore.class);
        when(store.getFingerprint("host", 22)).thenReturn(KEY_ONE_FINGERPRINT);
        TrustOnFirstUseHostKeyVerifier verifier = new TrustOnFirstUseHostKeyVerifier(store);

        assertTrue(verifier.verify("host", 22, publicKey(KEY_ONE)));
        verify(store, never()).trust(anyString(), anyInt(), anyString());
    }

    @Test
    public void testVerify_changedKeyIsRejected() throws Exception {
        HostKeyStore store = mock(HostKeyStore.class);
        when(store.getFingerprint("host", 22)).thenReturn(KEY_ONE_FINGERPRINT);
        TrustOnFirstUseHostKeyVerifier verifier = new TrustOnFirstUseHostKeyVerifier(store);

        assertFalse(verifier.verify("host", 22, publicKey(KEY_TWO)));
        verify(store, never()).trust(anyString(), anyInt(), anyString());

        RejectedHostKey rejected = verifier.getRejectedHostKey();
        assertEquals("host", rejected.getHost());
        assertEquals(22, rejected.getPort());
        assertEquals(KEY_TWO_FINGERPRINT, rejected.getFingerprint());
    }

    private static PublicKey publicKey(String base64Blob) throws Exception {
        return new Buffer.PlainBuffer(BaseEncoding.base64().decode(base64Blob)).readPublicKey();
    }
}
