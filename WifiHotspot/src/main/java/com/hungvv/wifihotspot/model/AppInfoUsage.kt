package com.hungvv.wifihotspot.model

import android.graphics.drawable.Drawable
import com.hungvv.wifihotspot.data_usage.DataUsageHelper

/**
- Create by :Vy Hùng
- Create at :07,October,2023
 **/

data class AppInfoUsage(
    val uid: Int,
    val name: String,
    val icon: Drawable?,
    val rxBytes: Long,
    val txBytes: Long,
    val type: AppInfoType,
    val state: AppInfoState


) {
    override fun toString(): String {
        return "AppInfoUsage(uid=$uid, name=$name, icon=$icon, rxBytes=${
            DataUsageHelper.humanReadableByteCountBin(
                rxBytes
            )
        }, txBytes=${
            DataUsageHelper.humanReadableByteCountBin(
                txBytes
            )
        }, type=$type, state=$state)"
    }
}