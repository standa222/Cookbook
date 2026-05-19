package cz.cvut.fel.zan.cookbook

import android.R.attr.description
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import cz.cvut.fel.zan.cookbook.di.appModule
import cz.cvut.fel.zan.cookbook.di.viewModelModule
import cz.cvut.fel.zan.cookbook.notifications.ChannelIds
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class CookbookApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.INFO)
            androidContext(this@CookbookApp)
            modules(appModule, viewModelModule)
        }
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val main = NotificationChannel(
            ChannelIds.COOKBOOK_MAIN,
            "Cookbook main channel",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Main app notifications"
        }

        val reminders = NotificationChannel(
            ChannelIds.COOKBOOK_REMINDERS,
            "Cookbook reminders",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Recipe timers"
        }

        val updates = NotificationChannel(
            ChannelIds.COOKBOOK_UPDATES,
            "App updates",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Non-urgent app updates and tips"
        }

        manager.createNotificationChannels(listOf(main, reminders, updates))
    }
}