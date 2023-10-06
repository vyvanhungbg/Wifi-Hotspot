package com.hungvv.wifihotspot.helper


/**
- Create by :Vy Hùng
- Create at :07,October,2023
 **/

interface OnChangeStateWifiHotspotListener {
    fun onLoading()
    fun onEnabled()
    fun onDisabled()
    fun onFailure()
}