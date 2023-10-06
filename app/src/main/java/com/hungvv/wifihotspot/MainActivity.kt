package com.hungvv.wifihotspot

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.hungvv.wifihotspot.helper.HotspotHelper
import com.hungvv.wifihotspot.helper.OnChangeStateWifiHotspotListener

private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    private val listenerWifiHotspotChanged = object :OnChangeStateWifiHotspotListener{
        override fun onLoading() {
            Log.e(TAG, "onLoading: ", )
        }

        override fun onEnabled() {
            Log.e(TAG, "onEnabled: ", )
        }

        override fun onDisabled() {
            Log.e(TAG, "onDisabled: ", )
        }

        override fun onFailure() {
            Log.e(TAG, "onFailure: ", )
        }
    }
    override fun onResume() {
        super.onResume()
        HotspotHelper.registerWifiHotspotChange(context = this, listenerWifiHotspotChanged)
    }

    override fun onPause() {
        super.onPause()
        HotspotHelper.unRegisterWifiHotspotChange(context = this,listenerWifiHotspotChanged)
    }
}