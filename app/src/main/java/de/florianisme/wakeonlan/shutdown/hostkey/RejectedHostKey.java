package de.florianisme.wakeonlan.shutdown.hostkey;

/**
 * A host key that did not match the one previously trusted for its host.
 */
public class RejectedHostKey {

    private final String host;
    private final int port;
    private final String fingerprint;

    public RejectedHostKey(String host, int port, String fingerprint) {
        this.host = host;
        this.port = port;
        this.fingerprint = fingerprint;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getFingerprint() {
        return fingerprint;
    }
}
