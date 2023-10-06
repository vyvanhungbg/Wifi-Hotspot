package com.hungvv.wifihotspot.model

import android.app.usage.NetworkStats

/**
- Create by :Vy Hùng
- Create at :07,October,2023
 **/

enum class AppInfoState {
    ALL, DEFAULT, FOREGROUND;

    companion object {
        fun getState(state: Int) = when (state) {
            NetworkStats.Bucket.STATE_ALL -> ALL
            NetworkStats.Bucket.STATE_DEFAULT -> DEFAULT
            NetworkStats.Bucket.STATE_FOREGROUND -> FOREGROUND
            else -> {
                DEFAULT
            }
        }
    }
}