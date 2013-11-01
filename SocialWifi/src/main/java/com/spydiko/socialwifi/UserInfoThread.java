package com.spydiko.socialwifi;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by spiros on 11/1/13.
 */
public class UserInfoThread extends AsyncTask<Void, Integer, Integer> {

	private final static int FAILED_TO_LOCALIZE = -1;
	private final static int FAILED_TO_OPEN_SOCKET = -2;
	private final static int FAILED_TO_ADD = -3;
	private final static int FAILED_TO_UPDATE = -4;
	private final static int FAILED_TO_REPORT = -5;
	private final static int FAILED_TO_CONNECT = -6;
	private final Context context;
	private ServerUtils serverUtils;
	private SocialWifi socialWifi;
	private boolean correctUser;
	private ProgressBar numOfUploadsProgressBar;
	private TextView numOfUploadsTextview;

	public UserInfoThread(Context context, SocialWifi socialWifi, View view) {

		this.context = context;
		this.socialWifi = socialWifi;
		this.numOfUploadsTextview = (TextView) view.findViewById(R.id.number_of_uploads_textview);
		this.numOfUploadsProgressBar = (ProgressBar) view.findViewById(R.id.number_of_uploads_progressbar);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		serverUtils = new ServerUtils();
		correctUser = serverUtils.setUsername(socialWifi.getSharedPreferenceString("username"));
		numOfUploadsProgressBar.setVisibility(View.VISIBLE);
		numOfUploadsTextview.setText(" " + socialWifi.getNumOfUploads());

	}

	@Override
	protected Integer doInBackground(Void... voids) {
		if (!correctUser) return ServerUtils.WRONG_USER;
		if (!serverUtils.tryToOpenSocket()) return FAILED_TO_OPEN_SOCKET;
		return serverUtils.tryToGetUserInfo(socialWifi);
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
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
		} else if (state == ServerUtils.WRONG_USERINFO) {
			Toast.makeText(context, "Error getting user info...\nTry again later...", Toast.LENGTH_SHORT).show();
		} else if (state == ServerUtils.CORRECT_USERINFO) {
			Toast.makeText(context, "Success getting user info...\nnumOfUploads: " + socialWifi.getNumOfUploads(), Toast.LENGTH_SHORT).show();
			numOfUploadsTextview.setText(" " + socialWifi.getNumOfUploads());
		}
		numOfUploadsProgressBar.setVisibility(View.INVISIBLE);
	}
}
