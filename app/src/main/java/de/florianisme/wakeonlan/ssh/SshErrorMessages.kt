package de.florianisme.wakeonlan.ssh

import android.content.Context
import com.google.common.base.Throwables
import de.florianisme.wakeonlan.R
import de.florianisme.wakeonlan.shutdown.exception.CommandExecuteException
import de.florianisme.wakeonlan.shutdown.hostkey.RejectedHostKey
import net.schmizz.sshj.connection.ConnectionException
import net.schmizz.sshj.userauth.UserAuthException
import java.net.ConnectException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException

/** User-facing texts for the outcomes of [SshCommandRunnable], shared by the test dialog and the notifications. */
object SshErrorMessages {

    @JvmStatic
    fun sudoPrompt(context: Context, commandModel: SshCommandModel): String =
        context.getString(R.string.test_shutdown_error_execution_sudo_prompt, commandModel.command)

    @JvmStatic
    fun hostKeyChanged(context: Context, hostKey: RejectedHostKey): String =
        context.getString(R.string.test_shutdown_error_host_key_changed, hostKey.host, hostKey.fingerprint)

    @JvmStatic
    fun forException(context: Context, exception: Exception, commandModel: SshCommandModel?): String {
        return when {
            exception is ConnectException && commandModel != null ->
                context.getString(
                    R.string.test_shutdown_error_connect_exception,
                    commandModel.sshAddress,
                    commandModel.sshPort
                )

            exception is UnknownHostException && commandModel != null ->
                context.getString(R.string.test_shutdown_error_unknown_host, commandModel.sshAddress)

            exception is UserAuthException && commandModel != null ->
                context.getString(
                    R.string.test_shutdown_error_auth_exception,
                    commandModel.username,
                    commandModel.sshAddress
                )

            exception is ConnectionException && Throwables.getRootCause(exception) is TimeoutException && commandModel != null ->
                context.getString(R.string.test_shutdown_error_execution_timeout, commandModel.command)

            exception is CommandExecuteException && commandModel != null -> {
                val exitStatus = exception.exitStatus
                context.getString(
                    R.string.test_shutdown_error_execution_exception,
                    commandModel.command,
                    exitStatus,
                    exitCodeExplanation(context, exitStatus),
                )
            }

            else -> context.getString(R.string.test_shutdown_error_unknown_exception, exception.message)
        }
    }

    private fun exitCodeExplanation(context: Context, exitStatus: Int): String =
        when (exitStatus) {
            127 -> context.getString(R.string.execution_error_command_not_found)
            126 -> context.getString(R.string.execution_error_command_not_executable)
            else -> context.getString(R.string.execution_error_unknown)
        }
}
