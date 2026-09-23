package de.florianisme.wakeonlan.persistence.mapper;

import de.florianisme.wakeonlan.persistence.crypto.SecretCipher;
import de.florianisme.wakeonlan.persistence.entities.DeviceEntity;
import de.florianisme.wakeonlan.persistence.models.Device;

public class DeviceEntityMapper implements EntityMapper<Device, DeviceEntity> {

    private final SecretCipher secretCipher;

    public DeviceEntityMapper(SecretCipher secretCipher) {
        this.secretCipher = secretCipher;
    }

    @Override
    public Device entityToModel(DeviceEntity entity) {
        if (entity == null) {
            return new Device();
        }
        return new Device(entity.id, entity.name, entity.macAddress, entity.broadcastAddress, entity.port, entity.statusIp, entity.secureOnPassword,
                entity.enableRemoteShutdown, entity.sshAddress, entity.sshPort, entity.sshUsername, secretCipher.decrypt(entity.sshPassword), entity.sshCommand);
    }

    @Override
    public DeviceEntity modelToEntity(Device model) {
        if (model == null) {
            return new DeviceEntity();
        }
        return new DeviceEntity(model.id, model.name, model.macAddress, model.broadcastAddress, model.port, model.statusIp, model.secureOnPassword,
                model.remoteShutdownEnabled, model.sshAddress, model.sshPort, model.sshUsername, secretCipher.encrypt(model.sshPassword), model.sshCommand);
    }
}
