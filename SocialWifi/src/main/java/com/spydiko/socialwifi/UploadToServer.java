package com.spydiko.socialwifi;

import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;

/**
 * Created by spiros on 10/2/13.
 * This AsyncTask handles the communication with the server. It asks for a refresh or it tries to upload a new password.
 *
 */
public class UploadToServer extends AsyncTask<Void, Void, Boolean> {

	private static final String TAG = UploadToServer.class.getSimpleName();
	private Context context;
	private Socket sk;
	private DataOutputStream dos;
	private DataInputStream dis;
	private String hostIPstr = "83.212.121.161";
	private int serverPort = 44444;
	private Dialog loadingDialog;
	private int size;
	private boolean add;
	private String ssid, bssid, password;
	private byte[] buffer;
	private SocialWifi socialWifi;
	private double[] location;
	private ConnectivityManager connectivityManager;
	private WifiManager wifi;
	private boolean failureToConnect;
	private String extraInfo;
	private boolean failureToLocate;
	private String response;

	public UploadToServer(Context context, SocialWifi socialWifi) {
		super();
		this.socialWifi = socialWifi;
		this.context = context;
		add = false;
	}

	public UploadToServer(String ssid, String bssid, String password, Context context, SocialWifi socialWifi, String extraInfo) {
		super();
		this.ssid = ssid;
		this.bssid = bssid;
		this.password = password;
		this.socialWifi = socialWifi;
		this.context = context;
		this.extraInfo = extraInfo;
		add = true;
	}

	/**
	 * Thread that does the main work. If a step fails the processs ends.
	 *
	 * @param params: nothing... :P
	 * @return true if successes, false otherwise
	 */
	@Override
	protected Boolean doInBackground(Void... params) {

		if (add) {
			if (!tryToConnect()) return false;
		}
		if (!tryToLocalize()) return false;


		if (!tryToOpenSocket()) return false;
		if (add) {
			if (!tryToAdd()) return false;
		} else {
			if (!tryToUpdate()) return false;
		}

		return true;
	}

	/**
	 * This function tries to update the local password database based on the radius the user has selected.
	 *
	 * @return true if communication successes, false otherwise.
	 */
	private boolean tryToUpdate() {
		try {
			dos.writeBytes("update" + "\r\n");
			dos.writeBytes(String.valueOf(socialWifi.getAreaRadius()) + "\r\n");
			dos.writeBytes(Double.toString(location[0]) + "\r\n");
			dos.writeBytes(Double.toString(location[1]) + "\r\n");
			Log.d(TAG, "Sent: " + String.valueOf(socialWifi.getAreaRadius()) + " " + Double.toString(location[0]) + " " + Double.toString(location[1]));
			size = Integer.parseInt(dis.readLine());
			Log.d(TAG, "Size: " + size);
			buffer = new byte[size];
			Log.d(TAG, "Buffer socket: " + sk.getReceiveBufferSize());
			sk.setReceiveBufferSize(size);
			dis.read(buffer);
			Log.d(TAG, "Messages sent");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * This function tries to send to server a new WiFi SSID-BSSIS and password. It also stores the username of the uploader.
	 *
	 * @return true if successful, false otherwise.
	 */
	private boolean tryToAdd() {
		try {
			dos.writeBytes("addPass" + "\r\n");
			Log.d(TAG, Double.toString(location[0]));
			Log.d(TAG, Double.toString(location[1]));
			dos.writeBytes(Double.toString(location[0]) + "\r\n");
			dos.writeBytes(Double.toString(location[1]) + "\r\n");
			Log.d(TAG, ssid);
			dos.writeBytes(ssid + "\r\n");
			Log.d(TAG, bssid);
			dos.writeBytes(bssid + "\r\n");
			Log.d(TAG, password);
			dos.writeBytes(password + "\r\n");
			Log.d(TAG, socialWifi.getSharedPreferenceString("username"));
			dos.writeBytes(socialWifi.getSharedPreferenceString("username") + "\r\n");
			Log.d(TAG, "Password add sent");
			response = dis.readLine();
			if (response.equals("Done")) return true;
			else return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * This function tries to open a Socket in order for the rest functions to communicatite with the server.
	 *
	 * @return true if opening the socket is successful, false otherwise.
	 */
	private boolean tryToOpenSocket() {
		try {
			Log.d(TAG, "Trying to open socket");
			sk = new Socket();
			SocketAddress remoteaddr = new InetSocketAddress(hostIPstr, serverPort);
			sk.setSoTimeout(10000);
			sk.connect(remoteaddr);
			buffer = null;
			Log.d(TAG, "Socket opened");
			dos = new DataOutputStream(sk.getOutputStream());
			dis = new DataInputStream(sk.getInputStream());
			Log.d(TAG, "Trying to sent message");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	/**
	 * This function tries to get the location of the phone, based on network.
	 *
	 * @return true if retrieving is successful, false otherwise.
	 */
	private boolean tryToLocalize() {
		long current = System.currentTimeMillis();
		/* Get current location through wifi*/
		while (!socialWifi.isGotLocation() && System.currentTimeMillis() - current < 10000) {
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (!socialWifi.isGotLocation()) {
			failureToLocate = true;
			return false;
		}
		location = socialWifi.getLocationCoord();
		return true;
	}

	/**
	 * Function which tries to connect to the specified WiFi, based on encryption method.
	 *
	 * @return true if connection successful, false otherwise.
	 */
	private boolean tryToConnect() {
		int typeOfEncryption = 0;
		Log.d(TAG, "extraInfo: " + extraInfo);
		if (extraInfo.contains("WEP")) typeOfEncryption = 1;
		else if (extraInfo.contains("WPA2")) typeOfEncryption = 3;
		else if (extraInfo.contains("WAP")) typeOfEncryption = 2;
		socialWifi.removeNetwork(ssid);
		socialWifi.connect(ssid, password, typeOfEncryption);
		    /* Check if password is correct and if the phone can connect to the network*/
		long current = System.currentTimeMillis();
		NetworkInfo networkInfo;
		boolean ok = false;
		Log.d(TAG, "entered");
		location = null;
		connectivityManager = socialWifi.getConnectivityManager();
		wifi = socialWifi.getWifi();
		while (System.currentTimeMillis() - current < 10000) {
			networkInfo = connectivityManager.getActiveNetworkInfo();
			try {
				if (networkInfo != null && networkInfo.isConnected() && wifi.getConnectionInfo().getBSSID().equals(bssid)) {
					ok = true;
					Log.d(TAG, "correct pass");
					wifi.saveConfiguration();
					break;
				}
			} catch (Exception e) {

			}
		}
		if (!ok) {
			failureToConnect = true;
			socialWifi.removeNetwork(ssid);
			wifi.setWifiEnabled(false);
			wifi.setWifiEnabled(true);
			return false;
		}
		return true;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		loadingDialog = new Dialog(context, R.style.CustomDialog);
		loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		loadingDialog.setContentView(R.layout.loading_dialog);
		loadingDialog.setCancelable(false);
		loadingDialog.show();
		failureToConnect = false;
		failureToLocate = false;
	}

	@Override
	protected void onPostExecute(Boolean aBool) {
		super.onPostExecute(aBool);
		if (failureToConnect) {
			Toast.makeText(context, "Error connecting to network\nCheck password", Toast.LENGTH_SHORT).show();
		} else if (failureToLocate) {
			Toast.makeText(context, "Failure to localize\nCheck connection", Toast.LENGTH_SHORT).show();
		} else if (!aBool) {
			Toast.makeText(context, "Failure to contact server\nCheck connection", Toast.LENGTH_SHORT).show();
		} else {
			Log.d(TAG, "onPostExecute");
			try {
				Log.d(TAG, "Closing Everything");
				dos.close();
				dis.close();
				sk.close();
				if (add) {
					ArrayList<WifiPass> tmp = socialWifi.getWifies();
					ArrayList<Double> loc = new ArrayList<Double>();
					loc.add(0, location[0]);
					loc.add(1, location[1]);
					tmp.add(new WifiPass(ssid, bssid, password, loc));
					socialWifi.setWifies(tmp);
					Toast.makeText(context, "Success!\nNew password stored!", Toast.LENGTH_SHORT).show();
				} else {
					if (buffer != null) {
						socialWifi.storeXML(buffer);
						socialWifi.setWifies(socialWifi.readFromXML("server.xml"));
						Toast.makeText(context, "Success!", Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(context, "Error updating...\nTry again later...", Toast.LENGTH_SHORT).show();
					}
				}
				socialWifi.getWifi().startScan();
			} catch (Exception e) {
				Toast.makeText(context, "Error updating...\nTry again later...", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
		}
		loadingDialog.dismiss();
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
	}
}