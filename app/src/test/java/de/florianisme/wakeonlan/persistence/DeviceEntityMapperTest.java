package de.florianisme.wakeonlan.persistence;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;

import java.util.Arrays;

import de.florianisme.wakeonlan.persistence.crypto.LegacySecretEncryption;
import de.florianisme.wakeonlan.persistence.crypto.SecretCipher;
import de.florianisme.wakeonlan.persistence.entities.DeviceEntity;
import de.florianisme.wakeonlan.persistence.mapper.DeviceEntityMapper;
import de.florianisme.wakeonlan.persistence.models.Device;

public class DeviceEntityMapperTest {

    /**
     * Reversible stand-in for the Keystore cipher, which is not available in JVM tests.
     */
    private static final SecretCipher FAKE_CIPHER = new SecretCipher() {
        @Override
        public String encrypt(String plainText) {
            return plainText == null || plainText.isEmpty() ? plainText : ENCRYPTED_PREFIX + new StringBuilder(plainText).reverse();
        }

        @Override
        public String decrypt(String storedValue) {
            if (!SecretCipher.isEncrypted(storedValue)) {
                return storedValue;
            }
            return new StringBuilder(storedValue.substring(ENCRYPTED_PREFIX.length())).reverse().toString();
        }
    };

    private final DeviceEntityMapper mapper = new DeviceEntityMapper(FAKE_CIPHER);

    @Test
    public void testSshPasswordIsEncryptedInEntityAndDecryptedInModel() {
        Device device = new Device(1, "PC", "aa:bb:cc:01:02:03", "192.168.0.255", 9, null, null,
                true, "192.168.0.10", 22, "user", "secret", "sudo poweroff");

        DeviceEntity entity = mapper.modelToEntity(device);
        assertEquals("enc1:terces", entity.sshPassword);
        assertEquals("secret", mapper.entityToModel(entity).sshPassword);
    }

    @Test
    public void testLegacyPlainTextPasswordIsReadAsIs() {
        DeviceEntity entity = new DeviceEntity();
        entity.sshPassword = "secret";

        assertEquals("secret", mapper.entityToModel(entity).sshPassword);
    }

    @Test
    public void testLegacySecretEncryption_encryptsOnlyPlainTextPasswords() {
        DeviceEntity plain = entityWithPassword(1, "secret");
        DeviceEntity encrypted = entityWithPassword(2, "enc1:terces");
        DeviceEntity empty = entityWithPassword(3, null);

        DeviceDao dao = mock(DeviceDao.class);
        when(dao.getAll()).thenReturn(Arrays.asList(plain, encrypted, empty));

        LegacySecretEncryption.encryptPlainTextPasswords(dao, FAKE_CIPHER);

        assertEquals("enc1:terces", plain.sshPassword);
        verify(dao).update(plain);
        verify(dao, never()).update(encrypted);
        verify(dao, never()).update(empty);
    }

    private static DeviceEntity entityWithPassword(int id, String password) {
        DeviceEntity entity = new DeviceEntity();
        entity.id = id;
        entity.sshPassword = password;
        return entity;
    }
}
