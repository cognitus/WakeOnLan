package de.florianisme.wakeonlan.ui.backup.model;

import com.google.gson.annotations.SerializedName;

import de.florianisme.wakeonlan.persistence.models.Device;

public class DeviceBackupModel {

    /*
    Aliases for each field have to be kept for some time because backups exported prior to
    version 1.6.5 were created using obfuscated classes. This means that each field was written
    to JSON with its obfuscated name ("a", "b", and so on).
     */

    @SerializedName(value = "id", alternate = "a")
    public int id;

    @SerializedName(value = "name", alternate = "b")
    public String name;

    @SerializedName(value = "mac_address", alternate = "c")
    public String macAddress;

    @SerializedName(value = "broadcast_address", alternate = "d")
    public String broadcastAddress;

    @SerializedName(value = "port", alternate = "e")
    public int port;

    @SerializedName(value = "status_ip", alternate = "f")
    public String statusIp;

    @SerializedName(value = "secure_on_password", alternate = "g")
    public String secureOnPassword;

    @SerializedName(value = "remote_shutdown_enabled")
    public boolean remoteShutdownEnabled;

    @SerializedName(value = "ssh_address")
    public String sshAddress;

    @SerializedName(value = "ssh_port")
    public Integer sshPort;

    @SerializedName(value = "ssh_username")
    public String sshUsername;

    @SerializedName(value = "ssh_password")
    public String sshPassword;

    @SerializedName(value = "ssh_command")
    public String sshCommand;

    @SerializedName(value = "wake_via_ssh")
    public boolean wakeViaSsh;

    @SerializedName(value = "relay_ssh_address")
    public String relaySshAddress;

    @SerializedName(value = "relay_ssh_port")
    public Integer relaySshPort;

    @SerializedName(value = "relay_ssh_username")
    public String relaySshUsername;

    @SerializedName(value = "relay_ssh_password")
    public String relaySshPassword;

    @SerializedName(value = "relay_ssh_command")
    public String relaySshCommand;

    public DeviceBackupModel(Device device) {
        this.id = device.id;
        this.name = device.name;
        this.macAddress = device.macAddress;
        this.broadcastAddress = device.broadcastAddress;
        this.port = device.port;
        this.statusIp = device.statusIp;
        this.secureOnPassword = device.secureOnPassword;
        this.remoteShutdownEnabled = device.remoteShutdownEnabled;
        this.sshAddress = device.sshAddress;
        this.sshPort = device.sshPort;
        this.sshUsername = device.sshUsername;
        this.sshPassword = device.sshPassword;
        this.sshCommand = device.sshCommand;
        this.wakeViaSsh = device.wakeViaSsh;
        this.relaySshAddress = device.relaySshAddress;
        this.relaySshPort = device.relaySshPort;
        this.relaySshUsername = device.relaySshUsername;
        this.relaySshPassword = device.relaySshPassword;
        this.relaySshCommand = device.relaySshCommand;
    }

    public Device toModel() {
        return new Device(this.id, this.name, this.macAddress, this.broadcastAddress, this.port, this.statusIp, this.secureOnPassword,
                this.remoteShutdownEnabled, this.sshAddress, this.sshPort, this.sshUsername, this.sshPassword, this.sshCommand,
                this.wakeViaSsh, this.relaySshAddress, this.relaySshPort, this.relaySshUsername, this.relaySshPassword, this.relaySshCommand);
    }

    @SuppressWarnings("unused")
    public DeviceBackupModel() {
    }

}
