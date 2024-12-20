package com.bigbrane.notificate.service

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.bigbrane.notificate.model.NotificationItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class NotificationService : NotificationListenerService() {
    
    companion object {
        private val _notifications = MutableStateFlow<Set<NotificationItem>>(emptySet())
        val notifications: StateFlow<Set<NotificationItem>> = _notifications.asStateFlow()
    }

    override fun onCreate() {
        super.onCreate()
        loadExistingNotifications()
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        loadExistingNotifications()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        // Clear notifications when service is disconnected
        _notifications.update { emptySet() }
    }

    private fun loadExistingNotifications() {
        try {
            val activeNotifications = getActiveNotifications() ?: return
            val notificationItems = activeNotifications.map { sbn ->
                createNotificationItem(sbn)
            }.toSet()
            
            _notifications.update { notificationItems }
        } catch (e: Exception) {
            // Handle any potential exceptions during loading
            _notifications.update { emptySet() }
        }
    }

    private fun createNotificationItem(sbn: StatusBarNotification): NotificationItem {
        val notification = sbn.notification
        val extras = notification.extras
        
        val appName = try {
            packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(sbn.packageName, PackageManager.GET_META_DATA)
            ).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            sbn.packageName
        }
        
        val appIcon = try {
            packageManager.getApplicationIcon(sbn.packageName) as Bitmap
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }

        return NotificationItem(
            id = sbn.id,
            appName = appName,
            appIcon = appIcon,
            title = extras.getString("android.title", ""),
            text = extras.getString("android.text", ""),
            postTime = sbn.postTime,
            packageName = sbn.packageName
        )
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val notificationItem = createNotificationItem(sbn)
        _notifications.update { currentNotifications ->
            currentNotifications + notificationItem
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        _notifications.update { currentNotifications ->
            currentNotifications.filterNot { it.id == sbn.id }.toSet()
        }
    }
}
