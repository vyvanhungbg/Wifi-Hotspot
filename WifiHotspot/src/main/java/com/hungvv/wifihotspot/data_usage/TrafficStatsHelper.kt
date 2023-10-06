package com.hungvv.wifihotspot.data_usage

import android.net.TrafficStats

/**
- Create by :Vy Hùng
- Create at :07,October,2023
 **/

object TrafficStatsHelper {

    fun getAllRxBytes(): Long {
        return TrafficStats.getTotalRxBytes()
    }

    fun getAllTxBytes(): Long {
        return TrafficStats.getTotalTxBytes()
    }

    fun getAllRxBytesMobile(): Long {
        return TrafficStats.getMobileRxBytes()
    }

    fun getAllTxBytesMobile(): Long {
        return TrafficStats.getMobileTxBytes()
    }

    fun getAllRxBytesWifi(): Long {
        return TrafficStats.getTotalRxBytes() - TrafficStats.getMobileRxBytes()
    }

    fun getAllTxBytesWifi(): Long {
        return TrafficStats.getTotalTxBytes() - TrafficStats.getMobileTxBytes()
    }

    fun getPackageRxBytes(uid: Int): Long {
        return TrafficStats.getUidRxBytes(uid)
    }

    fun getPackageTxBytes(uid: Int): Long {
        return TrafficStats.getUidTxBytes(uid)
    }

}