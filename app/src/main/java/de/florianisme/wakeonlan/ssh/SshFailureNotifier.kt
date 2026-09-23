package de.florianisme.wakeonlan.ssh

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import de.florianisme.wakeonlan.R
import de.florianisme.wakeonlan.persistence.models.Device
import de.florianisme.wakeonlan.shutdown.hostkey.RejectedHostKey

/**
 * Reports failures of an SSH command that runs without a UI waiting for it (wake via relay, shutdown).
 * Posts a notification, or a toast if the user has not allowed notifications.
 */
class SshFailureNotifier(
    context: Context,
    private val device: Device,
    @StringRes private val titleRes: Int,
) : SshCommandListener {

    private val context: Context = context.applicationContext

    override fun onTargetHostReached() = Unit

    override fun onLoginSuccessful() = Unit

    override fun onSessionStartSuccessful() = Unit

    override fun onCommandExecuteSuccessful() = Unit

    override fun onSudoPromptTriggered(commandModel: SshCommandModel) =
        notifyFailure(SshErrorMessages.sudoPrompt(context, commandModel))

    override fun onHostKeyChanged(rejectedHostKey: RejectedHostKey) =
        notifyFailure(SshErrorMessages.hostKeyChanged(context, rejectedHostKey))

    override fun onGeneralError(exception: Exception, commandModel: SshCommandModel?) =
        notifyFailure(SshErrorMessages.forException(context, exception, commandModel))

    private fun notifyFailure(message: String) {
        val title = context.getString(titleRes, device.name)

        if (!canPostNotifications()) {
            showToast(title, message)
            return
        }

        createChannel()
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_tile_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(openAppIntent())
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(context.getString(titleRes), device.id, notification)
        } catch (e: SecurityException) {
            // The permission was revoked after the check
            showToast(title, message)
        }
    }

    private fun showToast(title: String, message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, "$title\n$message", Toast.LENGTH_LONG).show()
        }
    }

    private fun canPostNotifications(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.ssh_failure_notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        )
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    // Opens the app rather than the edit screen: a Device extra would hand the decrypted SSH passwords to the system
    private fun openAppIntent(): PendingIntent? {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName) ?: return null
        return PendingIntent.getActivity(context, 0, launchIntent, PendingIntent.FLAG_IMMUTABLE)
    }

    private companion object {
        const val CHANNEL_ID = "ssh_failures"
    }
}
