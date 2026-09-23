package de.florianisme.wakeonlan.persistence.crypto;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.common.base.Strings;
import com.google.common.io.BaseEncoding;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * AES-GCM with a key that never leaves the Android Keystore and is not part of any backup.
 * Stored format: {@code enc1:<base64 iv>:<base64 cipher text>}
 */
public class KeystoreSecretCipher implements SecretCipher {

    private static final String KEYSTORE = "AndroidKeyStore";
    private static final String KEY_ALIAS = "device_secrets";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BITS = 128;
    private static final BaseEncoding BASE64 = BaseEncoding.base64();

    @Nullable
    @Override
    public String encrypt(@Nullable String plainText) {
        if (Strings.isNullOrEmpty(plainText)) {
            return plainText;
        }

        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey());
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return ENCRYPTED_PREFIX + BASE64.encode(cipher.getIV()) + ":" + BASE64.encode(cipherText);
        } catch (GeneralSecurityException | IOException e) {
            // Never fall back to storing the secret in plain text
            throw new IllegalStateException("Could not encrypt secret", e);
        }
    }

    @Nullable
    @Override
    public String decrypt(@Nullable String storedValue) {
        if (!SecretCipher.isEncrypted(storedValue)) {
            return storedValue;
        }

        try {
            String[] parts = storedValue.substring(ENCRYPTED_PREFIX.length()).split(":", 2);
            byte[] iv = BASE64.decode(parts[0]);
            byte[] cipherText = BASE64.decode(parts[1]);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IOException | IllegalArgumentException | IndexOutOfBoundsException e) {
            Log.w(getClass().getSimpleName(), "Could not decrypt secret, it has to be entered again", e);
            return null;
        }
    }

    private static SecretKey getOrCreateKey() throws GeneralSecurityException, IOException {
        KeyStore keyStore = KeyStore.getInstance(KEYSTORE);
        keyStore.load(null);

        KeyStore.Entry entry = keyStore.getEntry(KEY_ALIAS, null);
        if (entry instanceof KeyStore.SecretKeyEntry) {
            return ((KeyStore.SecretKeyEntry) entry).getSecretKey();
        }

        KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE);
        keyGenerator.init(new KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build());
        return keyGenerator.generateKey();
    }
}
