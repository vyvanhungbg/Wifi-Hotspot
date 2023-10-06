package com.hungvv.wifihotspot.helper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Handler;


import com.android.dx.stock.ProxyBuilder;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 - Create by :Vy Hùng
 - Create at :07,October,2023
 **/

public class ReflectionJavaClass {
    // cannot modify this class
    private static final String TAG = "ReflectionJavaClass";

    interface TetheringCallback {
        void onSuccess();

        void onFailure(String message);
    }

    static void tryEnableTethering(Context context, TetheringCallback callback) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            File outputDir = context.getCodeCacheDir();
            Object proxy;
            proxy = ProxyBuilder.forClass(HotspotHelper.INSTANCE.onStartTetheringCallbackClass())
                    .dexCache(outputDir).handler(new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            switch (method.getName()) {
                                case "onTetheringStarted":
                                    //Log.e(TAG, "onTetheringStarted: ");
                                    break;
                                case "onTetheringFailed":
                                    //Log.e(TAG, "onTetheringFailed: ");
                                    break;
                                default:
                                    ProxyBuilder.callSuper(proxy, method, args);
                            }
                            return null;
                        }

                    }).build();
            Method method = connectivityManager.getClass().getDeclaredMethod("startTethering", int.class, boolean.class, HotspotHelper.INSTANCE.onStartTetheringCallbackClass(), Handler.class);
            method.invoke(connectivityManager, ConnectivityManager.TYPE_MOBILE, false, proxy, null);
            callback.onSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            callback.onFailure(e.getMessage());
        }
    }


}
