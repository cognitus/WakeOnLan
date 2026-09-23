package de.florianisme.wakeonlan.shutdown.hostkey;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

/**
 * Remembers the SSH host key fingerprint of every host a shutdown command was sent to.
 */
public class HostKeyStore {

    private static final String PREFERENCES_NAME = "ssh_known_hosts";

    private final SharedPreferences preferences;

    public HostKeyStore(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    @Nullable
    public String getFingerprint(String host, int port) {
        return preferences.getString(buildKey(host, port), null);
    }

    public void trust(String host, int port, String fingerprint) {
        preferences.edit().putString(buildKey(host, port), fingerprint).apply();
    }

    private static String buildKey(String host, int port) {
        return "[" + host + "]:" + port;
    }
}
