package com.hungvv.wifihotspot.model

import android.app.usage.NetworkStats

/**
- Create by :Vy Hùng
- Create at :07,October,2023
 **/

enum class AppInfoType(val title: String) {
    ALL("System"),
    REMOVE("Deleted app"), NORMAL("Application"), TETHERING("Tethering");

    companion object {
        fun getType(uid: Int) = when (uid) {
            NetworkStats.Bucket.UID_REMOVED -> REMOVE
            NetworkStats.Bucket.UID_ALL -> ALL
            NetworkStats.Bucket.UID_TETHERING -> TETHERING
            else -> {
                NORMAL
            }
        }
    }
}