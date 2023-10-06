package com.hungvv.wifihotspot.data_usage

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import java.text.CharacterIterator
import java.text.StringCharacterIterator


/**
- Create by :Vy Hùng
- Create at :07,October,2023
 **/

object DataUsageHelper {
     fun humanReadableByteCountSI(bytesData: Long): String {
           var bytes = bytesData
           if (-1000 < bytes && bytes < 1000) {
               return "$bytes B"
           }
           val ci: CharacterIterator = StringCharacterIterator("kMGTPE")
           while (bytes <= -999_950 || bytes >= 999_950) {
               bytes /= 1000
               ci.next()
           }
           return String.format("%.1f %cB", bytes / 1000.0, ci.current())
       }

    fun humanReadableByteCountBin(bytes: Long): String {
        if(bytes == 0L)
            return "0MB"
        val absB = if (bytes == Long.MIN_VALUE) Long.MAX_VALUE else Math.abs(bytes)
        if (absB < 1024) {
            return "${bytes}B"
        }
        var value = absB
        val ci: CharacterIterator = StringCharacterIterator("KMGTPE")
        var i = 40
        while (i >= 0 && absB > 0xfffccccccccccccL shr i) {
            value = value shr 10
            ci.next()
            i -= 10
        }
        value *= java.lang.Long.signum(bytes).toLong()
        return String.format("%.1f%cB", value / 1024.0, ci.current())
    }


    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun openScreenRequestPermissions(): Intent {
        return Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
    }

    fun mBToBytes(mb: Long) = mb * 1024L * 1024L
}