package com.spydiko.socialwifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * Created by jim on 1/11/2013.
 */
public class WifiEnabledReceiver extends BroadcastReceiver {

	private static final String TAG = WifiEnabledReceiver.class.getSimpleName();
	SocialWifi socialWifi;

	@Override
	public void onReceive(Context context, Intent intent) {
		socialWifi = (SocialWifi) context.getApplicationContext();
		Log.d(TAG,"mpika");
		if (!socialWifi.getBoot()) return;
		Log.d(TAG,"boot enabled");

		if(intent.getExtras()!=null) {
			NetworkInfo ni=(NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);
			if(ni!=null && ni.getState()==NetworkInfo.State.CONNECTED) {
				Log.i("app","Network "+ni.getTypeName()+" connected");
				Log.d("app",socialWifi.getSharedPreferenceBoolean("backgroundupdate")+"");
				if (socialWifi.getSharedPreferenceBoolean("backgroundupdate")){
					socialWifi.getLocation();
					Log.d(TAG, "WIFI ENABLED");
					BackgroundUpdateThread backgroundUpdateThread= new BackgroundUpdateThread(context, socialWifi);
					backgroundUpdateThread.execute();
					socialWifi.setSharedPreferenceBoolean("backgroundupdate",false);
				}
			} else if(intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY,Boolean.FALSE)) {
				Log.d("app","There's no network connectivity");
				socialWifi.setSharedPreferenceBoolean("backgroundupdate",true);
			}
		}

		/*connectivityManager = socialWifi.getConnectivityManager();
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		boolean isWifi = false;
		if (activeNetworkInfo != null) {
			isWifi = activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI;
		}
		boolean isConnected = activeNetworkInfo !=null && activeNetworkInfo.isConnectedOrConnecting();
		if (isWifi && isConnected ){
			socialWifi.getLocation();
			Log.d(TAG, "WIFI ENABLED");
			BackgroundUpdateThread backgroundUpdateThread= new BackgroundUpdateThread(context, socialWifi);
			backgroundUpdateThread.execute();
		}*/
	}
}
