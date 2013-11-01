package com.spydiko.socialwifi;

import android.content.Context;
import android.os.AsyncTask;

/**
 * Created by jim on 1/11/2013.
 */
public class BackgroundUpdateThread extends AsyncTask<Void, Void, Integer> {


	private final Context context;
	private final SocialWifi socialWifi;
	private ServerUtils serverUtils;
	private final static int FAILED_TO_LOCALIZE = -1;
	private final static int FAILED_TO_OPEN_SOCKET = -2;
	private final static int FAILED_TO_ADD = -3;
	private final static int FAILED_TO_UPDATE = -4;
	private final static int FAILED_TO_REPORT = -5;
	private final static int FAILED_TO_CONNECT = -6;
	private boolean correctUser;

	public BackgroundUpdateThread(Context context, SocialWifi socialWifi) {
		this.context = context;
		this.socialWifi = socialWifi;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		serverUtils = new ServerUtils();
		correctUser = serverUtils.setUsername(socialWifi.getSharedPreferenceString("username"));
	}

	@Override
	protected void onPostExecute(Integer state) {
		super.onPostExecute(state);
		serverUtils.tryToCloseSocket();
		if (state == ServerUtils.WRONG_USER) {
			socialWifi.logout();
		} else if (state == ServerUtils.CORRECT_UPDATE) {
			socialWifi.storeXML(serverUtils.getBuffer());
			socialWifi.setWifies(socialWifi.readFromXML("server.xml"));
			socialWifi.storeXML(serverUtils.getPyBuffer());
			socialWifi.setPyWifies(socialWifi.readFromXMLPython("server.xml"));
			socialWifi.getWifiManager().startScan();
		}
	}

	@Override
	protected Integer doInBackground(Void... params) {
		if (!correctUser) return ServerUtils.WRONG_USER;
		if (!serverUtils.tryToLocalize(socialWifi)) return FAILED_TO_LOCALIZE;
		if (!serverUtils.tryToOpenSocket()) return FAILED_TO_OPEN_SOCKET;
		return serverUtils.tryToUpdate(socialWifi.getAreaRadius(), socialWifi.getLocationCoord());
	}
}
