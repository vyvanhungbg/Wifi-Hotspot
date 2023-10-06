package com.hungvv.wifihotspot.data_usage

import android.content.Context
import android.content.pm.PackageManager

/**
- Create by :Vy Hùng
- Create at :07,October,2023
 **/

object PackageManagerHelper {

    fun isPackage(context: Context, s: CharSequence): Boolean {
        val packageManager = context.packageManager
        try {
            packageManager.getPackageInfo(s.toString(), PackageManager.GET_META_DATA);
        } catch (e: PackageManager.NameNotFoundException) {
            return false
        }
        return true
    }

    fun getPackageUid(context: Context, packageName: String): Int? {
        val packageManager = context.packageManager
        return try {
            val packageInfo =
                packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA)

            packageInfo.applicationInfo.uid

        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }
}