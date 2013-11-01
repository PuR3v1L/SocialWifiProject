package com.spydiko.socialwifi;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by spiros on 11/1/13.
 */
public class ReportThread extends AsyncTask<Void, Void, Integer> {

	private final static String TAG = ReportThread.class.getSimpleName();
	private final static int FAILED_TO_LOCALIZE = -1;
	private final static int FAILED_TO_OPEN_SOCKET = -2;
	private final static int FAILED_TO_ADD = -3;
	private final static int FAILED_TO_UPDATE = -4;
	private final static int FAILED_TO_REPORT = -5;
	private final static int FAILED_TO_CONNECT = -6;
	private final Context context;
	private final String ssid;
	private final String extraInfo;
	private final String bssid;
	private final String password;
	private Dialog loadingDialog;
	private ServerUtils serverUtils;
	private SocialWifi socialWifi;
	private boolean correctUser;


	public ReportThread(Context context, SocialWifi socialWifi, String ssid, String bssid, String password, String extraInfo) {
		this.context = context;
		this.socialWifi = socialWifi;
		this.ssid = ssid;
		this.bssid = bssid;
		this.password = password;
		this.extraInfo = extraInfo;

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
		if (!serverUtils.tryToOpenSocket()) return FAILED_TO_OPEN_SOCKET;
		return serverUtils.tryToReport();

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
		} else if (state == ServerUtils.WRONG_REPORT) {
			Toast.makeText(context, "Error reporting...\nTry again later...", Toast.LENGTH_SHORT).show();
		} else if (state == ServerUtils.CORRECT_REPORT) {
			Toast.makeText(context, "Success!\nReport worked!", Toast.LENGTH_SHORT).show();
			ArrayList<WifiPass> tmp = socialWifi.getWifies();
			for (WifiPass wifi : tmp) {
				if (wifi.getBssid() == bssid) {
					wifi.setPassword(password);
					Log.d(TAG, "password changed to " + password);
				}
			}
			socialWifi.setWifies(tmp);

		}
		loadingDialog.dismiss();
	}
}
