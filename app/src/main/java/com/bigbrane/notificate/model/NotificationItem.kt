package com.bigbrane.notificate.model

data class NotificationItem(
    val id: Int,
    val appName: String,
    val title: String,
    val text: String,
    val postTime: Long,
    val packageName: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NotificationItem) return false
        return title == other.title && 
               text == other.text && 
               packageName == other.packageName
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + text.hashCode()
        result = 31 * result + packageName.hashCode()
        return result
    }
}
