package de.florianisme.wakeonlan.ui.modify

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.florianisme.wakeonlan.R
import de.florianisme.wakeonlan.shutdown.hostkey.HostKeyStore
import de.florianisme.wakeonlan.shutdown.hostkey.RejectedHostKey
import de.florianisme.wakeonlan.ssh.SshCommandListener
import de.florianisme.wakeonlan.ssh.SshCommandModel
import de.florianisme.wakeonlan.ssh.SshErrorMessages

private val SuccessGreen = Color(0xFF479C44)

/**
 * Runs an SSH command and shows the progress of each step.
 *
 * @param runCommand starts the command, reporting to the given listener
 */
@Composable
fun SshCommandTestDialog(
    titleRes: Int,
    executingCommandRes: Int,
    runCommand: (Context, SshCommandListener) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current

    var destinationReached by remember { mutableStateOf(false) }
    var authorized by remember { mutableStateOf(false) }
    var sessionCreated by remember { mutableStateOf(false) }
    var commandExecuted by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var rejectedHostKey by remember { mutableStateOf<RejectedHostKey?>(null) }
    var attempt by remember { mutableIntStateOf(0) }

    androidx.compose.runtime.LaunchedEffect(attempt) {
        destinationReached = false
        authorized = false
        sessionCreated = false
        commandExecuted = false
        errorMessage = null
        rejectedHostKey = null

        val listener = object : SshCommandListener {
            override fun onTargetHostReached() {
                destinationReached = true
            }

            override fun onLoginSuccessful() {
                authorized = true
            }

            override fun onSessionStartSuccessful() {
                sessionCreated = true
            }

            override fun onCommandExecuteSuccessful() {
                commandExecuted = true
            }

            override fun onSudoPromptTriggered(commandModel: SshCommandModel) {
                errorMessage = SshErrorMessages.sudoPrompt(context, commandModel)
            }

            override fun onHostKeyChanged(hostKey: RejectedHostKey) {
                errorMessage = SshErrorMessages.hostKeyChanged(context, hostKey)
                rejectedHostKey = hostKey
            }

            override fun onGeneralError(exception: Exception, commandModel: SshCommandModel?) {
                errorMessage = SshErrorMessages.forException(context, exception, commandModel)
            }
        }
        runCommand(context, listener)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(titleRes)) },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.ok)) }
        },
        dismissButton = {
            rejectedHostKey?.let { hostKey ->
                TextButton(onClick = {
                    HostKeyStore(context).trust(hostKey.host, hostKey.port, hostKey.fingerprint)
                    attempt++
                }) { Text(stringResource(R.string.test_shutdown_trust_new_host_key)) }
            }
        },
        text = {
            Column {
                StepRow(
                    completed = destinationReached,
                    textRes = if (destinationReached) R.string.test_shutdown_successful_destination else R.string.test_shutdown_initial_destination,
                )
                StepRow(
                    completed = authorized,
                    textRes = if (authorized) R.string.test_shutdown_successful_authorization else R.string.test_shutdown_initial_authorization,
                )
                StepRow(
                    completed = sessionCreated,
                    textRes = if (sessionCreated) R.string.test_shutdown_successful_session else R.string.test_shutdown_initial_session,
                )
                StepRow(
                    completed = commandExecuted,
                    textRes = if (commandExecuted) R.string.test_shutdown_successful_command_execute else executingCommandRes,
                )
                errorMessage?.let { message ->
                    Spacer(modifier = Modifier.padding(top = 8.dp))
                    Text(text = message, color = MaterialTheme.colorScheme.error)
                }
            }
        },
    )
}

@Composable
private fun StepRow(completed: Boolean, textRes: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp),
    ) {
        if (completed) {
            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = SuccessGreen)
        } else {
            Icon(
                Icons.Outlined.RadioButtonUnchecked,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(stringResource(textRes))
    }
}
