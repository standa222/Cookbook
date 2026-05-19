package cz.cvut.fel.zan.cookbook.notifications

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import cz.cvut.fel.zan.cookbook.R

object NotificationHelper {

    /** Returns true if the app is allowed to post notifications. */
    fun canPostNotifications(context: Context): Boolean {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) {
            return true
        }
        return checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    /** Step 2.4 — the simplest possible notification. */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showBasicNotification(context: Context) {
        if (!canPostNotifications(context)) return

        val notification = NotificationCompat.Builder(context, ChannelIds.COOKBOOK_MAIN)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Cookbook reminder")
            .setContentText("Don't forget — time to check the recipe!")
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NotificationIds.BASIC, notification)
    }
}

object NotificationIds {
    const val BASIC = 1
    const val BIG_TEXT = 2
    const val BIG_PICTURE = 3
    const val INBOX = 4
    const val WITH_ACTIONS = 5
    const val LOCK_SCREEN_PUBLIC = 6
    const val LOCK_SCREEN_PRIVATE = 7
    const val PROGRESS = 8
    const val GROUP_CHILD_BASE = 1000  // children get GROUP_CHILD_BASE + index
    const val GROUP_SUMMARY = 2000
    const val SCHEDULED_BY_WORK = 100
    const val SCHEDULED_BY_ALARM = 101
}