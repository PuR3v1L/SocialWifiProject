package com.spydiko.socialwifi;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.Window;
import android.widget.Toast;

/**
 * Created by jim on 29/10/2013.
 */
public class UpdateThread extends AsyncTask<Void, Void, Integer> {

	private final Context context;
	private Dialog loadingDialog;
	private SocialWifi socialWifi;
	private ServerUtils serverUtils;
	private final static int FAILED_TO_LOCALIZE = -1;
	private final static int FAILED_TO_OPEN_SOCKET = -2;
	private final static int FAILED_TO_ADD = -3;
	private final static int FAILED_TO_UPDATE = -4;
	private final static int FAILED_TO_REPORT = -5;
	private final static int FAILED_TO_CONNECT = -6;

	public UpdateThread(Context context, SocialWifi socialWifi) {
		this.context = context;
		this.socialWifi = socialWifi;
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
		serverUtils.setUsername(socialWifi.getSharedPreferenceString("username"));
	}

	@Override
	protected Integer doInBackground(Void... params) {
		if (!serverUtils.tryToLocalize(socialWifi)) return FAILED_TO_LOCALIZE;
		if (!serverUtils.tryToOpenSocket()) return FAILED_TO_OPEN_SOCKET;
		return serverUtils.tryToUpdate(socialWifi.getAreaRadius(), socialWifi.getLocationCoord());
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
		} else if (state == ServerUtils.WRONG_UPDATE) {
			Toast.makeText(context, "Error updating...\nTry again later...", Toast.LENGTH_SHORT).show();
		} else if (state == ServerUtils.CORRECT_UPDATE) {
			Toast.makeText(context, "Success!\nUpdate worked!", Toast.LENGTH_SHORT).show();
			socialWifi.storeXML(serverUtils.getBuffer());
			socialWifi.setWifies(socialWifi.readFromXML("server.xml"));
			socialWifi.storeXML(serverUtils.getPyBuffer());
			socialWifi.setPyWifies(socialWifi.readFromXMLPython("server.xml"));
		}
		loadingDialog.dismiss();
	}
}
