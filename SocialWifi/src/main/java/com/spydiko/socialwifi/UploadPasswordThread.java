package com.spydiko.socialwifi;

import android.app.Dialog;
import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.view.Window;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by jim on 29/10/2013.
 */
public class UploadPasswordThread extends AsyncTask<Void, Void, Integer> {

	private final Context context;
	private final String ssid;
	private final String extraInfo;
	private final String bssid;
	private final String password;
	private Dialog loadingDialog;
	private SocialWifi socialWifi;
	private ServerUtils serverUtils;
	private Location location;
	private final static int FAILED_TO_LOCALIZE = -1;
	private final static int FAILED_TO_OPEN_SOCKET = -2;
	private final static int FAILED_TO_ADD = -3;
	private final static int FAILED_TO_UPDATE = -4;
	private final static int FAILED_TO_REPORT = -5;
	private final static int FAILED_TO_CONNECT = -6;
	private boolean correctUser;


	public UploadPasswordThread(Context context, SocialWifi socialWifi, String ssid, String bssid, String password, String extraInfo) {
		this.context = context;
		this.socialWifi = socialWifi;
		this.ssid = ssid;
		this.bssid = bssid;
		this.password = password;
		this.extraInfo = extraInfo;
		this.location = LocationUtils.getCurrentLocation();
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		loadingDialog = new Dialog(context, R.style.CustomDialog);
		loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		loadingDialog.setContentView(R.layout.loading_dialog);
		loadingDialog.setCancelable(false);
		loadingDialog.show();
		serverUtils = new ServerUtils();
		correctUser = serverUtils.setUsername(socialWifi.getSharedPreferenceString("username"));
		serverUtils.setWiFiInfo(ssid, bssid, password, extraInfo);
	}

	@Override
	protected Integer doInBackground(Void... params) {
		if (!correctUser) return ServerUtils.WRONG_USER;
		if (!serverUtils.tryToConnect(socialWifi.getWifiManager(), socialWifi.getConnectivityManager())) return FAILED_TO_CONNECT;
		if (location == null) return FAILED_TO_LOCALIZE;
		//		try {
		//			Thread.sleep(4000);
		//		} catch (Exception e){
		//			e.printStackTrace();
		//		}
		if (!serverUtils.tryToOpenSocket()) return FAILED_TO_OPEN_SOCKET;
		//		location = socialWifi.getLocationCoord();
		return serverUtils.tryToAdd(location);
	}

	@Override
	protected void onPostExecute(Integer state) {
		super.onPostExecute(state);
		serverUtils.tryToCloseSocket();
		if (state == FAILED_TO_CONNECT) {
			Toast.makeText(context, "Error connecting to network\nCheck password", Toast.LENGTH_SHORT).show();
		} else if (state == FAILED_TO_LOCALIZE) {
			Toast.makeText(context, "Failure to localize\nCheck connection", Toast.LENGTH_SHORT).show();
		} else if (state == FAILED_TO_OPEN_SOCKET) {
			Toast.makeText(context, "Socket error\nCheck connection", Toast.LENGTH_SHORT).show();
		} else if (state == FAILED_TO_ADD) {
			Toast.makeText(context, "Failure to add password\nCheck connection", Toast.LENGTH_SHORT).show();
		} else if (state == FAILED_TO_UPDATE) {
			Toast.makeText(context, "Failure to update list\nCheck connection", Toast.LENGTH_SHORT).show();
		} else if (state == FAILED_TO_REPORT) {
			Toast.makeText(context, "Failure to report", Toast.LENGTH_SHORT).show();
		} else if (state == ServerUtils.WRONG_USER) {
			Toast.makeText(context, "NOT VALID USER...Logged out", Toast.LENGTH_SHORT).show();
			socialWifi.logout();
			loadingDialog.dismiss();
		} else if (state == ServerUtils.WRONG_ADD) {
			Toast.makeText(context, "Error uploading...\nTry again later...", Toast.LENGTH_SHORT).show();
		} else if (state == ServerUtils.CORRECT_ADD) {
			Toast.makeText(context, "Success!\nNew password stored!", Toast.LENGTH_SHORT).show();
			ArrayList<WifiPass> tmp = socialWifi.getWifies();
			ArrayList<Double> loc = new ArrayList<Double>();
			loc.add(0, location.getLatitude());
			loc.add(1, location.getLongitude());
			tmp.add(new WifiPass(ssid, bssid, password, loc));
			socialWifi.setWifies(tmp);
			socialWifi.getWifiManager().startScan();
		}
		loadingDialog.dismiss();
	}
}
