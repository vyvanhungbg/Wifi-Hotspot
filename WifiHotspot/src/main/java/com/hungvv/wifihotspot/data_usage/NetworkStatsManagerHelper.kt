package com.hungvv.wifihotspot.data_usage

import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.telephony.TelephonyManager
import androidx.annotation.RequiresPermission
import com.hungvv.wifihotspot.model.AppInfoState
import com.hungvv.wifihotspot.model.AppInfoType
import com.hungvv.wifihotspot.model.AppInfoUsage
import java.util.Calendar


/**
- Create by :Vy Hùng
- Create at :07,October,2023
 **/

private const val TAG = "NetworkStatsManagerHelp"

object NetworkStatsManagerHelper {


   // Missing permissions required by TelephonyManager.getSubscriberId: android.permission.READ_PRIVILEGED_PHONE_STAT
    // But cannot request permissions
    //https://source.android.com/docs/core/connect/device-identifiers
    private fun getSubscriberId(
        networkType: Int = ConnectivityManager.TYPE_MOBILE,
        context: Context
    ): String? {
        if (networkType != ConnectivityManager.TYPE_MOBILE) {
            return null
        }
        return try {
            val telephonyManager =
                context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager?
            telephonyManager?.subscriberId
        } catch (e: Exception) {
            // security exception
            // Log.e(TAG, "getSubscriberId: ")
            null
        }
    }

    @RequiresPermission(android.Manifest.permission.PACKAGE_USAGE_STATS)
    fun getNetworkUsageForDay(
        context: Context,
        startTime: Long,
        endTime: Long,
        networkType: Int = NetworkCapabilities.TRANSPORT_CELLULAR,
        onSuccess: (List<AppInfoUsage>, totalUsage: Long) -> Unit,
        onFinish:(List<AppInfoUsage>, totalUsage: Long) -> Unit,
        onFailure: (String) -> Unit
    ) {

//        val appInfoUsageList = mutableListOf<AppInfoUsage>()
        val mapInfoUsage = mutableMapOf<Int, AppInfoUsage>()

        val subscriberId = getSubscriberId(networkType, context)

        try {
            val networkStatsManager =
                context.getSystemService(Context.NETWORK_STATS_SERVICE) as? NetworkStatsManager?
            val networkStats = networkStatsManager?.querySummary(
                networkType,
                subscriberId,
                startTime,
                endTime
            )

            var totalUsage = 0L

            networkStats?.let {
                while (it.hasNextBucket()) {
                    val bucket = NetworkStats.Bucket()
                    it.getNextBucket(bucket)
                    /*Log.e(TAG, "App name             : ${getApplicationNameByUid(bucket.uid)}")
                      TrafficStatsHelper.humanReadableByteCountSI(bucket.txBytes)
                        }" Log.e(
                        TAG,
                        "getNetworkUsageForDay: ${TrafficStatsHelper.humanReadableByteCountSI(bucket.rxBytes)} - ${

                    )*/
                    val nameAndIcon = getApplicationNameByUid(context, bucket.uid)
                    val appInfoUsage = AppInfoUsage(
                        uid = bucket.uid,
                        name = nameAndIcon.first,
                        icon = nameAndIcon.second,
                        rxBytes = bucket.rxBytes,
                        txBytes = bucket.txBytes,
                        type = AppInfoType.getType(uid = bucket.uid),
                        state = AppInfoState.getState(bucket.state)
                    )
                    /*if(appInfoUsage.type == AppInfoType.TETHERING)
                        Log.e(TAG, "getNetworkUsageForDay: ${appInfoUsage.toString()} ${getTime(bucket.startTimeStamp)
                        } ${getTime(bucket.endTimeStamp)} ")*/

                    if (mapInfoUsage.containsKey(appInfoUsage.uid)) {
                        val oldItem = mapInfoUsage[appInfoUsage.uid]!!
                        val newItem = AppInfoUsage(
                            uid = appInfoUsage.uid,
                            name = if (oldItem.name.length > appInfoUsage.name.length) oldItem.name else appInfoUsage.name,
                            icon = oldItem.icon ?: appInfoUsage.icon,
                            rxBytes = oldItem.rxBytes + appInfoUsage.rxBytes,
                            txBytes = oldItem.txBytes + appInfoUsage.txBytes,
                            type = oldItem.type,
                            state = oldItem.state
                        )
                        mapInfoUsage[appInfoUsage.uid] = newItem
                    } else {
                        mapInfoUsage[appInfoUsage.uid] = appInfoUsage
                    }

                    totalUsage += bucket.rxBytes + bucket.txBytes
                    onSuccess(mapInfoUsage.values.toMutableList(), totalUsage)
                }
            }



            networkStats?.close()
            onFinish(mapInfoUsage.values.toMutableList(),totalUsage)

        } catch (e: SecurityException) {
            onFailure(e.message.toString())
        } catch (e: Exception) {
            onFailure(e.message.toString())
        }
    }

    fun getTime(timeStamp: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeStamp

        val day = calendar[Calendar.DAY_OF_MONTH]
        val month = calendar[Calendar.MONTH] + 1
        val year = calendar[Calendar.YEAR]
        val hour = calendar[Calendar.HOUR_OF_DAY]
        val minute = calendar[Calendar.MINUTE]
        val second = calendar[Calendar.SECOND]
        return "$hour:${minute}:${second}-${day}//${month}//${year}"
    }

    @RequiresPermission(android.Manifest.permission.PACKAGE_USAGE_STATS)
    fun getTotalNetworkUsageForDay(
        context: Context,
        startTime: Long,
        endTime: Long,
        networkType: Int = NetworkCapabilities.TRANSPORT_CELLULAR,
        onSuccess: (AppInfoUsage, totalUsage: Long) -> Unit,
        onFailure: (String) -> Unit
    ) {

        val subscriberId = getSubscriberId(networkType, context)
        // Log.e(TAG, "getTotalNetworkUsageForDay: ${subscriberId}", )
        try {
            val networkStatsManager =
                context.getSystemService(Context.NETWORK_STATS_SERVICE) as? NetworkStatsManager?
            val networkStats = networkStatsManager?.querySummaryForDevice(
                networkType,
                subscriberId,
                startTime,
                endTime
            )

            if (networkStats != null) {
                val nameAndIcon = getApplicationNameByUid(context, networkStats.uid)
                val appInfoUsage = AppInfoUsage(
                    uid = networkStats.uid,
                    name = nameAndIcon.first,
                    icon = nameAndIcon.second,
                    rxBytes = networkStats.rxBytes,
                    txBytes = networkStats.txBytes,
                    type = AppInfoType.getType(uid = networkStats.uid),
                    state = AppInfoState.getState(networkStats.state)
                )
                onSuccess(appInfoUsage, appInfoUsage.txBytes + appInfoUsage.rxBytes)
            } else {
                onFailure("Failed to query network usage")
            }


        } catch (e: SecurityException) {
            onFailure(e.message.toString())
        } catch (e: Exception) {
            onFailure(e.message.toString())
        }
    }


    fun getApplicationNameByUid(context: Context, uid: Int): Pair<String, Drawable?> {
        context.packageManager.let {
            val packages = it.getInstalledApplications(PackageManager.GET_META_DATA)
            val packageInfo = packages.find { it.uid == uid }
            return Pair(packageInfo?.loadLabel(it)?.toString() ?: "", packageInfo?.loadIcon(it))
        }

    }

    fun getApplicationIconByUid(context: Context, uid: Int): Drawable? {
        context.packageManager.let {
            val packages = it.getInstalledApplications(PackageManager.GET_META_DATA)
            val packageInfo = packages.find { it.uid == uid }
            return packageInfo?.loadIcon(it)
        }

    }

    fun getStartOfDay(calendar: Calendar): Long {
        val startOfDay = Calendar.getInstance().apply {
            timeInMillis = calendar.timeInMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        return startOfDay.timeInMillis
    }

    fun getEndOfDay(calendar: Calendar): Long {
        val endOfDay = Calendar.getInstance().apply {
            timeInMillis = calendar.timeInMillis
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }
        return endOfDay.timeInMillis
    }

}