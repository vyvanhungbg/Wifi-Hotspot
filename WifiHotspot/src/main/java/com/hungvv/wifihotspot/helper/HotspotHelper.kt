package com.hungvv.wifihotspot.helper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresPermission
import java.lang.reflect.Method


/**
- Create by :Vy Hùng
- Create at :07,October,2023
 **/

object HotspotHelper {
    private const val methodTurnOfHotspot = "setWifiApEnabled"
    private const val packageName: String = "com.android.settings"
    private const val classNameTether = "com.android.settings.TetherSettings"
    private const val classNameSettingTether =
        "com.android.settings.Settings\$TetherWifiSettingsActivity"
    private const val classNameSettingTether2 =
        "com.android.settings.Settings\$WifiApSettingsActivity"

    const val ACTION_AP_CHANGE = "android.net.wifi.WIFI_AP_STATE_CHANGED"
    const val KEY_EXTRA_WIFI_AP_STATE = "wifi_state"

    const val DISABLED_HOTSPOT = 11
    const val LOADING_HOTSPOT = 12
    const val ENABLED_HOTSPOT = 13
    const val FAILED_HOTSPOT = 14


    private val listClassNameTether = mutableListOf(
        classNameSettingTether,
        classNameSettingTether2,
        classNameTether
    )

    private val listRegisteredHotspotChange = mutableListOf<OnChangeStateWifiHotspotListener>()

    private val hotspotChanged = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_AP_CHANGE) {
                val wifiState = intent.getIntExtra(KEY_EXTRA_WIFI_AP_STATE, -1)

                when (wifiState) {
                    DISABLED_HOTSPOT -> {
                        listRegisteredHotspotChange.forEach { it.onDisabled() }
                    }

                    ENABLED_HOTSPOT -> {
                        listRegisteredHotspotChange.forEach { it.onEnabled() }
                    }

                    LOADING_HOTSPOT -> {
                        listRegisteredHotspotChange.forEach { it.onLoading() }
                    }
                    FAILED_HOTSPOT -> {
                        listRegisteredHotspotChange.forEach { it.onEnabled() }
                    }
                }
            }
        }
    }



    @RequiresPermission("android.permission.ACCESS_WIFI_STATE")
    fun registerWifiHotspotChange(context: Context,listener: OnChangeStateWifiHotspotListener){
        synchronized(this) {
            if(listRegisteredHotspotChange.isEmpty()){
                context.registerReceiver(
                    hotspotChanged, IntentFilter(ACTION_AP_CHANGE)
                )
            }
            listRegisteredHotspotChange.add(listener)
        }
    }

    fun unRegisterWifiHotspotChange(context: Context,listener: OnChangeStateWifiHotspotListener){
        synchronized(this){
            listRegisteredHotspotChange.remove(listener)
            if (listRegisteredHotspotChange.isEmpty()){
                context.unregisterReceiver(hotspotChanged)
            }
        }
    }


    fun addNewClassNameTether(classNameTether: String) {
        if (!listClassNameTether.contains(classNameTether) && classNameTether.isNotEmpty()) {
            listClassNameTether.add(classNameTether)
        }
    }

    fun navigateToHotspotSetting(
        context: Context,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        listClassNameTether.forEach { _classNameTether ->
            val intent = Intent().apply {
                setClassName(packageName, _classNameTether)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            kotlin.runCatching {
                context.startActivity(intent)
            }.onSuccess {
                return
            }
        }
        onFailure.invoke()
    }

    @RequiresPermission(android.Manifest.permission.WRITE_SETTINGS)
    fun setStateHotspot(
        context: Context,
        isActive: Boolean,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {

        if (!Settings.System.canWrite(context)) {
            onFailure.invoke("Requires permission write settings")
            return
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) {
            try {
                val wifiManager =
                    context.getSystemService(Context.WIFI_SERVICE) as WifiManager
                val methods =
                    wifiManager.javaClass.declaredMethods
                for (method in methods) {
                    if (method.name == methodTurnOfHotspot) {
                        if (isActive) {
                            wifiManager.isWifiEnabled = false
                            method.invoke(wifiManager, null, true)
                            onSuccess()
                        } else {
                            method.invoke(wifiManager, null, false)
                            wifiManager.isWifiEnabled = true
                            onSuccess()
                        }
                    }
                }

            } catch (e: Exception) {
                onFailure(e.message.toString())
            }
        } else {
            if (isActive) {
                startTethering(context, { onSuccess() }, { onFailure(it) })
            } else {
                stopTethering(context, { onSuccess() }, { onFailure(it) })
            }
        }
    }

    fun isTetherActive(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            val wifi = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            return try {
                val method: Method =
                    wifi.javaClass.getDeclaredMethod("isWifiApEnabled")
                (method.invoke(wifi) as Boolean)
            } catch (ignored: Throwable) {
                false
            }
        } else {
            try {

                val method: Method? =
                    connectivityManager.javaClass.getDeclaredMethod("getTetheredIfaces")
                if (method == null) {
                    return false
                } else {
                    val res = method.invoke(connectivityManager) as Array<String>
                    if (res.isNotEmpty()) {
                        return true
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                return false
            }

        }
        return false
    }


    fun onStartTetheringCallbackClass(): Class<*>? {
        val className = "android.net.ConnectivityManager\$OnStartTetheringCallback"
        try {
            return Class.forName(className)
        } catch (e: ClassNotFoundException) {

            e.printStackTrace()
        }
        return null
    }


    @RequiresPermission(android.Manifest.permission.WRITE_SETTINGS)
    private fun startTethering(
        context: Context,
        onSuccess: () -> Unit,
        onFailed: (String) -> Unit
    ) {

        if (isTetherActive(context)) {
            onSuccess()
            return
        }
        ReflectionJavaClass.tryEnableTethering(context,
            object : ReflectionJavaClass.TetheringCallback {
                override fun onSuccess() {
                    onSuccess()
                }

                override fun onFailure(message: String?) {
                    onFailed(message.toString())
                }
            })
    }

    private fun stopTethering(
        context: Context,
        onSuccess: () -> Unit,
        onFailed: (String) -> Unit
    ) {
        try {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val method: Method? = connectivityManager.javaClass.getDeclaredMethod(
                "stopTethering",
                Int::class.javaPrimitiveType
            )
            method?.invoke(connectivityManager, ConnectivityManager.TYPE_MOBILE)
            onSuccess()
        } catch (e: Exception) {
            onFailed(e.message.toString())
        }
    }
}