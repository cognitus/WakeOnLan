package de.florianisme.wakeonlan.persistence.crypto;

import androidx.annotation.Nullable;

/**
 * Encrypts secrets before they are written to the database.
 */
public interface SecretCipher {

    String ENCRYPTED_PREFIX = "enc1:";

    @Nullable
    String encrypt(@Nullable String plainText);

    /**
     * @return the plain text, the value itself if it was stored before encryption was introduced,
     * or {@code null} if it can not be decrypted anymore (e.g. after restoring a backup on another device)
     */
    @Nullable
    String decrypt(@Nullable String storedValue);

    static boolean isEncrypted(@Nullable String storedValue) {
        return storedValue != null && storedValue.startsWith(ENCRYPTED_PREFIX);
    }
}
