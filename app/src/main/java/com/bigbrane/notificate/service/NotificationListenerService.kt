package com.bigbrane.notificate.service

import android.content.pm.PackageManager
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

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val notification = sbn.notification
        val extras = notification.extras
        
        val appName = try {
            packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(sbn.packageName, PackageManager.GET_META_DATA)
            ).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            sbn.packageName
        }

        val notificationItem = NotificationItem(
            id = sbn.id,
            appName = appName,
            title = extras.getString("android.title", ""),
            text = extras.getString("android.text", ""),
            postTime = sbn.postTime,
            packageName = sbn.packageName
        )

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
