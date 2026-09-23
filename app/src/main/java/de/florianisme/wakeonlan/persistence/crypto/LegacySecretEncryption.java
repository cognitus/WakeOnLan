package de.florianisme.wakeonlan.persistence.crypto;

import android.util.Log;

import com.google.common.base.Strings;

import de.florianisme.wakeonlan.persistence.DeviceDao;
import de.florianisme.wakeonlan.persistence.entities.DeviceEntity;

/**
 * Encrypts SSH passwords that were stored in plain text by versions up to 2.0.4.
 */
public class LegacySecretEncryption {

    private LegacySecretEncryption() {
    }

    public static void encryptPlainTextPasswords(DeviceDao deviceDao, SecretCipher secretCipher) {
        for (DeviceEntity deviceEntity : deviceDao.getAll()) {
            if (Strings.isNullOrEmpty(deviceEntity.sshPassword) || SecretCipher.isEncrypted(deviceEntity.sshPassword)) {
                continue;
            }

            try {
                deviceEntity.sshPassword = secretCipher.encrypt(deviceEntity.sshPassword);
                deviceDao.update(deviceEntity);
            } catch (IllegalStateException e) {
                // Keep the existing value, the migration is retried on the next start
                Log.e(LegacySecretEncryption.class.getSimpleName(), "Could not encrypt password of device " + deviceEntity.id, e);
            }
        }
    }
}
