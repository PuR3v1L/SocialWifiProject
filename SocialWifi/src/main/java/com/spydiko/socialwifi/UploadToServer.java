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
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;

/**
 * Created by spiros on 10/2/13.
 * This AsyncTask handles the communication with the server. It asks for a refresh or it tries to upload a new password.
 */
public class UploadToServer extends AsyncTask<Void, Void, Integer> {

	private static final String TAG = UploadToServer.class.getSimpleName();
	private Context context;
	private Socket sk;
	private DataOutputStream dos;
	private DataInputStream dis;
	private String hostIPstr = "83.212.121.161";
	private int serverPort = 44444;
	private Dialog loadingDialog;
	private int size;
	private boolean add, report, refresh;
	private String ssid, bssid, password;
	private byte[] buffer;
	private SocialWifi socialWifi;
	private double[] location;
	private ConnectivityManager connectivityManager;
	private WifiManager wifi;
	private boolean failureToConnect;
	private String extraInfo;
	private boolean notUser;
	private String response;

	public UploadToServer(Context context, SocialWifi socialWifi) {
		super();
		this.socialWifi = socialWifi;
		this.context = context;
		this.notUser = false;
		add = false;
		report = false;
		refresh = true;
	}

	public UploadToServer(String ssid, String bssid, String password, Context context, SocialWifi socialWifi, String extraInfo) {
		super();
		this.ssid = ssid;
		this.bssid = bssid;
		this.password = password;
		this.socialWifi = socialWifi;
		this.context = context;
		this.extraInfo = extraInfo;
		this.notUser = false;
		add = true;
		report = false;
		refresh = false;
	}

	public UploadToServer(String ssid, String bssid, String password, Context context, SocialWifi socialWifi, String extraInfo, boolean b) {
		add = false;
		report = true;
		refresh = false;
		this.notUser = false;
		this.ssid = ssid;
		this.bssid = bssid;
		this.password = password;
		this.socialWifi = socialWifi;
		this.context = context;
		this.extraInfo = extraInfo;
	}

	/**
	 * Thread that does the main work. If a step fails the processs ends.
	 *
	 * @param params: nothing... :P
	 * @return 0 if connection error, -1 if localization error, -2 if socket error, -3 if error to add, -4 if error to update
	 * and 1 if correct
	 */
	@Override
	protected Integer doInBackground(Void... params) {

		if (add || report) {
			if (!tryToConnect()) return 0;
		}
		if (add || refresh) {
			if (!tryToLocalize()) return -1;

		}

		if (!tryToOpenSocket()) return -2;

		if (add) {
			if (!tryToAdd()) return -3;
		} else if (refresh) {
			if (!tryToUpdate()) return -4;
		} else if (report) {
			if (!tryToReport()) return -5;
		}

		return 1;
	}

	private boolean tryToReport() {
		try {
			dos.writeBytes("changePass" + "\r\n");
			Log.d(TAG, ssid);
			dos.writeBytes(ssid + "\r\n");
			Log.d(TAG, bssid);
			dos.writeBytes(bssid + "\r\n");
			Log.d(TAG, password);
			dos.writeBytes(password + "\r\n");
			Log.d(TAG, socialWifi.getSharedPreferenceString("username"));
			dos.writeBytes(socialWifi.getSharedPreferenceString("username") + "\r\n");
			Log.d(TAG, "Password report sent");
			response = dis.readLine();
			if (response.equals("Done")) return true;
			else if (response.contains("notUser")) {
				socialWifi.logout();
				notUser = true;
				return true;
			} else return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

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
			dos.writeBytes(socialWifi.getSharedPreferenceString("username") + "\r\n");
			Log.d(TAG, "Sent: " + String.valueOf(socialWifi.getAreaRadius()) + " " + Double.toString(location[0]) + " " + Double.toString(location[1]) + " " + socialWifi.getSharedPreferenceString("username"));
			String inputMsg = dis.readLine();
			Log.d(TAG, "inputMsg: " + inputMsg);
			try {
				size = Integer.parseInt(inputMsg);
				Log.d(TAG, "Size: " + size);
				buffer = new byte[size];
				Log.d(TAG, "Buffer socket: " + sk.getReceiveBufferSize());
				sk.setReceiveBufferSize(size);
                dis.readFully(buffer, 0, buffer.length);
                Log.d(TAG, "Buffer size: " + buffer.length);
            } catch (NumberFormatException e) {
				Log.d(TAG, "Not User");
				e.printStackTrace();
				socialWifi.logout();
				notUser = true;
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}

			Log.d(TAG, "Messages Received");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * This function tries to send to server a new WiFi SSID-BSSID and password. It also stores the username of the uploader.
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
			Log.d(TAG, "Response " + response);
			if (response.equals("Done")) return true;
			else if (response.contains("notUser")) {
				socialWifi.logout();
				notUser = true;
				return true;
			} else return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * This function tries to open a Socket in order for the rest functions to communicate with the server.
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
		else if (extraInfo.contains("WAP") || extraInfo.contains("WPA")) typeOfEncryption = 2;
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
	}

	@Override
	protected void onPostExecute(Integer state) {
		super.onPostExecute(state);
		if (state == 0) {
			Toast.makeText(context, "Error connecting to network\nCheck password", Toast.LENGTH_SHORT).show();
		} else if (state == -1) {
			Toast.makeText(context, "Failure to localize\nCheck connection", Toast.LENGTH_SHORT).show();
		} else if (state == -2) {
			Toast.makeText(context, "Socket error\nCheck connection", Toast.LENGTH_SHORT).show();
		} else if (state == -3) {
			Toast.makeText(context, "Failure to add password\nCheck connection", Toast.LENGTH_SHORT).show();
		} else if (state == -4) {
			Toast.makeText(context, "Failure to update list\nCheck connection", Toast.LENGTH_SHORT).show();
		} else {
			Log.d(TAG, "onPostExecute");
			if (add) {
				if (notUser) {
					Toast.makeText(context, "NOT VALID USER...Logged out", Toast.LENGTH_SHORT).show();
					loadingDialog.dismiss();
					return;
				} else {
					Toast.makeText(context, "Success!\nNew password stored!", Toast.LENGTH_SHORT).show();
				}
				ArrayList<WifiPass> tmp = socialWifi.getWifies();
				ArrayList<Double> loc = new ArrayList<Double>();
				loc.add(0, location[0]);
				loc.add(1, location[1]);
				tmp.add(new WifiPass(ssid, bssid, password, loc));
				socialWifi.setWifies(tmp);
			} else if (refresh) {
				if (buffer != null) {
					if (notUser) {
						Toast.makeText(context, "NOT VALID USER...Logged out", Toast.LENGTH_SHORT).show();
						loadingDialog.dismiss();
						return;
					} else {
						Toast.makeText(context, "Success!\nUpdate worked!", Toast.LENGTH_SHORT).show();
					}
					socialWifi.storeXML(buffer);
					socialWifi.setWifies(socialWifi.readFromXML("server.xml"));
				} else {
					Toast.makeText(context, "Error updating...\nTry again later...", Toast.LENGTH_SHORT).show();
				}
			} else if (report) {
				if (notUser) {
					Toast.makeText(context, "NOT VALID USER...Logged out", Toast.LENGTH_SHORT).show();
					loadingDialog.dismiss();
					return;
				} else {
					Toast.makeText(context, "Success!\nReport worked!", Toast.LENGTH_SHORT).show();
				}
				ArrayList<WifiPass> tmp = socialWifi.getWifies();
				for (WifiPass wifi : socialWifi.getWifies()) {
					if (wifi.getBssid() == bssid) {
						wifi.setPassword(password);
						Log.d(TAG, "password changed to " + password);
					}
				}
			}
			socialWifi.getWifi().startScan();

		}
		Log.d(TAG, "Closing Everything");
		try {
			sk.close();
			dos.close();
			dis.close();
		} catch (Exception e) {
			e.printStackTrace();
			//            Toast.makeText(context, "Error updating...\nTry again later...", Toast.LENGTH_SHORT).show();
		}
		loadingDialog.dismiss();
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
	}
}