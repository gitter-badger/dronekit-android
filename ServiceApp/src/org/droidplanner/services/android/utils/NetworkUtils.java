package org.droidplanner.services.android.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import org.droidplanner.services.android.core.drone.companion.solo.SoloComp;

/**
 * Created by Fredia Huya-Kouadio on 5/11/15.
 */
public class NetworkUtils {

    public static boolean isNetworkAvailable(Context context) {
        if (isOnSololinkNetwork(context))
            return false;

        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static String getCurrentWifiLink(Context context) {
        final WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        final WifiInfo connectedWifi = wifiMgr.getConnectionInfo();
        final String connectedSSID = connectedWifi == null ? null : connectedWifi.getSSID().replace("\"", "");
        return connectedSSID;
    }

    public static boolean isOnSololinkNetwork(Context context) {

        final String connectedSSID = getCurrentWifiLink(context);
        return connectedSSID != null && connectedSSID.startsWith(SoloComp.SOLO_LINK_WIFI_PREFIX);
    }
}
