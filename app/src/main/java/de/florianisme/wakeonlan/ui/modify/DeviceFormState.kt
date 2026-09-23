package de.florianisme.wakeonlan.ui.modify

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.florianisme.wakeonlan.R
import de.florianisme.wakeonlan.persistence.models.Device
import de.florianisme.wakeonlan.wol.WakeCommandTemplate

/**
 * Holds the mutable state of the add/edit device form and derives validation errors,
 * replacing the TextWatcher + protected getter logic of the old ModifyDeviceActivity.
 */
class DeviceFormState(initial: Device? = null) {

    private val existing: Device? = initial

    var name by mutableStateOf(initial?.name.orEmpty())
    var statusIp by mutableStateOf(initial?.statusIp.orEmpty())
    var mac by mutableStateOf(initial?.macAddress.orEmpty())
    var broadcast by mutableStateOf(initial?.broadcastAddress.orEmpty())
    var port by mutableStateOf(initial?.let { it.port.toString() } ?: "9")
    var secureOn by mutableStateOf(initial?.secureOnPassword.orEmpty())

    var remoteShutdownEnabled by mutableStateOf(initial?.remoteShutdownEnabled ?: false)
    var sshAddress by mutableStateOf(initial?.sshAddress.orEmpty())
    var sshPort by mutableStateOf(portFallback(initial?.sshPort))
    var sshUsername by mutableStateOf(initial?.sshUsername.orEmpty())
    var sshPassword by mutableStateOf(initial?.sshPassword.orEmpty())
    var sshCommand by mutableStateOf(initial?.sshCommand.orEmpty())

    var wakeViaSsh by mutableStateOf(initial?.wakeViaSsh ?: false)
    var relayAddress by mutableStateOf(initial?.relaySshAddress.orEmpty())
    var relayPort by mutableStateOf(portFallback(initial?.relaySshPort))
    var relayUsername by mutableStateOf(initial?.relaySshUsername.orEmpty())
    var relayPassword by mutableStateOf(initial?.relaySshPassword.orEmpty())
    var relayCommand by mutableStateOf(relayCommandFallback(initial))

    /** Set to true after the first save attempt so errors are shown for untouched fields too. */
    var showErrors by mutableStateOf(false)

    private val statusIpFallbackMissing: Boolean
        get() = remoteShutdownEnabled && statusIp.trim().isEmpty()

    val nameError: Int?
        get() = DeviceFormValidation.validateNotEmpty(name, R.string.add_device_error_name_empty)
    val macError: Int?
        get() = DeviceFormValidation.validateMac(mac)
    val portError: Int?
        get() = DeviceFormValidation.validatePort(port.trim())
    val secureOnError: Int?
        get() = DeviceFormValidation.validateSecureOnPassword(secureOn)
    val sshAddressError: Int?
        get() = DeviceFormValidation.validateConditionalNotEmpty(
            sshAddress,
            statusIpFallbackMissing,
            R.string.add_device_error_ssh_address_empty
        )
    val sshUsernameError: Int?
        get() = DeviceFormValidation.validateConditionalNotEmpty(
            sshUsername,
            remoteShutdownEnabled,
            R.string.add_device_error_ssh_username_empty
        )
    val sshCommandError: Int?
        get() = DeviceFormValidation.validateConditionalNotEmpty(
            sshCommand,
            remoteShutdownEnabled,
            R.string.add_device_error_ssh_command_empty
        )

    val relayAddressError: Int?
        get() = DeviceFormValidation.validateConditionalNotEmpty(
            relayAddress,
            wakeViaSsh,
            R.string.add_device_error_ssh_address_empty
        )
    val relayPortError: Int?
        get() = if (wakeViaSsh) DeviceFormValidation.validatePort(relayPort.trim()) else null
    val relayUsernameError: Int?
        get() = DeviceFormValidation.validateConditionalNotEmpty(
            relayUsername,
            wakeViaSsh,
            R.string.add_device_error_ssh_username_empty
        )
    val relayCommandError: Int?
        get() = DeviceFormValidation.validateConditionalNotEmpty(
            relayCommand,
            wakeViaSsh,
            R.string.add_device_error_ssh_command_empty
        )

    val isValid: Boolean
        get() = nameError == null && macError == null && portError == null && secureOnError == null &&
                sshAddressError == null && sshUsernameError == null && sshCommandError == null &&
                relayAddressError == null && relayPortError == null && relayUsernameError == null && relayCommandError == null

    fun getPort(): Int = port.trim().toIntOrNull() ?: 9

    private fun getSshPort(): Int = sshPort.trim().toIntOrNull() ?: -1

    private fun getRelayPort(): Int = relayPort.trim().toIntOrNull() ?: -1

    fun toDevice(): Device {
        val device = existing ?: Device()
        device.name = name.trim()
        device.statusIp = statusIp.trim()
        device.macAddress = mac.trim()
        device.broadcastAddress = broadcast.trim()
        device.port = getPort()
        device.secureOnPassword = secureOn.trim()
        device.remoteShutdownEnabled = remoteShutdownEnabled
        device.sshAddress = sshAddress.trim()
        device.sshPort = getSshPort()
        device.sshUsername = sshUsername.trim()
        device.sshPassword = sshPassword.trim()
        device.sshCommand = sshCommand.trim()
        device.wakeViaSsh = wakeViaSsh
        device.relaySshAddress = relayAddress.trim()
        device.relaySshPort = getRelayPort()
        device.relaySshUsername = relayUsername.trim()
        device.relaySshPassword = relayPassword.trim()
        device.relaySshCommand = relayCommand.trim()
        return device
    }

    fun isUnchanged(): Boolean {
        val device = existing ?: return isEmptyForm()
        return device.name.orEmpty() == name.trim() &&
                device.broadcastAddress.orEmpty() == broadcast.trim() &&
                device.macAddress.orEmpty() == mac.trim() &&
                device.statusIp.orEmpty() == statusIp.trim() &&
                device.port == getPort() &&
                device.secureOnPassword.orEmpty() == secureOn.trim() &&
                device.remoteShutdownEnabled == remoteShutdownEnabled &&
                device.sshAddress.orEmpty() == sshAddress.trim() &&
                (device.sshPort ?: -1) == getSshPort() &&
                device.sshUsername.orEmpty() == sshUsername.trim() &&
                device.sshPassword.orEmpty() == sshPassword.trim() &&
                device.sshCommand.orEmpty() == sshCommand.trim() &&
                device.wakeViaSsh == wakeViaSsh &&
                device.relaySshAddress.orEmpty() == relayAddress.trim() &&
                (device.relaySshPort ?: -1) == getRelayPort() &&
                device.relaySshUsername.orEmpty() == relayUsername.trim() &&
                device.relaySshPassword.orEmpty() == relayPassword.trim() &&
                relayCommandFallback(device) == relayCommand.trim()
    }

    private fun isEmptyForm(): Boolean =
        name.trim().isEmpty() && mac.trim().isEmpty() && getPort() == 9 &&
                broadcast.trim().isEmpty() && statusIp.trim().isEmpty() &&
                secureOn.trim().isEmpty() && !remoteShutdownEnabled &&
                sshAddress.trim().isEmpty() && getSshPort() == -1 && sshUsername.trim().isEmpty() &&
                sshPassword.trim().isEmpty() && sshCommand.trim().isEmpty() &&
                !wakeViaSsh && relayAddress.trim().isEmpty() && getRelayPort() == -1 &&
                relayUsername.trim().isEmpty() && relayPassword.trim().isEmpty() &&
                relayCommand.trim() == WakeCommandTemplate.DEFAULT_TEMPLATE

    companion object {
        private fun portFallback(port: Int?): String {
            if (port == null) return ""
            return if (port < 0) "" else port.toString()
        }

        /** Pre-fills the command template so the user only has to adjust it to their relay. */
        private fun relayCommandFallback(device: Device?): String =
            device?.relaySshCommand.orEmpty().ifEmpty { WakeCommandTemplate.DEFAULT_TEMPLATE }
    }
}


