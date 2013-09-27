package com.spydiko.socialwifi;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;

/**
 * Created by jim on 5/9/2013.
 */
public class SocialWifi extends Application {

    private final static String TAG = SocialWifi.class.getSimpleName();
    private WifiManager wifi;
    private ConnectivityManager connectivityManager;

    public void onCreate() {
        super.onCreate();
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    }


    public WifiManager getWifi() {
        return wifi;
    }

    public ConnectivityManager getConnectivityManager() {
        return connectivityManager;
    }

}
